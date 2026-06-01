package ru.otus.otuskotlin.marketplace.e2e.be.base.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import ru.otus.otuskotlin.marketplace.e2e.be.base.DockerCompose
import java.time.Duration
import java.util.*

/**
 * Отправка запросов в очереди kafka
 */
class KafkaClient(dockerCompose: DockerCompose) : Client {
    private val host = "localhost:9092"

    private val producer by lazy {
        KafkaProducer<String, String>(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to host,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
            )
        )
    }
    private val consumer by lazy {
        KafkaConsumer<String, String>(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to host,
                ConsumerConfig.GROUP_ID_CONFIG to UUID.randomUUID().toString(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
            )
        ).also { cons ->
            cons.subscribe(versions.map { "marketplace-ad-$it-out" })
        }
    }
    private var counter = 0
    private val versions = setOf("v1", "v2")

    override suspend fun sendAndReceive(version: String, path: String, request: String): String {
        if (version !in versions) {
            throw UnsupportedOperationException("Unknown version $version")
        }

        counter += 1
        withContext(Dispatchers.IO) {
            producer.send(ProducerRecord("marketplace-ad-$version-in", "test-$counter", request)).get()
        }

        val read = consumer.poll(Duration.ofSeconds(10))
        return read.firstOrNull()?.value() ?: ""
    }
}