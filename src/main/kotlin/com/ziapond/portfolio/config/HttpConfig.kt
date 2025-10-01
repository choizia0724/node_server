package com.ziapond.portfolio.config

import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class HttpConfig {

    @Bean
    fun restClientCustomizer(): RestClientCustomizer {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build()

        val factory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(Duration.ofSeconds(15))
        }

        return RestClientCustomizer { builder ->
            builder.requestFactory(factory)
        }
    }
}
