package com.ziapond.portfolio.batch.web

import com.ziapond.portfolio.batch.service.DailyMinuteCollector
import com.ziapond.portfolio.batch.service.InvestorFlowClient
import com.ziapond.portfolio.batch.service.StockItemInfo
import com.ziapond.portfolio.batch.web.dto.SyncDailyMinuteRequest
import com.ziapond.portfolio.batch.web.dto.SyncDailyMinuteResponse
import com.ziapond.portfolio.common.domain.InvestorFlow
import com.ziapond.portfolio.common.mappers.InvestorFlowMapper
import com.ziapond.portfolio.utils.LastWeekMonday
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @fileoverview
 * @filename StockController.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */

@RestController
@RequestMapping("/api/sync")
class InvestorFlowBatchController(
    private val service: InvestorFlowClient,
    private val stockItemInfo: StockItemInfo,
    private val investorFlowMapper: InvestorFlowMapper


) {

    /**
     * KRX 상장종목 기본정보 동기화 트리거
     * 예) POST /api/sync/krx/investor?beginBasDt=20250923
     * beginBasDt 미지정 시: 지난주 월요일 기준
     */

    @PostMapping("/krx/investor")
    fun syncListed(
        @RequestParam(required = false) divCode: String,
        @RequestParam(required = false) symbol: String
    ): ResponseEntity<Map<String, Any>> {
        val kospiSymbols = stockItemInfo.getSymbolsFromDb(null,null, null, true)
            .map { it.symbol }
            .distinct()

        val mkt="J"

        val batch = mutableListOf<InvestorFlow>()
        for (sym in kospiSymbols) {
            val rows = service.fetchWindowByMarket(mkt, sym)
            batch += rows
            if (rows.isNotEmpty()) investorFlowMapper.upsertInvestorFlows(rows)
        }


        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "divCode" to divCode,
                "symbol" to symbol,
                "batch" to batch,
            )
        )
    }
}
