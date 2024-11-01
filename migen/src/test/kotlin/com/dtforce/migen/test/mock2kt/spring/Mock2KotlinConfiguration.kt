package com.dtforce.migen.test.mock2kt.spring

import com.dtforce.dokka.json.DokkaJsonModule
import com.dtforce.dokka.json.DokkaJsonResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class Mock2KotlinConfiguration {
    @Bean
    open fun dokkaJsonModule(): DokkaJsonModule {
        return DokkaJsonResolver.read("build/dokka/html/index.json")
    }
}
