package ru.otus.otuskotlin.marketplace.app.rabbit

import kotlinx.coroutines.runBlocking
import ru.otus.otuskotlin.marketplace.app.rabbit.config.MkplAppSettings
import ru.otus.otuskotlin.marketplace.app.rabbit.config.RabbitConfig
import ru.otus.otuskotlin.marketplace.app.rabbit.config.RabbitExchangeConfiguration
import ru.otus.otuskotlin.marketplace.app.rabbit.mappers.fromArgs
import ru.otus.otuskotlin.marketplace.common.MkplCorSettings
import ru.otus.otuskotlin.marketplace.logging.common.MpLoggerProvider
import ru.otus.otuskotlin.marketplace.logging.jvm.mpLoggerLogback

fun main(vararg args: String) = runBlocking {
    val appSettings = MkplAppSettings(
        rabbit = RabbitConfig.fromArgs(*args),
        corSettings = MkplCorSettings(
            loggerProvider = MpLoggerProvider { mpLoggerLogback(it) }
        ),
        controllersConfigV1 = RabbitExchangeConfiguration(
            exchange = "marketplace.exchange.v1",
            exchangeType = "direct",
            queue = "v1-queue",
            keyIn = "v1-queue",          // routing key для приёма
            keyOut = "v1-queue-out",     // ответы в эту очередь
            consumerTag = "marketplace-consumer-v1"
        ),
        controllersConfigV2 = RabbitExchangeConfiguration(
            exchange = "marketplace.exchange.v2",
            exchangeType = "direct",
            queue = "v2-queue",
            keyIn = "v2-queue",          // routing key для приёма
            keyOut = "v2-queue-out",     // ответы в эту очередь
            consumerTag = "marketplace-consumer-v2"
        ),
    )
    val app = RabbitApp(appSettings = appSettings, this)
    app.start()
}
