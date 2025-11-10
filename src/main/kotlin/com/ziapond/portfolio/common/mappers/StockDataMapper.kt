package com.ziapond.portfolio.common.mappers

import com.ziapond.portfolio.common.domain.StockDataResponse
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface StockDataMapper {
    fun getStockData ()
    fun upsertAll(@Param("list") rows: List<StockDataResponse>): Int
}