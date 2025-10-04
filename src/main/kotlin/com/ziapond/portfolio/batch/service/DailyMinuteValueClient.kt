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
    @Value("\${kis.daily-minute-value.tr_id}")
    private val trId: String,
    @Value("\${kis.daily-minute-value.path}") private val path: String
) {
    private val KST = ZoneId.of("Asia/Seoul")
    private val yyyymmdd = DateTimeFormatter.BASIC_ISO_DATE // yyyyMMdd

    /**
     * KIS 일중(분) 데이터 조회 (최대 120개/호출). 페이지네이션(ctx_area_fk/nk) 대응.
     * @param symbol6 6자리 종목코드 (예: "005930")
     * @param ymd     영업일(한국 기준)
     */
    fun fetchDay(symbol6: String, ymd: LocalDate, time:String?): List<StockData> {
        val all = mutableListOf<StockData>()

        var ctxFk: String? = null
        var ctxNk: String? = null
        var trContHeader = ""          // 첫 호출은 공백, 이후 페이지가 있으면 "N"

        while (true) {
            val query = buildMap {
                put("FID_COND_MRKT_DIV_CODE", "J")                  // 국내주식
                put("FID_INPUT_ISCD", symbol6)                      // 6자리 코드
                put("FID_INPUT_DATE_1", ymd.format(yyyymmdd))       // 조회일
                put("FID_INPUT_HOUR_1", time)                       // 시간 1300000
                // KIS 계정마다 필요한 쿼리 키가 더 있을 수 있음: FID_PERIOD_DIV_CODE 등
                ctxFk?.let { put("CTX_AREA_FK", it) }
                ctxNk?.let { put("CTX_AREA_NK", it) }
            }

            val node: JsonNode? = http.getJson(
                path = path,
                query = query,
                trId = trId,
                headers = mapOf("tr_cont" to trContHeader) // 페이지네이션 제어
            )

            if (node == null) break

            // 본문 배열 추출 (계정/환경에 따라 output2 또는 nested 위치가 다를 수 있어 유연하게)
            val arr = listOf(
                node.path("output2"),
                node.path("output"),                   // 일부 응답 스키마 호환
                node.path("response").path("body").path("items").path("item")
            ).firstOrNull { it.isArray || (!it.isMissingNode && !it.isNull) }

            if (arr == null || arr.isMissingNode || arr.isNull) break

            val rows = if (arr.isArray) arr.toList() else listOf(arr)
            rows.forEach { n ->
                parseRow(symbol6, ymd, n)?.let { all += it }
            }

            // 다음 페이지 토큰 추출 (본문에 내려오는 경우가 많음)
            val ctx = listOf(node, node.path("output"), node.path("output3"))
            ctxFk = ctx.firstNotNullOfOrNull { it.path("ctx_area_fk").asText(null) }
            ctxNk = ctx.firstNotNullOfOrNull { it.path("ctx_area_nk").asText(null) }

            // 다음 페이지가 없으면 종료
            if (ctxFk.isNullOrBlank() || ctxNk.isNullOrBlank()) break

            // 다음 페이지 호출
            trContHeader = "N"
        }

        // 시간순 정렬 및 동일분 중복 제거(있다면 마지막 값 우선)
        return all
            .sortedBy { it.tsKst }
            .distinctBy { it.tsKst }
    }

    private fun parseRow(symbol6: String, ymd: LocalDate, n: JsonNode): StockData? {
        // 시간 필드 후보: stck_bsop_time / stck_cntg_hour (HHmmss)
        val hhmmss = n.path("stck_bsop_time").asText(
            n.path("stck_cntg_hour").asText("")
        )
        if (hhmmss.length < 4) return null
        val hh = hhmmss.substring(0, 2).toIntOrNull() ?: return null
        val mm = hhmmss.substring(2, 4).toIntOrNull() ?: 0
        val time = LocalTime.of(hh, mm, 0)
        val ts = OffsetDateTime.of(ymd, time, KST.rules.getOffset(Instant.now()))

        fun bd(key: String): BigDecimal? =
            n.path(key).asText().takeIf { it.isNotBlank() }?.toBigDecimalOrNull()

        val open  = bd("stck_oprc") ?: return null
        val high  = bd("stck_hgpr") ?: return null
        val low   = bd("stck_lwpr") ?: return null
        val close = bd("stck_clpr") ?: bd("stck_prpr") ?: open
        val vol   = n.path("acml_vol").asLong(n.path("cntg_vol").asLong(0))

        return StockData(
            symbol = symbol6,
            tsKst  = ts,
            open   = open,
            high   = high,
            low    = low,
            close  = close,
            volume = vol
        )
    }
}
