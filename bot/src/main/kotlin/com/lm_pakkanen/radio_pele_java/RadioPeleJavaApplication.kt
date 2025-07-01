package com.lm_pakkanen.radio_pele_java

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class RadioPeleJavaApplication

fun main(args: Array<String>) {
  SpringApplication.run(RadioPeleJavaApplication::class.java, *args)
}
