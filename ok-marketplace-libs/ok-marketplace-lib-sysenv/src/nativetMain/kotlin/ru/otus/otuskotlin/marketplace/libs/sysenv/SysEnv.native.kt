package ru.otus.otuskotlin.marketplace.libs.sysenv

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual fun sysEnv(key: String): String? =
    getenv(key)?.toKString()
