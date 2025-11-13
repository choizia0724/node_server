package com.ziapond.portfolio.batch.schedule

import com.ziapond.portfolio.calendar.TradingCalendar
import com.ziapond.portfolio.batch.service.MinuteCandleClient
import com.ziapond.portfolio.batch.service.StockItemInfo
import com.ziapond.portfolio.common.domain.StockDataResponse
import com.ziapond.portfolio.common.mappers.StockDataMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*

@Component
class StockDataScheduler(
    private val calendar: TradingCalendar,
    private val stockItemInfo: StockItemInfo,
    private val minuteClient: MinuteCandleClient,
    private val stockDataMapper: StockDataMapper,
    @Value("\${schedules.krx.listed.basdt-offset-days:7}") private val basdtOffsetDays: Long
) {
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    /** 평일 09:30~15:30 매 30분 */
    @Scheduled(cron = "0 0,30 9-15 * * MON-FRI", zone = "Asia/Seoul")
    fun runKOSPIHalfHourly() {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) return

        val windowEnd: LocalDateTime = snapToHalfHour(now.toLocalDateTime()) // 09:30, 10:00, ...
        val windowStart: LocalDateTime = windowEnd.minusMinutes(30)

        // 최근 기준일 기준 KOSPI 심볼 목록
        val kospiSymbols = stockItemInfo.getSymbolsFromDb(null, null, "KOSPI", true)
            .map { it.symbol }
            .distinct()

        val batch = mutableListOf<StockDataResponse>()
        for (sym in kospiSymbols) {
            val ticks = minuteClient.fetchWindowTicks(sym, windowEnd)
            val bar = StockDataResponse(sym, ticks)
            batch += bar
        }
        if (batch.isNotEmpty()) stockDataMapper.upsertAll(batch)
    }

    /** 평일 09:30~15:30 매 30분 */
    @Scheduled(cron = "0 0,30 9-15 * * MON-FRI", zone = "Asia/Seoul")
    fun runKOSDAQHalfHourly() {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) return

        val windowEnd: LocalDateTime = snapToHalfHour(now.toLocalDateTime())
        val windowStart: LocalDateTime = windowEnd.minusMinutes(30)

        // 최근 기준일 기준 KOSDAQ 심볼 목록
        val kosdaqSymbols = stockItemInfo.getSymbolsFromDb(null, null, "KOSDAQ", true)
            .map { it.symbol }
            .distinct()

        val batch = mutableListOf<StockDataResponse>()
        for (sym in kosdaqSymbols) {
            val ticks = minuteClient.fetchWindowTicks(sym, windowEnd)
            val bar = StockDataResponse(sym, ticks)
            batch += bar
        }
        if (batch.isNotEmpty()) stockDataMapper.upsertAll(batch)
    }
    /** 평일 09:30~15:30 매 30분 */
    @Scheduled(cron = "0 0,30 9-15 * * MON-FRI", zone = "Asia/Seoul")
    fun runKODEXHalfHourly() {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) return

        val windowEnd: LocalDateTime = snapToHalfHour(now.toLocalDateTime())
        val windowStart: LocalDateTime = windowEnd.minusMinutes(30)

        // 최근 기준일 기준 KOSDAQ 심볼 목록
        val kosdaqSymbols = stockItemInfo.getSymbolsFromDb(null, null, "삼성자산운용", true)
            .map { it.symbol }
            .distinct()

        val batch = mutableListOf<StockDataResponse>()
        for (sym in kosdaqSymbols) {
            val ticks = minuteClient.fetchWindowTicks(sym, windowEnd)
            val bar = StockDataResponse(sym, ticks)
            batch += bar
        }
        if (batch.isNotEmpty()) stockDataMapper.upsertAll(batch)
    }


    private fun snapToHalfHour(t: LocalDateTime): LocalDateTime =
        if (t.minute > 30) t.withMinute(30).withSecond(0)
        else t.withMinute(0).withSecond(0).plusHours(1)
}
