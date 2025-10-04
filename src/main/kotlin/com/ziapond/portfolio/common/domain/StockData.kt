package com.ziapond.portfolio.common.domain

import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * @fileOverview 주식데이터
 * @path /StockData.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */
data class StockData(
    val symbol: String,
    val tsKst: OffsetDateTime,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)