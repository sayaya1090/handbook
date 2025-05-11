package dev.sayaya.handbook.domain

import java.time.Instant

data class Period(
    val start: Instant,
    val end: Instant
)