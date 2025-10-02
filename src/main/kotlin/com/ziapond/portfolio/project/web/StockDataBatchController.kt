package com.ziapond.portfolio.project.web

import com.ziapond.portfolio.project.calendar.TradingCalendar
import com.ziapond.portfolio.project.domain.StockData
import com.ziapond.portfolio.project.mappers.StockDataMapper
import com.ziapond.portfolio.project.service.MinuteCandleClient
import com.ziapond.portfolio.project.service.StockDataAgg
import com.ziapond.portfolio.project.service.StockItemInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.max


data class Aggregate30mRequest(
    val symbols: List<String>? = null,   // 지정 없으면 KOSPI 전 종목
    val endTime: String? = null          // "HH:mm" (없으면 현재 시각 기준 눈금 스냅)
)

data class Aggregate30mResponse(
    val skipped: Boolean,
    val reason: String? = null,
    val windowStart: String? = null,
    val windowEnd: String? = null,
    val aggregated: Int = 0
)

@RestController
@RequestMapping("/api/stock-data")
class StockDataBatchController(
    private val calendar: TradingCalendar,
    private val stockItemInfo: StockItemInfo,
    private val minuteClient: MinuteCandleClient,
    private val stockDataMapper: StockDataMapper
) {
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    @PostMapping("/aggregate30m")
    fun aggregate(@RequestBody req: Aggregate30mRequest): ResponseEntity<Aggregate30mResponse> {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) {
            return ResponseEntity.ok(
                Aggregate30mResponse(skipped = true, reason = "Holiday or weekend")
            )
        }

        val windowEnd: LocalTime = req.endTime?.let {
            LocalTime.parse(it) // "HH:mm"
        } ?: snapToHalfHour(now.toLocalTime())

        val windowStart = windowEnd.minusMinutes(30)

        val symbols: List<String> = req.symbols?.takeIf { it.isNotEmpty() } ?: run {
            val beginBasDt = today.minusDays(max(1, 7L))
            stockItemInfo.getStockItemName(beginBasDt)
                .filter { it.mrktctg == "KOSPI" }
                .map { it.symbol }
                .distinct()
        }

        val batch = mutableListOf<StockData>()
        for (sym in symbols) {
            val ticks = minuteClient.fetchWindowTicks(sym, windowStart, windowEnd)
            val bar = StockDataAgg.to30m(sym, LocalDateTime.of(today, windowStart), ticks)
            if (bar != null) batch += bar
        }
        if (batch.isNotEmpty()) stockDataMapper.upsertAll(batch)

        return ResponseEntity.ok(
            Aggregate30mResponse(
                skipped = false,
                windowStart = windowStart.toString(),
                windowEnd = windowEnd.toString(),
                aggregated = batch.size
            )
        )
    }

    private fun snapToHalfHour(t: LocalTime): LocalTime =
        if (t.minute < 30) t.withMinute(30).withSecond(0)
        else t.withMinute(0).withSecond(0).plusHours(1)
}