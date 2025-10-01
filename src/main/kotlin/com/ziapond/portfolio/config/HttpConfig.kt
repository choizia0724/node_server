package com.ziapond.portfolio.config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestClient
import org.springframework.http.client.JdkClientHttpRequestFactory
import java.net.http.HttpClient
import java.time.Duration
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

/**
 * @fileoverview
 * @filename HttpConfig.kt
 * @author zia
 * @version 1.0.0 - 2025. 10. 1.
 * @copyright 2025,
 */


@Configuration
class HttpConfig {

    @Bean
    fun restClient(
        baseUrl: String
    ): RestClient {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build()

        val factory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(Duration.ofSeconds(15))
        }

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build()
    }
}
