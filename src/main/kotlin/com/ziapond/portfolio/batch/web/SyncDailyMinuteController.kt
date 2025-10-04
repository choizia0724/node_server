package com.ziapond.portfolio.batch.web
// com/ziapond/portfolio/project/web/SyncDailyMinuteController.kt

import com.ziapond.portfolio.batch.service.DailyMinuteCollector
import com.ziapond.portfolio.batch.web.dto.SyncDailyMinuteRequest
import com.ziapond.portfolio.batch.web.dto.SyncDailyMinuteResponse
import org.springframework.cglib.core.Local
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId

/**
 * @fileoverview
 * @filename SyncDailyMinuteController.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 4.
 * @copyright 2025,
 */


@RestController
@RequestMapping("/api/batch/daily-minute")
class SyncDailyMinuteController(
    private val collector: DailyMinuteCollector
) {
    private val KST = ZoneId.of("Asia/Seoul")

    @PostMapping("/sync")
    fun sync(@RequestBody req: SyncDailyMinuteRequest): ResponseEntity<SyncDailyMinuteResponse> {
        val time = req.time
        val date = req.ymd?.let { LocalDate.parse(it) } ?: LocalDate.now(KST)
        val count = collector.collectForDate(time, date, req.symbols)
        return ResponseEntity.ok(
            SyncDailyMinuteResponse(
                requestedDate = date.toString(),
                processedSymbols = count
            )
        )
    }
}
