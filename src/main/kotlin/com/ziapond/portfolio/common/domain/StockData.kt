package com.ziapond.portfolio.common.domain

import java.math.BigDecimal
import java.time.LocalDateTime

data class StockData(
    val symbol: String,
    val tsKst: LocalDateTime,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)