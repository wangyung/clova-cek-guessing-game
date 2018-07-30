package com.linecorp.clova.extension.guessing.routes

import com.linecorp.clova.extension.guessing.handler.DefaultHandler
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class AppRoutes(private val handler: DefaultHandler) {

    @Bean
    fun default() = router {
        (accept(MediaType.APPLICATION_JSON) and "/").nest {
            //For healthy check of nucleo
            GET("/", handler::healthCheck)
            POST("/guessing", handler::guessing)
        }
    }
}
