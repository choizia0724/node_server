package com.ziapond.portfolio.batch.service

import com.ziapond.portfolio.calendar.TradingCalendar
import com.ziapond.portfolio.common.domain.StockData
import com.ziapond.portfolio.common.mappers.StockDataMapper
import com.ziapond.portfolio.common.mappers.StockListMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

/**
 * @fileoverview
 * @filename DailyMinuteCollector.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 4.
 * @copyright 2025,
 */

@Service
class DailyMinuteCollector(
    private val calendar: TradingCalendar,
    private val stockListMapper: StockListMapper,
    private val client: DailyMinuteValueClient,
    private val mapper: StockDataMapper,
    @Value("\${batch.daily.markets}") private val markets: String,
) {
    private val KST = ZoneId.of("Asia/Seoul")

    /**
     * KOSPI 전 종목(or 주어진 심볼 리스트)의 특정 영업일 분데이터 수집 후 업서트
     * @return 처리한 종목 수
     */
    fun collectForDate(time:String? ,ymd: LocalDate, symbols: List<String>? = null): List<String> {
        if (!calendar.isTradingDay(ymd)) return emptyList()

        val syms: List<String> = symbols?.takeIf { it.isNotEmpty() } ?: run {
            stockListMapper.searchStocks(null, null, markets,null,null)
                .map { it.symbol }
                .distinct()
        }
        println(syms.joinToString(", "))

        val batch = ArrayList<StockData>(4096)
        fun flush() {
            if (batch.isNotEmpty()) {
                mapper.upsertAll(batch)
                batch.clear()
            }
        }

        for (s in syms) {
            val rows = client.fetchDay(s, ymd, time)
            if (rows.isNotEmpty()) {
                batch.addAll(rows)
                if (batch.size >= 10_000) flush()
            }
            println(rows.joinToString(", "))
        }
        flush()
        return syms
    }
}