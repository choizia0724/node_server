package com.ziapond.portfolio.kis.auth

interface KisTokenProvider {
    fun accessToken():String
    fun invalidate()

    fun getRestToken(): String
    fun getWsToken(): String
    fun refreshAndGetRest(): String
    fun refreshAndGetWs(): String

}