package com.ziapond.portfolio.project.web

import com.ziapond.portfolio.project.domain.StockTable
import com.ziapond.portfolio.project.mappers.StockListMapper
import com.ziapond.portfolio.project.service.StockItemInfo
import com.ziapond.portfolio.utils.LastWeekMonday
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
    private val stockMapper: StockListMapper
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
        val date = beginBasDt?.let { LocalDate.parse(it, yyyymmdd) } ?: LastWeekMonday().lastWeekMonday()
        service.fetchAndUpsert(date, numOfRows)
        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "beginBasDt" to date.toString(),
                "numOfRows" to numOfRows
            )
        )
    }

}
