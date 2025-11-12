package com.ziapond.portfolio.project.web


import com.ziapond.portfolio.common.domain.StockData
import com.ziapond.portfolio.common.domain.StockDataResponse
import com.ziapond.portfolio.common.domain.StockTable
import com.ziapond.portfolio.common.domain.toResponse
import com.ziapond.portfolio.common.mappers.StockDataMapper
import com.ziapond.portfolio.common.mappers.StockListMapper
import com.ziapond.portfolio.project.web.dto.PageResponse
import com.ziapond.portfolio.project.web.dto.Pagination
import com.ziapond.portfolio.project.web.dto.StockDataRequest
import com.ziapond.portfolio.project.web.dto.StockSearchRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.math.ceil

/**
 * @fileoverview
 * @filename StockSearchController.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */

@RestController
@RequestMapping("/api/stocks")
class StockSearchController(
    private val stockListMapper: StockListMapper,
    private val stockDataMapper: StockDataMapper,
    @Value("\${batch.investor.markets}") val marketsCsv: String,
) {

    @PostMapping("/search")
    fun search(@RequestBody @Valid req: StockSearchRequest): ResponseEntity<PageResponse<StockTable>> {
        val page  = req.page.coerceAtLeast(1)
        val limit = req.limit.coerceIn(1, 5000)
        val offset = page * limit

        val total: Long = stockListMapper.countStocks(
            symbol = req.symbol,
            name = req.name,
            mrktctg = req.mrktctg,
            useornot = req.useornot
        )

        val rows: List<StockTable> = if (total > 0)
            stockListMapper.searchStocksPaging(
                symbol = req.symbol,
                name = req.name,
                mrktctg = req.mrktctg,
                limit = limit,
                offset = offset,
                useornot = req.useornot,
            )
        else emptyList()

        val totalPages = if (total == 0L) 0 else ceil(total.toDouble() / limit).toInt()

        return ResponseEntity.ok(
            PageResponse(
                data = rows,
                pagination = Pagination(
                    totalItems = total,
                    currentPage = page,
                    totalPages = totalPages,
                    limit = limit
                )
            )
        )
    }

    @PostMapping("/search/{code}")
    fun searchDetail(@RequestBody @Valid req: StockSearchRequest,
                     @PathVariable code: String ): ResponseEntity<StockDataResponse> {
        val rows: List<StockData> = stockDataMapper.getStockData(req.copy(symbol = code))
        println(rows)
        val body: StockDataResponse = rows.toResponse(code)

        println(body)

        return ResponseEntity.ok(body)
    }
}
