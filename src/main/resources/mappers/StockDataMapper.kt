package mappers

import com.ziapond.portfolio.common.domain.StockData
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface StockDataMapper {
    fun getStockData ()
    fun upsertAll(@Param("list") rows: List<StockData>): Int
}