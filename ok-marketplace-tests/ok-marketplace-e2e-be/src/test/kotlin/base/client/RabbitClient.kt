package ru.otus.otuskotlin.marketplace.e2e.be.base.client

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import ru.otus.otuskotlin.marketplace.e2e.be.base.DockerCompose

/**
 * Клиент, работающий через rabbit-mq
 * Запросы уходят в $version-queue, а ответы читаются из $version-queue-out
 */
class RabbitClient(
    dockerCompose: DockerCompose,
) : Client {
    private val channel by lazy {
//        Thread.sleep(20_000)
        ConnectionFactory().apply {
            val url = dockerCompose.inputUrl
            host = url.host
            port = url.port
            username = dockerCompose.user
            password = dockerCompose.password
        }.newConnection().createChannel()
    }
    private val coroChannelByVersion = mutableMapOf<String, Channel<String>>()

    private fun getCoroChannel(version: String): Channel<String> = coroChannelByVersion.computeIfAbsent(version) {
        val coroChannel = Channel<String>()

        val exchangeName = "marketplace.exchange.$version"
        val queueOutName = "$version-queue-out"
        val routingKeyOut = "$version-queue-out"

        // Используем false, так как сервер создает неустойчивый exchange
        channel.exchangeDeclare(exchangeName, "direct", false)

        // Создаем неустойчивую, эксклюзивную, автоудаляемую очередь для ответов
        channel.queueDeclare(queueOutName, false, false, true, null)
        channel.queueBind(queueOutName, exchangeName, routingKeyOut)

        val deliverCallback = DeliverCallback { consumerTag, delivery ->
            val responseJson = String(delivery.body, Charsets.UTF_8)
            println("Received in callback $version by $consumerTag:\n$responseJson")
            runBlocking {
                coroChannel.send(responseJson)
            }
        }

        channel.basicConsume(queueOutName, true, deliverCallback, CancelCallback { })

        coroChannel
    }

    override suspend fun sendAndReceive(version: String, path: String, request: String): String {
        val coroChannel = getCoroChannel(version)
        coroChannel.tryReceive()

        val exchangeName = "marketplace.exchange.$version"
        val routingKeyIn = "$version-queue"

        println("Send $version:\n$request")

        channel.queueDeclare(routingKeyIn, false, false, false, null)
        channel.queueBind(routingKeyIn, exchangeName, routingKeyIn)

        channel.basicPublish(exchangeName, routingKeyIn, null, request.toByteArray())

        val response = coroChannel.receive()
        println("Received:\n$response")
        return response
    }
}
