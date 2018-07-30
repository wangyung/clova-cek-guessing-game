package com.linecorp.clova.extension.guessing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GuessingGameApplication

fun main(args: Array<String>) {
    runApplication<GuessingGameApplication>(*args)
}
