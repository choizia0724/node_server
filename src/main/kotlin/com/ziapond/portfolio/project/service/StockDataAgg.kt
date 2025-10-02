package com.ziapond.portfolio.project.service

import com.ziapond.portfolio.project.domain.StockData
import java.time.*

object StockDataAgg {
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    fun to30m(
        symbol: String,
        windowStart: LocalDateTime,
        ticks: List<MinuteCandleClient.MinuteTick>
    ): StockData? {
        if (ticks.isEmpty()) return null
        val sorted = ticks.sortedBy { it.tsKst }

        val open   = sorted.first().open
        val close  = sorted.last().close
        val high   = sorted.maxOf { it.high }
        val low    = sorted.minOf { it.low }
        val volume = sorted.sumOf { it.volume }

        val bucketStart = OffsetDateTime.of(windowStart, KST.rules.getOffset(Instant.now()))
        return StockData(symbol, bucketStart, open, high, low, close, volume)
    }
}
