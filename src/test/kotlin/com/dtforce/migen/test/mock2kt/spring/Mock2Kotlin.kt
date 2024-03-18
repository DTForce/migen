package com.dtforce.migen.test.mock2kt.spring

import com.dtforce.migen.intergrations.spring.EnableMigrationGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(Mock2KotlinConfiguration::class)
@EntityScan("com.dtforce.migen.test.mock2kt")
@EnableMigrationGenerator
open class Mock2Kotlin {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Mock2Kotlin::class.java)
        }
    }
}
