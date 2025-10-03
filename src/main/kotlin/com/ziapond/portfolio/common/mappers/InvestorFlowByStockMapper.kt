package com.ziapond.portfolio.common.mappers

import com.ziapond.portfolio.project.web.dto.InvestorFlowRow
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.LocalDate

@Mapper
interface InvestorFlowByStockMapper {
    fun searchStockFlows(
        @Param("symbol") symbol: String,
        @Param("investorType") investorType: String?,
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): List<InvestorFlowRow>

    fun countStockFlows(
        @Param("symbol") symbol: String,
        @Param("investorType") investorType: String?,
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?
    ): Long
}
