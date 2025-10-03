package com.ziapond.portfolio.project.web

import com.ziapond.portfolio.common.mappers.InvestorFlowByStockMapper
import com.ziapond.portfolio.common.mappers.InvestorFlowMapper
import com.ziapond.portfolio.project.web.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.math.ceil

/**
 * @fileoverview
 * @filename StockInvestorController.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */


@RestController
@RequestMapping("/api/investor")
class StockInvestorController(
    private val marketMapper: InvestorFlowMapper,
    private val stockMapper: InvestorFlowByStockMapper
) {
    @PostMapping("/flow/search")
    fun search(@RequestBody @Valid req: InvestorFlowSearchRequest): ResponseEntity<PageResponse<InvestorFlowRow>> {
        val page  = req.page.coerceAtLeast(1)
        val limit = req.limit.coerceIn(1, 1000)
        val offset = (page - 1) * limit

        val (total, rows) =
            if (!req.symbol.isNullOrBlank()) {
                val t = stockMapper.countStockFlows(
                    symbol = req.symbol,
                    investorType = req.investorType,
                    from = req.from, to = req.to
                )
                val r = if (t > 0)
                    stockMapper.searchStockFlows(
                        symbol = req.symbol,
                        investorType = req.investorType,
                        from = req.from, to = req.to,
                        limit = limit, offset = offset
                    ) else emptyList()
                t to r
            } else {
                val t = marketMapper.countMarketFlows(
                    marketCode = req.marketCode,
                    investorType = req.investorType,
                    from = req.from, to = req.to
                )
                val r = if (t > 0)
                    marketMapper.searchMarketFlows(
                        marketCode = req.marketCode,
                        investorType = req.investorType,
                        from = req.from, to = req.to,
                        limit = limit, offset = offset
                    ) else emptyList()
                t to r
            }

        val totalPages = if (total == 0L) 0 else ceil(total.toDouble() / limit).toInt()
        return ResponseEntity.ok(
            PageResponse(
                data = rows,
                pagination = Pagination(totalItems = total, currentPage = page, totalPages = totalPages, limit = limit)
            )
        )
    }
}
