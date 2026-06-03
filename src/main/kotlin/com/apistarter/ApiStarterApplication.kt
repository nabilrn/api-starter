package com.apistarter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ApiStarterApplication

fun main(args: Array<String>) {
    runApplication<ApiStarterApplication>(*args)
}
