package com.ziapond.portfolio.project.dto

data class Aggregate30mResponse(
    val skipped: Boolean,
    val reason: String? = null,
    val windowStart: String? = null,
    val windowEnd: String? = null,
    val aggregated: Int = 0
)