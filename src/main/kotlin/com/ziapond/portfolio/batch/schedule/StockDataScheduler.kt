package com.ziapond.portfolio.project.schedule

import com.ziapond.portfolio.project.calendar.TradingCalendar
import com.ziapond.portfolio.common.domain.StockData
import com.ziapond.portfolio.common.mappers.StockDataMapper
import com.ziapond.portfolio.batch.service.StockDataAgg
import com.ziapond.portfolio.batch.service.MinuteCandleClient
import com.ziapond.portfolio.batch.service.StockItemInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*
import kotlin.math.max

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
    fun runHalfHourly() {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) {
            // 휴장일 → skip
            return
        }

        val windowEnd = snapToHalfHour(now.toLocalTime())   // 09:30, 10:00, ...
        val windowStart = windowEnd.minusMinutes(30)

        // 최근 기준일 기준 KOSPI 심볼 목록
        val beginBasDt = today.minusDays(max(1, basdtOffsetDays))
        val kospiSymbols = stockItemInfo.getStockItemName(beginBasDt)
            .filter { it.mrktctg == "KOSPI" }
            .map { it.symbol }
            .distinct()

        val batch = mutableListOf<StockData>()
        for (sym in kospiSymbols) {
            val ticks = minuteClient.fetchWindowTicks(sym, windowStart, windowEnd)
            val bar = StockDataAgg.to30m(sym, LocalDateTime.of(today, windowStart), ticks)
            if (bar != null) batch += bar
        }
        if (batch.isNotEmpty()) stockDataMapper.upsertAll(batch)
    }

    private fun snapToHalfHour(t: LocalTime): LocalTime =
        if (t.minute < 30) t.withMinute(30).withSecond(0)
        else t.withMinute(0).withSecond(0).plusHours(1)
}
