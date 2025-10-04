package com.ziapond.portfolio.project.web

import com.ziapond.portfolio.kis.auth.KisTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @fileoverview
 * @filename StockTokenController.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 5.
 * @copyright 2025,
 */

@RestController
@RequestMapping("/api/token")
class StockTokenController (
    private val tokenProvider: KisTokenProvider
){

    @GetMapping("/rest")
    fun getRestToken(): ResponseEntity<Map<String, String>> {
        val restToken:String = tokenProvider.refreshAndGetRest()
        return ResponseEntity.ok(mapOf("access_token" to restToken))
    }

    @GetMapping("/ws")
    fun getWsToken(): ResponseEntity<Map<String, String>> {
        val wsToken = tokenProvider.refreshAndGetWs()
        return ResponseEntity.ok(mapOf("access_token" to wsToken))
    }

}