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
import com.ziapond.portfolio.common.mappers.InvestorFlowMapper
import com.ziapond.portfolio.common.mappers.StockListMapper
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
    private val investorFlowMapper: InvestorFlowMapper,
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
        symbol: String
    ): List<InvestorFlow> {

        val node: JsonNode? = http.getJson(
            path = path,
            query = mapOf(
                "FID_COND_MRKT_DIV_CODE" to marketCode,
                "FID_INPUT_ISCD"       to symbol
            ),
            trId = trId,
        )

        val arr = node?.path("output")
            ?: node?.path("response")?.path("body")?.path("items")?.path("item")

        if (arr == null || arr.isMissingNode || arr.isNull) return emptyList()

        val nodes = if (arr.isArray) arr.toList() else listOf(arr)

        val rows = nodes.mapNotNull { it.toInvestorFlow(symbol) }
            .filter { it.stck_bsop_date.isNotBlank() }

        return rows
    }
    private fun JsonNode.textOf(vararg keys: String, default: String = ""): String {
        for (k in keys) {
            val v = this.path(k)
            if (!v.isMissingNode && !v.isNull) return v.asText().trim()
        }
        return default
    }

    private fun JsonNode.toInvestorFlow(symbol: String): InvestorFlow? {
        // 필수키 체크: 영업일자 없으면 스킵
        val date = textOf("stck_bsop_date", "basDt") // 일부 API는 basDt로 올 때가 있어 대비
        if (date.isEmpty()) return null

        return InvestorFlow(
            symbol = symbol,

            stck_bsop_date     = date,
            stck_clpr          = textOf("stck_clpr"),
            prdy_vrss          = textOf("prdy_vrss"),
            prdy_vrss_sign     = textOf("prdy_vrss_sign"),

            prsn_ntby_qty      = textOf("prsn_ntby_qty"),
            frgn_ntby_qty      = textOf("frgn_ntby_qty"),
            orgn_ntby_qty      = textOf("orgn_ntby_qty"),

            prsn_ntby_tr_pbmn  = textOf("prsn_ntby_tr_pbmn"),
            frgn_ntby_tr_pbmn  = textOf("frgn_ntby_tr_pbmn"),
            orgn_ntby_tr_pbmn  = textOf("orgn_ntby_tr_pbmn"),

            prsn_shnu_vol      = textOf("prsn_shnu_vol"),
            frgn_shnu_vol      = textOf("frgn_shnu_vol"),
            orgn_shnu_vol      = textOf("orgn_shnu_vol"),

            prsn_shnu_tr_pbmn  = textOf("prsn_shnu_tr_pbmn"),
            frgn_shnu_tr_pbmn  = textOf("frgn_shnu_tr_pbmn"),
            orgn_shnu_tr_pbmn  = textOf("orgn_shnu_tr_pbmn"),

            prsn_seln_vol      = textOf("prsn_seln_vol"),
            frgn_seln_vol      = textOf("frgn_seln_vol"),
            orgn_seln_vol      = textOf("orgn_seln_vol"),

            prsn_seln_tr_pbmn  = textOf("prsn_seln_tr_pbmn"),
            frgn_seln_tr_pbmn  = textOf("frgn_seln_tr_pbmn"),
            orgn_seln_tr_pbmn  = textOf("orgn_seln_tr_pbmn")
        )
    }

}
