package com.dtforce.migen.test.mock3.spring

import com.dtforce.migen.intergrations.spring.EnableMigrationGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(Mock2KotlinConfiguration::class)
@EntityScan("com.dtforce.migen.test.mock3")
@EnableMigrationGenerator
open class Mock2Kotlin {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Mock2Kotlin::class.java)
        }
    }
}
