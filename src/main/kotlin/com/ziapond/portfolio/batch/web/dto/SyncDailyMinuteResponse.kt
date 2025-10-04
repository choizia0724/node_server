package com.ziapond.portfolio.batch.web.dto

/**
 * @fileoverview
 * @filename SyncDailyMinuteResponse.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 4.
 * @copyright 2025,
 */

data class SyncDailyMinuteResponse(
    val requestedDate: String,
    val requestedTime: String?,
    val processedSymbols: List<String>
)