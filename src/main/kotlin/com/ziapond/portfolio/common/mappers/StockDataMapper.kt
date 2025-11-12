package com.ziapond.portfolio.common.mappers

import com.ziapond.portfolio.common.domain.StockData
import com.ziapond.portfolio.common.domain.StockDataResponse
import com.ziapond.portfolio.project.web.dto.StockSearchRequest
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface StockDataMapper {
    fun getStockData (@Param("req") req: StockSearchRequest): List<StockData>
    fun upsertAll(@Param("list") rows: List<StockDataResponse>): Int
}