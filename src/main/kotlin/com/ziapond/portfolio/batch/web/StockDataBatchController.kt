package com.ziapond.portfolio.batch.web

import com.ziapond.portfolio.batch.service.DailyMinuteCollector
import com.ziapond.portfolio.calendar.TradingCalendar
import com.ziapond.portfolio.batch.service.MinuteCandleClient
import com.ziapond.portfolio.batch.service.StockItemInfo
import com.ziapond.portfolio.batch.web.dto.Aggregate30mRequest
import com.ziapond.portfolio.batch.web.dto.Aggregate30mResponse
import com.ziapond.portfolio.batch.web.dto.SyncDailyMinuteRequest
import com.ziapond.portfolio.batch.web.dto.SyncDailyMinuteResponse
import com.ziapond.portfolio.common.domain.StockDataResponse
import com.ziapond.portfolio.common.domain.StockTable
import com.ziapond.portfolio.common.mappers.StockDataMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.*


@RestController
@RequestMapping("/api/stock-data")
class StockDataBatchController(
    private val calendar: TradingCalendar,
    private val stockItemInfo: StockItemInfo,
    private val minuteClient: MinuteCandleClient,
    private val stockDataMapper: StockDataMapper,
    @Value("\${batch.investor.markets}") private val marketsCsv: String,
    private val collector: DailyMinuteCollector,
) {
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    @PostMapping("/aggregate30m/kospi")
    fun aggregateKOSPI(@RequestBody req: Aggregate30mRequest): ResponseEntity<Aggregate30mResponse> {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) {
            return ResponseEntity.ok(
                    Aggregate30mResponse(skipped = true, reason = "Holiday or weekend")
            )
        }

        val windowEnd: LocalDateTime =
            req.endTime?.let { LocalDateTime.parse(it) }
                ?: snapToHalfHour(now.toLocalDateTime())

        val windowStart = windowEnd.minusMinutes(30)

        val symbols: List<StockTable> =
            stockItemInfo.getSymbolsFromDb(req.symbols,null,"KOSPI",true)

        val batch = mutableListOf<StockDataResponse>()
        for (sym in symbols) {
            val ticks = minuteClient.fetchWindowTicks(sym.symbol, windowEnd)
            val bar = StockDataResponse(sym.symbol, ticks)
            batch += bar
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

    @PostMapping("/aggregate30m/kosdaq")
    fun aggregateKOSDAQ(@RequestBody req: Aggregate30mRequest): ResponseEntity<Aggregate30mResponse> {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) {
            return ResponseEntity.ok(
                Aggregate30mResponse(skipped = true, reason = "Holiday or weekend")
            )
        }

        val windowEnd: LocalDateTime =
            req.endTime?.let { LocalDateTime.parse(it) }
                ?: snapToHalfHour(now.toLocalDateTime())

        val windowStart = windowEnd.minusMinutes(30)

        val symbols: List<StockTable> =
            stockItemInfo.getSymbolsFromDb(req.symbols,null,"KOSDAQ",true)

        val batch = mutableListOf<StockDataResponse>()
        for (sym in symbols) {
            val ticks = minuteClient.fetchWindowTicks(sym.symbol, windowEnd)
            val bar = StockDataResponse(sym.symbol, ticks)
            batch += bar
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


    @PostMapping("/aggregate30m/kodex")
    fun aggregateKODEX(@RequestBody req: Aggregate30mRequest): ResponseEntity<Aggregate30mResponse> {
        val now = ZonedDateTime.now(KST)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) {
            return ResponseEntity.ok(
                Aggregate30mResponse(skipped = true, reason = "Holiday or weekend")
            )
        }

        val windowEnd: LocalDateTime =
            req.endTime?.let { LocalDateTime.parse(it) }
                ?: snapToHalfHour(now.toLocalDateTime())

        val windowStart = windowEnd.minusMinutes(30)

        val symbols: List<StockTable> =
            stockItemInfo.getSymbolsFromDb(req.symbols,null,"삼성자산운용",true)

        val batch = mutableListOf<StockDataResponse>()
        for (sym in symbols) {
            val ticks = minuteClient.fetchWindowTicks(sym.symbol, windowEnd)
            val bar = StockDataResponse(sym.symbol, ticks)
            batch += bar
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

    @PostMapping("/aggregateDaily120m")
    fun sync(@RequestBody req: SyncDailyMinuteRequest): ResponseEntity<SyncDailyMinuteResponse> {
        val time = req.time
        val date = req.ymd?.let(LocalDate::parse) ?: LocalDate.now(KST)

        val processed: List<String> = collector.collectForDate(time, date, req.symbols)

        val body = SyncDailyMinuteResponse(
            requestedDate = date.toString(),
            requestedTime = req.time,
            processedSymbols = processed
        )

        return if (processed.isNotEmpty()) {
            ResponseEntity.ok(body)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(body)
        }
    }
    private fun snapToHalfHour(t: LocalDateTime): LocalDateTime =
        if (t.minute < 30) t.withMinute(30).withSecond(0)
        else t.withMinute(0).withSecond(0).plusHours(1)
}