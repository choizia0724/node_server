package com.ziapond.portfolio.common.mappers

import com.ziapond.portfolio.common.domain.KisToken
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.LocalDate

@Mapper
interface KisTokenMapper {
    fun selectByTypeAndDate(@Param("tokenType") tokenType: Short,
                            @Param("dateKey") dateKey: LocalDate): KisToken?

    fun upsert(k: KisToken): Int

    fun deleteExpired(@Param("now") now: java.time.OffsetDateTime): Int
}
