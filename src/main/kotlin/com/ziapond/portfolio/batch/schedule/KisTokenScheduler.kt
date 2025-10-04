package com.ziapond.portfolio.batch.schedule


import com.ziapond.portfolio.kis.auth.KisTokenProvider
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * @fileoverview
 * @filename KisTokenScheduler.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 5.
 * @copyright 2025,
 */

@Component
@EnableScheduling
class KisTokenScheduler(
    private val provider: KisTokenProvider
) {
    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    fun prefetchDailyTokens() {
        provider.refreshAndGetRest()
        provider.refreshAndGetWs()
    }
}
