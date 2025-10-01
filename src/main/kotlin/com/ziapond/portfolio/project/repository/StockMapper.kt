package com.ziapond.portfolio.project.repository

import com.ziapond.portfolio.project.domain.StockTable
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 * @fileoverview
 * @path /StockMapper.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */
@Mapper
interface StockMapper {
    fun upsertStock(@Param("r") row:StockTable) : Int
    fun upsertStocks(@Param("list") rows: List<StockTable>) : Int
}