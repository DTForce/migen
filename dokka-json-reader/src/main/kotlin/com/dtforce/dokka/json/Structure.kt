package com.dtforce.dokka.json


data class DokkaDocNode(
    val asText: String
)

data class DokkaJsonModule(
    val name: String,
    val dri: String,
    val documentation: DokkaDocNode?,
    val packages: List<DokkaJsonPackage>,
)

data class DokkaJsonPackage(
    val name: String,
    val dri: String,
    val documentation: DokkaDocNode?,
    val functions: List<DokkaJsonFunction>,
    val properties: List<DokkaJsonProperty>,
    val classlikes: List<DokkaJsonClasslike>,
)

data class DokkaJsonClasslike(
    val name: String?,
    val dri: String,
    val documentation: DokkaDocNode?,
    val constructors: List<DokkaJsonFunction>,
    val functions: List<DokkaJsonFunction>,
    val properties: List<DokkaJsonProperty>,
    val classlikes: List<DokkaJsonClasslike>,
)

data class DokkaJsonFunction(
    val name: String,
    val dri: String,
    val documentation: DokkaDocNode?,
    val isConstructor: Boolean,
    val parameters: List<DokkaJsonParameter>,
)

data class DokkaJsonProperty(
    val name: String,
    val dri: String,
    val documentation: DokkaDocNode?,
    val setter: DokkaJsonFunction?,
    val getter: DokkaJsonFunction?,
)

data class DokkaJsonParameter(
    val name: String?,
    val dri: String,
    val documentation: DokkaDocNode?
)
