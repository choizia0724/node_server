package mappers

import com.ziapond.portfolio.common.domain.InvestorFlow
import com.ziapond.portfolio.project.web.dto.InvestorFlowRow
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.LocalDate

/**
 * @fileoverview
 * @filename InvestorFlowMapper.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 3.
 * @copyright 2025,
 */

@Mapper
interface InvestorFlowMapper {
    fun upsertAll(@Param("list") rows: List<InvestorFlow>): Int
    fun searchMarketFlows(
        @Param("marketCode") marketCode: String?,
        @Param("investorType") investorType: String?,
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): List<InvestorFlowRow>

    fun countMarketFlows(
        @Param("marketCode") marketCode: String?,
        @Param("investorType") investorType: String?,
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?
    ): Long
}