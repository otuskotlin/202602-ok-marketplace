package ru.otus.otuskotlin.marketplace.states.common

import kotlin.time.Instant


private val INSTANT_NONE = Instant.fromEpochMilliseconds(Long.MIN_VALUE)
val Instant.Companion.NONE
    get() = INSTANT_NONE
