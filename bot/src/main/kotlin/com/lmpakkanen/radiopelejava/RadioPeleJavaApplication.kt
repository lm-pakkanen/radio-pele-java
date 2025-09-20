package com.lmpakkanen.radiopelejava

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class RadioPeleJavaApplication

fun main(args: Array<String>) {
    SpringApplication.run(RadioPeleJavaApplication::class.java, *args)
}
