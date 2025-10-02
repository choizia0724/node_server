package com.ziapond.portfolio.common.mappers

import com.ziapond.portfolio.common.domain.StockTable
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.LocalDate

/**
 * @fileoverview
 * @path /StockMapper.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */
@Mapper
interface StockListMapper {
    fun upsertStock(@Param("r") row: StockTable) : Int
    fun upsertStocks(@Param("list") rows: List<StockTable>) : Int
    fun findBySymbolAndRange(symbol: String, from: LocalDate, to: LocalDate): List<StockTable>

    fun searchStocks(
        @Param("symbol") symbol: String?,
        @Param("name") name: String?,
        @Param("mrktctg") mrktctg: String?,
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): List<StockTable>

     fun countStocks(
        @Param("symbol") symbol: String?,
        @Param("name") name: String?,
        @Param("mrktctg") mrktctg: String?
    ): Long
}
