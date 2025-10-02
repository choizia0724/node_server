package com.ziapond.portfolio.batch.web.dto

/**
 * @fileoverview
 * @filename Aggregate30mRequest.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */

data class Aggregate30mRequest(
    val symbols: List<String>? = null,   // 지정 없으면 KOSPI 전 종목
    val endTime: String? = null          // "HH:mm" (없으면 현재 시각 기준 눈금 스냅)
)