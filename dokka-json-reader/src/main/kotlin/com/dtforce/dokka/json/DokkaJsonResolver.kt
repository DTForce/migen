package com.dtforce.dokka.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object DokkaJsonResolver {

    private val objectMapper = createObjectMapper()

    fun createObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModules(KotlinModule.Builder().build())
    }

    fun read(path: String): DokkaJsonModule {
        return objectMapper.readValue(File(path))
    }

    fun resolveMethod(dokkaJsonModule: DokkaJsonModule, kFunction1: KFunction<*>, kClass: KClass<*>): DokkaJsonFunction? {
        return resolveClass(dokkaJsonModule, kClass)
            ?.let { it.functions.filter { it.name == kFunction1.name }.singleOrNull() }
    }

    fun resolveMethod(dokkaJsonModule: DokkaJsonModule, kFunction1: Method, kClass: Class<*>): DokkaJsonFunction? {
        return resolveClass(dokkaJsonModule, kClass)
            ?.let { it.functions.filter { it.name == kFunction1.name }.singleOrNull() }
    }

    fun resolveClass(dokkaJsonModule: DokkaJsonModule, kClass: KClass<*>): DokkaJsonClasslike? {
        return dokkaJsonModule.packages.singleOrNull { it.name == kClass.java.packageName }
            ?.classlikes
            ?.singleOrNull { it.name == kClass.simpleName }
    }

    fun resolveClass(dokkaJsonModule: DokkaJsonModule, kClass: Class<*>): DokkaJsonClasslike? {
        return dokkaJsonModule.packages.singleOrNull { it.name == kClass.packageName }
            ?.classlikes
            ?.singleOrNull { it.name == kClass.simpleName }
    }

    fun resolveProperty(dokkaJsonModule: DokkaJsonModule, kClass: Class<*> , name: String): DokkaJsonProperty? {
        return resolveClass(dokkaJsonModule, kClass)
            ?.let { it.properties.filter { it.name == name }.singleOrNull() }
    }
}
