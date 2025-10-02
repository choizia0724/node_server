package com.ziapond.portfolio.batch.web.dto

/**
 * @fileoverview
 * @filename Aggregate30mResponse.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */

data class Aggregate30mResponse(
    val skipped: Boolean,
    val reason: String? = null,
    val windowStart: String? = null,
    val windowEnd: String? = null,
    val aggregated: Int = 0
)