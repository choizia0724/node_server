package com.ziapond.portfolio.batch.service

import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.common.domain.StockDataResponse

import com.ziapond.portfolio.kis.http.KisHttp
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.*
import java.time.format.DateTimeFormatter
/**
 * @fileoverview
 * @filename DailyMinuteValueClient.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 4.
 * @copyright 2025,
 */

@Service
class DailyMinuteValueClient(
    private val http: KisHttp,
    @Value("\${kis.minute-value.tr-id}")
    private val trId: String,
    @Value("\${kis.minute-value.path}") private val path: String
) {

    private val KST: ZoneId = ZoneId.of("Asia/Seoul")
    private val yyyymmdd = DateTimeFormatter.BASIC_ISO_DATE
    private val HHMMSS = DateTimeFormatter.ofPattern("HHmmss")
    private val HHMM   = DateTimeFormatter.ofPattern("HHmm")

    fun fetchDay(symbol6: String, ymd: LocalDate, time: String?): List<StockDataResponse> {
        val all = mutableListOf<StockDataResponse>()

        val lt : LocalTime = when (time?.length) {
            6 -> LocalTime.parse(time, HHMMSS)
            4 -> LocalTime.parse(time, HHMM)
            else -> throw IllegalArgumentException("time must be HHmm or HHmmss: '$time'")
        }

        val tKst: LocalDateTime = ymd.atTime(lt)                          // ← ymd + time

        println("ymd: "+ ymd.format(yyyymmdd) +", t: "+time)

        val query = buildMap {
            put("FID_COND_MRKT_DIV_CODE", "J")
            put("FID_INPUT_ISCD", symbol6)
            put("FID_ETC_CLS_CODE", "J")
            put("FID_INPUT_HOUR_1", time)
            put("FID_PW_DATA_INCU_YN","Y")
        }

        val node: JsonNode? = http.getJson(
            path = path,
            query = query,
            trId = trId,
            auth = true,
        )
        println(node)

        // 응답의 분봉 배열(output2 또는 호환 위치)
        val arr = node!!.path("output2")

        parseRowToStockData(symbol6, tKst, arr)?.let(all::add)

        return all.toList()
    }

    private fun parseRowToStockData(symbol6: String, tsKst: LocalDateTime, n: JsonNode): StockDataResponse? {
        fun text(name: String): String? =
            n.path(name).asText().takeIf { it.isNotBlank() }

        val hhmmssRaw = text("stck_cntg_hour") ?: text("stck_bsop_time") ?: return null
        val (hh, mm, ss) = when (hhmmssRaw.length) {
            6 -> Triple(
                hhmmssRaw.substring(0, 2).toIntOrNull(),
                hhmmssRaw.substring(2, 4).toIntOrNull(),
                hhmmssRaw.substring(4, 6).toIntOrNull()
            )
            4 -> Triple(
                hhmmssRaw.substring(0, 2).toIntOrNull(),
                hhmmssRaw.substring(2, 4).toIntOrNull(),
                0
            )
            else -> return null
        }
        if (hh == null || mm == null || ss == null) return null
        // Time
        val dateStr = text("stck_bsop_date") 
        val timeStr = text("stck_cntg_hour") 

        val date = LocalDate.parse(dateStr, yyyymmdd)
        val time = LocalTime.parse(timeStr, HHMMSS)

        val timeToLocalTime = LocalDateTime.of(date, time)
        
        // OHLCV
        val open  = text("stck_oprc")?.toBigDecimalOrNull() ?: return null
        val high  = text("stck_hgpr")?.toBigDecimalOrNull() ?: return null
        val low   = text("stck_lwpr")?.toBigDecimalOrNull() ?: return null
        val close =  text("stck_prpr")?.toBigDecimalOrNull() ?: return null
        val volume = text("acml_vol")?.toLongOrNull() ?: return null

        val oneTick = StockDataResponse.MinuteTick(
            tsKst = timeToLocalTime,
            open = open, high = high, low = low, close = close, volume = volume
        )

        return StockDataResponse(
            symbol = symbol6,
            tick = listOf(oneTick)
        )
    }
}
