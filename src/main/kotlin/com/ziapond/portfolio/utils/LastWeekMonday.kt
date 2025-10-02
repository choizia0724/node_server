package com.ziapond.portfolio.utils

import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Component
class LastWeekMonday{
    fun lastWeekMonday(): LocalDate =
        LocalDate.now(ZoneId.of("Asia/Seoul")).minusWeeks(1).with(DayOfWeek.MONDAY)
}