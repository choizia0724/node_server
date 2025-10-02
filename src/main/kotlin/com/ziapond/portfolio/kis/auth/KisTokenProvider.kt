package com.ziapond.portfolio.kis.auth

interface KisTokenProvider {
    fun accessToken():String

    fun invalidate()

}