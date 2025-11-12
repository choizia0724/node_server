package com.ziapond.portfolio.common.domain

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
    val symbol: String?,
    val tick :List<MinuteTick>,
){
    data class MinuteTick(
        val tsKst: LocalDateTime?,
        val open: BigDecimal?,
        val high: BigDecimal?,
        val low: BigDecimal?,
        val close: BigDecimal?,
        val volume: Long?
    )
}
/** DB에서 읽은 바(행)들을 심볼 단위 응답으로 변환 */
fun List<StockData>.toResponse(symbol: String?): StockDataResponse {
    val ticks = this
        .sortedBy { it.tsKst }  // 시간 오름차순
        .map {
            StockDataResponse.MinuteTick(
                tsKst  = it.tsKst,
                open   = it.open,
                high   = it.high,
                low    = it.low,
                close  = it.close,
                volume = it.volume
            )
        }
        .distinctBy { it.tsKst }      // 페이지 경계 중복이 있으면 제거
    return StockDataResponse(symbol = symbol, tick = ticks)
}