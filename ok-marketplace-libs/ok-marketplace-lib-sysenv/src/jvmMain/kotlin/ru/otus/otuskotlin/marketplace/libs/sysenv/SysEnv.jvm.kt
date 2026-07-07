package ru.otus.otuskotlin.marketplace.libs.sysenv

actual fun sysEnv(key: String): String? =
    System.getenv(key)
