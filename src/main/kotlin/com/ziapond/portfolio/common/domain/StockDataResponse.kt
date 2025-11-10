package com.ziapond.portfolio.common.domain

import com.ziapond.portfolio.batch.service.MinuteCandleClient.MinuteTick
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * @fileOverview 주식데이터
 * @path /StockDataResponse.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */
data class StockDataResponse(
    val symbol: String,
//    val bucketStart: LocalDateTime,
    val tick :List<MinuteTick>,
//    val open: BigDecimal,
//    val high: BigDecimal,
//    val low: BigDecimal,
//    val close: BigDecimal,
//    val volume: Long
)