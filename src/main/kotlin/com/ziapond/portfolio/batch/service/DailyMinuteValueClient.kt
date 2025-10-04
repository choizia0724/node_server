package com.ziapond.portfolio.batch.service

import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.common.domain.StockData

import com.ziapond.portfolio.kis.http.KisHttp
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.Timestamp
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
    @Value("\${kis.daily-minute-value.tr-id}")
    private val trId: String,
    @Value("\${kis.daily-minute-value.path}") private val path: String
) {

    private val KST: ZoneId = ZoneId.of("Asia/Seoul")
    private val yyyymmdd = DateTimeFormatter.BASIC_ISO_DATE
    private val HHMMSS = DateTimeFormatter.ofPattern("HHmmss")
    private val HHMM   = DateTimeFormatter.ofPattern("HHmm")

    fun fetchDay(symbol6: String, ymd: LocalDate, time: String?): List<StockData> {
        val all = mutableListOf<StockData>()

        val t = when (time?.length) {
            6 -> LocalTime.parse(time, HHMMSS)
            4 -> LocalTime.parse(time, HHMM)
            else -> throw IllegalArgumentException("time must be HHmm or HHmmss: '$time'")
        }

        ymd.atTime(t).atZone(KST).toOffsetDateTime()

        val query = buildMap {
            put("FID_COND_MRKT_DIV_CODE", "J")
            put("FID_INPUT_ISCD", symbol6)
            put("FID_INPUT_DATE_1", ymd.format(yyyymmdd))
            time?.takeIf { it.isNotBlank() }?.let { put("FID_INPUT_HOUR_1", it) } // 옵션
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

        parseRowToStockData(symbol6, ymd, arr)?.let(all::add)


        return all
            .sortedBy { it.bucketStart }
            .distinctBy { it.bucketStart } // 중복(페이지 경계) 제거
    }

    private fun parseRowToStockData(symbol6: String, ymd: LocalDate, n: JsonNode): StockData? {
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
        val bucketStart = ZonedDateTime.of(ymd, LocalTime.of(hh, mm, ss), KST).toOffsetDateTime()

        // OHLCV
        val open  = text("stck_oprc")?.toBigDecimalOrNull() ?: return null
        val high  = text("stck_hgpr")?.toBigDecimalOrNull() ?: return null
        val low   = text("stck_lwpr")?.toBigDecimalOrNull() ?: return null
        val close = (text("stck_clpr") ?: text("stck_prpr"))?.toBigDecimalOrNull() ?: return null
        val volume = text("cntg_vol")?.toLongOrNull()
            ?: n.path("cntg_vol").asLong(0)

        return StockData(
            symbol = symbol6,
            bucketStart = bucketStart, // <-- mapper가 기대하는 필드명
            open = open, high = high, low = low, close = close, volume = volume
        )
    }
}
