package ru.otus.otuskotlin.marketplace.app.rabbit.mappers

import ru.otus.otuskotlin.marketplace.app.rabbit.config.RabbitConfig

fun RabbitConfig.Companion.fromArgs(vararg args: String) = RabbitConfig(
    host = args.arg("-h") ?: System.getenv("RABBIT_HOST") ?: HOST,
    port = args.arg("-p")?.toInt()  ?: System.getenv("RABBIT_PORT")?.toInt() ?: PORT,
    user = args.arg("-u") ?: System.getenv("RABBIT_USER") ?: USER,
    password = args.arg("-pw") ?: System.getenv("RABBIT_PASS") ?: PASSWORD,
)

private fun Array<out String>.arg(option: String) = indexOf(option)
    .takeIf { it != -1 }
    ?.let { this[it + 1] }
