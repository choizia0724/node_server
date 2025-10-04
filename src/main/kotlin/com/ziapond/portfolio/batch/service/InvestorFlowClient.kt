package com.ziapond.portfolio.batch.service

/**
 * @fileoverview
 * @filename InvestorFlowClient.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */

import com.fasterxml.jackson.databind.JsonNode
import com.ziapond.portfolio.common.domain.InvestorFlow
import com.ziapond.portfolio.kis.http.KisHttp
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter

@Service
class InvestorFlowClient(
    private val http: KisHttp,
    @Value("\${kis.investor.tr-id}") private val trId: String,
    @Value("\${kis.investor.path}") private val path: String,
) {
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")
    private val ymdBasic = DateTimeFormatter.BASIC_ISO_DATE // yyyyMMdd

    /**
     * 시장 단위(KOSPI/KOSDAQ 등)의 "시간대별 투자자 동향"을 조회하고,
     * [windowStart, windowEnd) 범위 내 데이터로 30분 버킷 스냅샷을 만든다.
     */
    fun fetchWindowByMarket(
        marketCode: String,
        windowStart: LocalTime,
        windowEnd: LocalTime
    ): List<InvestorFlow> {
        val endStr = "%02d%02d%02d".format(windowEnd.hour, windowEnd.minute, 0)

        val node: JsonNode? = http.getJson(
            path = path,
            query = mapOf(
                "FID_COND_MRKT_DIV_CODE" to marketCode,
                "FID_INPUT_HOUR_1"       to endStr
            ),
            trId = trId,
        )

        val arr = node?.path("output2")
            ?: node?.path("response")?.path("body")?.path("items")?.path("item")

        if (arr == null || arr.isMissingNode || arr.isNull) return emptyList()

        val today = LocalDate.now(KST)
        val nodes = if (arr.isArray) arr.toList() else listOf(arr)

        val grouped: Map<String, List<Tick>> = nodes.mapNotNull { n ->
            // 시간(HHmmss)
            val hhmmss = str(n, "stck_cntg_hour", "stck_bsop_time") ?: return@mapNotNull null
            if (hhmmss.length < 6) return@mapNotNull null
            val t = try {
                LocalTime.of(hhmmss.substring(0,2).toInt(), hhmmss.substring(2,4).toInt(), hhmmss.substring(4,6).toInt())
            } catch (_: Exception) { return@mapNotNull null }
            if (t.isBefore(windowStart) || !t.isBefore(windowEnd)) return@mapNotNull null

            val ymd = str(n, "stck_bsop_date", "bsop_date")
                ?.let { LocalDate.parse(it, ymdBasic) } ?: today

            val typeCode = str(n, "invt_tp_cd", "invest_tp_code", "ivst_tp_code") ?: return@mapNotNull null
            val typeName = str(n, "invt_tp_nm", "invest_tp_name", "ivst_tp_name")

            // 누적 순매수 수량/금액(필드명은 계정/문서에 따라 다를 수 있음)
            val acmlQty = long(n, "acml_ntby_qty", "acml_net_buysell_qty")
            val acmlAmt = bd(n, "acml_ntby_amt", "acml_net_buysell_amt")

            Tick(
                ymd = ymd, time = t,
                investorTypeCode = typeCode, investorTypeName = typeName,
                acmlNetQty = acmlQty, acmlNetAmt = acmlAmt
            )
        }.groupBy { it.investorTypeCode }

        val bucketStart = OffsetDateTime.of(
            LocalDateTime.of(today, windowStart),
            KST.rules.getOffset(Instant.now())
        )

        // 유형별로 정렬 → first/last 차분 → 스냅샷
        val result = mutableListOf<InvestorFlow>()
        grouped.forEach { (typeCode, ticks) ->
            val sorted = ticks.sortedBy { it.time }
            val first = sorted.firstOrNull() ?: return@forEach
            val last  = sorted.lastOrNull()  ?: return@forEach

            val diffQty = safeDiff(last.acmlNetQty, first.acmlNetQty)
            val diffAmt = safeDiff(last.acmlNetAmt, first.acmlNetAmt)

            result += InvestorFlow(
                ymd = last.ymd,
                bucketStart = bucketStart,
                marketCode = marketCode,
                investorTypeCode = typeCode,
                investorTypeName = last.investorTypeName,
                netQty = diffQty,
                netAmt = diffAmt,
                acmlNetQty = last.acmlNetQty,
                acmlNetAmt = last.acmlNetAmt
            )
        }
        return result
    }

    // ---- internal helpers ----
    private data class Tick(
        val ymd: LocalDate,
        val time: LocalTime,
        val investorTypeCode: String,
        val investorTypeName: String?,
        val acmlNetQty: Long?,
        val acmlNetAmt: BigDecimal?
    )

    private fun str(n: JsonNode, vararg keys: String): String? =
        keys.firstNotNullOfOrNull { k ->
            n.path(k).asText().takeIf { it.isNotBlank() }
        }

    private fun long(n: JsonNode, vararg keys: String): Long? =
        keys.firstNotNullOfOrNull { k ->
            n.path(k).asText().takeIf { it.isNotBlank() }?.toLongOrNull()
        }

    private fun bd(n: JsonNode, vararg keys: String): BigDecimal? =
        keys.firstNotNullOfOrNull { k ->
            n.path(k).asText().takeIf { it.isNotBlank() }?.toBigDecimalOrNull()
        }

    private fun safeDiff(a: Long?, b: Long?): Long? =
        if (a != null && b != null) (a - b) else null

    private fun safeDiff(a: BigDecimal?, b: BigDecimal?): BigDecimal? =
        if (a != null && b != null) a.subtract(b) else null
}
