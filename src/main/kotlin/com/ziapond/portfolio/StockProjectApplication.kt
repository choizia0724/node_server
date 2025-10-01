package com.ziapond.portfolio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class StockProjectApplication

fun main(args: Array<String>) {
	runApplication<StockProjectApplication>(*args)
}
