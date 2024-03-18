package com.dtforce.dokka.json

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DokkaDocLink::class, name = "link"),
    JsonSubTypes.Type(value = DokkaDocCodeInline::class, name = "code"),
    JsonSubTypes.Type(value = DokkaDocText::class, name = "text"),
)
interface DokkaDocPart {
    val text: String
}

data class DokkaDocText(
    override val text: String
) : DokkaDocPart

data class DokkaDocLink(
    val dri: String,
    override val text: String
) : DokkaDocPart

data class DokkaDocCodeInline(
    override val text: String
) : DokkaDocPart

data class DokkaDocParagraph(
    val docParts: List<DokkaDocPart>
)

data class DokkaDocNode(
    val asText: String,
    val paragraphs: List<DokkaDocParagraph>
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
