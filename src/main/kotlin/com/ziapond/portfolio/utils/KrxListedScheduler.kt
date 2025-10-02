package com.ziapond.portfolio.utils

import com.ziapond.portfolio.project.service.StockItemInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

/**
 * @fileoverview
 * @filename KrxListedScheduler.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */


@Component
class KrxListedScheduler(
    private val service: StockItemInfo,
    @Value("\${schedules.krx.listed.enabled:true}") private val enabled: Boolean,
    @Value("\${schedules.krx.listed.num-of-rows:3000}") private val numOfRows: Int,
    @Value("\${schedules.krx.listed.date-mode:lastWeekMonday}") private val dateMode: String,
) {
    private val KST = ZoneId.of("Asia/Seoul")

    private val lastDay = LastWeekMonday().lastWeekMonday();

    /** 평일 오전 06:10 실행 (월~금) */
    //@Scheduled(cron = "\${schedules.krx.listed.cron:0 10 6 * * MON-FRI}", zone = "Asia/Seoul")
    fun run() {
        if (!enabled) return

        val date = when (dateMode.lowercase()) {
            "yesterday" -> LocalDate.now(KST).minusDays(1)
            "today"     -> LocalDate.now(KST)
            "lastweekmonday" -> lastDay
            else -> lastDay
        }

        service.fetchAndUpsert(date, numOfRows)
    }

}
