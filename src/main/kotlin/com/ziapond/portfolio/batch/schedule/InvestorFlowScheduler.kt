package com.ziapond.portfolio.batch.schedule

import com.ziapond.portfolio.project.calendar.TradingCalendar
import com.ziapond.portfolio.common.domain.InvestorFlow
import com.ziapond.portfolio.common.mappers.InvestorFlowMapper
import com.ziapond.portfolio.batch.service.InvestorFlowClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*

/**
 * @fileoverview
 * @filename InvestorFlowScheduler.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */

@Component
class InvestorFlowScheduler(
    private val calendar: TradingCalendar,
    private val client: InvestorFlowClient,
    private val mapper: InvestorFlowMapper,
    @Value("\${batch.investor.markets}") marketsCsv: String
) {
    private val KST = ZoneId.of("Asia/Seoul")
    private val markets: List<String> = marketsCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }

    /** 평일 09:30~15:30 매 30분 */
    @Scheduled(cron = "0 0,30 9-15 * * MON-FRI", zone = "Asia/Seoul")
    fun runHalfHourly() {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) return

        val windowEnd = snapToHalfHour(now.toLocalTime())   // 09:30, 10:00, ...
        val windowStart = windowEnd.minusMinutes(30)

        val batch = mutableListOf<InvestorFlow>()
        for (mkt in markets) {
            val rows = client.fetchWindowByMarket(mkt, windowStart, windowEnd)
            batch += rows
        }
        if (batch.isNotEmpty()) mapper.upsertAll(batch)
    }

    private fun snapToHalfHour(t: LocalTime): LocalTime =
        if (t.minute < 30) t.withMinute(30).withSecond(0)
        else t.withMinute(0).withSecond(0).plusHours(1)
}
