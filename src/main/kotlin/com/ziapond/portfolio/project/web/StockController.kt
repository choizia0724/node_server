package com.ziapond.portfolio.project.web

import com.ziapond.portfolio.project.domain.StockTable
import com.ziapond.portfolio.project.repository.StockMapper
import com.ziapond.portfolio.project.service.StockItemInfo
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
@RequestMapping("/api")
class StockController(
    private val service: StockItemInfo,
    private val stockMapper: StockMapper
) {
    private val yyyymmdd = DateTimeFormatter.BASIC_ISO_DATE // "yyyyMMdd"

    /**
     * KRX 상장종목 기본정보 동기화 트리거
     * 예) POST /api/krx/listed/sync?beginBasDt=20250923&numOfRows=3000
     * beginBasDt 미지정 시: 지난주 월요일 기준
     */

    @PostMapping("/krx/listed/sync")
    fun syncListed(
        @RequestParam(required = false) beginBasDt: String?,
        @RequestParam(defaultValue = "3000") numOfRows: Int
    ): ResponseEntity<Map<String, Any>> {
        val date = beginBasDt?.let { LocalDate.parse(it, yyyymmdd) } ?: lastWeekMonday()
        service.fetchAndUpsert(date, numOfRows)
        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "beginBasDt" to date.toString(),
                "numOfRows" to numOfRows
            )
        )
    }

    /** web */

//    /**
//     * 종목별 일자 구간 조회
//     * 예) GET /api/stocks/005930?from=2025-09-01&to=2025-10-01
//     * from/to 미지정 시: 최근 30일
//     */
//
//    @GetMapping("/stocks/{symbol}")
//    fun getStocks(
//        @PathVariable symbol: String,
//        @RequestParam(required = false) from: String?,
//        @RequestParam(required = false) to: String?
//    ): List<StockTable> {
//        val end = to?.let { LocalDate.parse(it) } ?: LocalDate.now(ZoneId.of("Asia/Seoul"))
//        val start = from?.let { LocalDate.parse(it) } ?: end.minusDays(30)
//        return stockMapper.findBySymbolAndRange(symbol, start, end)
//    }

    private fun lastWeekMonday(): LocalDate =
        LocalDate.now(ZoneId.of("Asia/Seoul")).minusWeeks(1).with(java.time.DayOfWeek.MONDAY)
}
