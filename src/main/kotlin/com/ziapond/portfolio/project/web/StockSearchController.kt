package com.ziapond.portfolio.project.web


import com.ziapond.portfolio.project.domain.StockTable
import com.ziapond.portfolio.project.web.dto.PageResponse
import com.ziapond.portfolio.project.web.dto.Pagination
import com.ziapond.portfolio.project.mappers.StockListMapper
import com.ziapond.portfolio.project.web.dto.StockSearchRequest
import jakarta.validation.Valid
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
    private val stockMapper: StockListMapper
) {
    @PostMapping("/search")
    fun search(@RequestBody @Valid req: StockSearchRequest): ResponseEntity<PageResponse<StockTable>> {
        val page  = req.page.coerceAtLeast(1)
        val limit = req.limit.coerceIn(1, 5000)
        val offset = (page - 1) * limit

        val total: Long = stockMapper.countStocks(
            symbol = req.symbol,
            name = req.name,
            mrktctg = req.mrktctg,
            from = req.from,
            to = req.to
        )

        val rows: List<StockTable> = if (total > 0)
            stockMapper.searchStocks(
                symbol = req.symbol,
                name = req.name,
                mrktctg = req.mrktctg,
                from = req.from,
                to = req.to,
                limit = limit,
                offset = offset
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
}
