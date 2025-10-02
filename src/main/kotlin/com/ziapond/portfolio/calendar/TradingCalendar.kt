package com.ziapond.portfolio.calendar

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate

@Component
class TradingCalendar(
    @Value("\${market.holidays:}") holidaysCsv: String
) {
    private val holidays: Set<LocalDate> = holidaysCsv
        .split(',', ' ', '\n', '\t')
        .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.let(LocalDate::parse) }
        .toSet()

    fun isTradingDay(date: LocalDate): Boolean {
        val dow = date.dayOfWeek
        val isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
        return !isWeekend && !holidays.contains(date)
    }
}
