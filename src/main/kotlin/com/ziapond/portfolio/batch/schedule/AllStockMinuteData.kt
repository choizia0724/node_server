package com.ziapond.portfolio.batch.schedule

import com.ziapond.portfolio.batch.service.MinuteCandleClient
import com.ziapond.portfolio.batch.service.StockItemInfo
import com.ziapond.portfolio.calendar.TradingCalendar
import com.ziapond.portfolio.common.domain.StockData
import mappers.StockDataMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*
import kotlin.math.max

@Component
class AllStockMinuteData(
    private val calendar: TradingCalendar,
    private val stockItemInfo: StockItemInfo,
    private val minuteClient: MinuteCandleClient,
    private val mapper: mappers.StockDataMapper,
    @Value("\${batch.symbol.market:KOSPI}") private val marketCode: String
) {
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    /**
     * 평일 09:01~15:30 매 1분(정각)에 직전 1분 구간을 수집
     * 예: 09:02에 실행 → [09:01, 09:02) 구간 1분틱 저장
     */
    @Scheduled(cron = "0 * 9-15 * * MON-FRI", zone = "Asia/Seoul")
    fun collectPerMinute() {
        val now = ZonedDateTime.now(KST).withSecond(0).withNano(0)
        val today = now.toLocalDate()
        if (!calendar.isTradingDay(today)) return

        val windowEnd = now.toLocalTime()
        val windowStart = windowEnd.minusMinutes(1)

        // 최근 기준일로 KOSPI 심볼 목록
        val beginBasDt = today.minusDays(max(1, 7L))
        val symbols = stockItemInfo.getStockItemName(beginBasDt)
            .filter { (it.mrktctg ?: "") == marketCode }
            .map { it.symbol }
            .distinct()

        val batch = symbols.flatMap { sym ->
            minuteClient.fetchWindowTicks(sym, windowStart, windowEnd).map { t ->
                StockData(
                    symbol = sym,
                    bucketStart  = t.tsKst,
                    open   = t.open,
                    high   = t.high,
                    low    = t.low,
                    close  = t.close,
                    volume = t.volume
                )
            }
        }

        if (batch.isNotEmpty()) mapper.upsertAll(batch)
    }
}
