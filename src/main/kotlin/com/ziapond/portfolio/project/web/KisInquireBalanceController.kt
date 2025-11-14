package com.ziapond.portfolio.project.web

import com.ziapond.portfolio.project.service.KisInquireBalanceService
import com.ziapond.portfolio.project.web.dto.KisInquireBalanceRequest
import com.ziapond.portfolio.project.web.dto.KisInquireBalanceResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @fileoverview
 * @filename KisInquireBalanceController.kt
 * @author zia
 * @version 1.0.0 - 2025. 11. 14.
 * @copyright 2025,
 */

@RestController
@RequestMapping("/api/kis")
class KisInquireBalanceController(
    private val kisInquireBalanceService: KisInquireBalanceService
) {

    @GetMapping("/balance")
    fun getBalance(@Valid req: KisInquireBalanceRequest): KisInquireBalanceResponse {
        return kisInquireBalanceService.getBalance(req)
    }
}
