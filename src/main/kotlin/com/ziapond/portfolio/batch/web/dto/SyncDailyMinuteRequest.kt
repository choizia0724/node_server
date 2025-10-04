package com.ziapond.portfolio.batch.web.dto

/**
 * @fileoverview
 * @filename SyncDailyMinuteRequest.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 4.
 * @copyright 2025,
 */

data class SyncDailyMinuteRequest(
    val ymd: String? = null,            // "yyyy-MM-dd" (없으면 오늘)
    val time: String? = null,           // "hhmmss"
    val symbols: List<String>? = null   // 없으면 KOSPI 전 종목
)