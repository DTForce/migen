/*
 *    Copyright 2024 Jan Mareš, DTForce s.r.o.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dtforce.dokka.json

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.addSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaDefaults
import org.jetbrains.dokka.DokkaException
import org.jetbrains.dokka.DokkaSourceSetID
import org.jetbrains.dokka.DokkaSourceSetImpl
import org.jetbrains.dokka.ExternalDocumentationLinkImpl
import org.jetbrains.dokka.PackageOptionsImpl
import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.SourceLinkDefinitionImpl
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.renderers.OutputWriter
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.Callable
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.DocumentableSource
import org.jetbrains.dokka.model.IsVar
import org.jetbrains.dokka.model.Modifier
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.Visibility
import org.jetbrains.dokka.model.WithGenerics
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.properties.ExtraProperty
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import org.jetbrains.dokka.transformers.pages.PageTransformer
import java.io.File

class DokkaJsonRenderer(private val context: DokkaContext) : Renderer {

    protected val outputWriter: OutputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }

    protected open val preprocessors: Iterable<PageTransformer> = emptyList()

    override fun render(root: RootPageNode) {
        val newRoot = preprocessors.fold(root) { acc, t -> t(acc) }

        val module = SimpleModule();
        module.setMixInAnnotation(IsVar::class.java, IsVarMixin::class.java)
            .setMixInAnnotation(DProperty::class.java, DPropertyMixin::class.java)
            .setMixInAnnotation(DokkaSourceSetImpl::class.java, DokkaSourceSetImplMixin::class.java)
            .addKeySerializer(DokkaConfiguration.DokkaSourceSet::class.java, object : JsonSerializer<DokkaConfiguration.DokkaSourceSet>() {
                override fun serialize(value: DokkaConfiguration.DokkaSourceSet, gen: JsonGenerator, serializers: SerializerProvider?) {
                    gen.writeFieldName(value.sourceSetID.toString())
                }
            })
            .addKeySerializer(ExtraProperty.Key::class.java, object : JsonSerializer<ExtraProperty.Key<*, *>>() {
                override fun serialize(value: ExtraProperty.Key<*, *>, gen: JsonGenerator, serializers: SerializerProvider?) {
                    gen.writeFieldName(value::class.java.name)
                }
            })
            .addSerializer(DokkaConfiguration.DokkaSourceSet::class.java, object : JsonSerializer<DokkaConfiguration.DokkaSourceSet>() {
                override fun serialize(value: DokkaConfiguration.DokkaSourceSet, gen: JsonGenerator, serializers: SerializerProvider?) {
                    gen.writeString(value.sourceSetID.toString())
                }
            })
            .addSerializer(DRI::class.java, object : JsonSerializer<DRI>() {
                override fun serialize(value: DRI, gen: JsonGenerator, serializers: SerializerProvider?) {
                    gen.writeString(value.toString())
                }
            })
            .addSerializer(DocumentableSource::class.java, object : JsonSerializer<DocumentableSource>() {
                override fun serialize(value: DocumentableSource, gen: JsonGenerator, serializers: SerializerProvider) {
                    gen.writeString(value.path)
                }
            })

        val objectMapper = ObjectMapper()
            .registerModules(KotlinModule.Builder().build(), module)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

        val locationProvider =
            context.plugin<DokkaBase>().querySingle { locationProviderFactory }.getLocationProvider(newRoot)

        val path by lazy {
            locationProvider.resolve(newRoot, skipExtension = true)
                ?: throw DokkaException("Cannot resolve path for ${newRoot.name}")
        }
        if (root is ModulePageNode) {
            runBlocking(Dispatchers.Default) {
                outputWriter.write(path, objectMapper.writeValueAsString(root.documentables), ".json")
            }
        }
    }
}

object IsVarMixin : ExtraProperty<DProperty>, ExtraProperty.Key<DProperty, IsVar> {
    @JsonIgnore
    override val key: ExtraProperty.Key<DProperty, *> = this
}


public data class DPropertyMixin(
    override val dri: DRI,
    override val name: String,
    override val documentation: SourceSetDependent<DocumentationNode>,
    override val expectPresentInSet: DokkaConfiguration.DokkaSourceSet?,
    override val sources: SourceSetDependent<DocumentableSource>,
    override val visibility: SourceSetDependent<Visibility>,
    override val type: Bound,
    override val receiver: DParameter?,
    val setter: DFunction?,
    val getter: DFunction?,
    override val modifier: SourceSetDependent<Modifier>,
    override val sourceSets: Set<DokkaConfiguration.DokkaSourceSet>,
    override val generics: List<DTypeParameter>,
    override val isExpectActual: Boolean,
    @JsonIgnore
    override val extra: PropertyContainer<DPropertyMixin> = PropertyContainer.empty()
) : Documentable(), Callable, WithExtraProperties<DPropertyMixin>, WithGenerics {
    override val children: List<Nothing>
        get() = emptyList()

    override fun withNewExtras(newExtras: PropertyContainer<DPropertyMixin>): DPropertyMixin = copy(extra = newExtras)
}

data class DokkaSourceSetImplMixin (
    override val displayName: String = DokkaDefaults.sourceSetDisplayName,
    override val sourceSetID: DokkaSourceSetID,
    @JsonIgnore
    override val classpath: List<File> = emptyList(),
    override val sourceRoots: Set<File> = emptySet(),
    override val dependentSourceSets: Set<DokkaSourceSetID> = emptySet(),
    override val samples: Set<File> = emptySet(),
    override val includes: Set<File> = emptySet(),
    @Deprecated("Use [documentedVisibilities] property for a more flexible control over documented visibilities")
    override val includeNonPublic: Boolean = DokkaDefaults.includeNonPublic,
    override val reportUndocumented: Boolean = DokkaDefaults.reportUndocumented,
    override val skipEmptyPackages: Boolean = DokkaDefaults.skipEmptyPackages,
    override val skipDeprecated: Boolean = DokkaDefaults.skipDeprecated,
    override val jdkVersion: Int = DokkaDefaults.jdkVersion,
    override val sourceLinks: Set<SourceLinkDefinitionImpl> = mutableSetOf(),
    override val perPackageOptions: List<PackageOptionsImpl> = mutableListOf(),
    override val externalDocumentationLinks: Set<ExternalDocumentationLinkImpl> = mutableSetOf(),
    override val languageVersion: String? = null,
    override val apiVersion: String? = null,
    override val noStdlibLink: Boolean = DokkaDefaults.noStdlibLink,
    override val noJdkLink: Boolean = DokkaDefaults.noJdkLink,
    override val suppressedFiles: Set<File> = emptySet(),
    override val analysisPlatform: Platform = DokkaDefaults.analysisPlatform,
    override val documentedVisibilities: Set<DokkaConfiguration.Visibility> = DokkaDefaults.documentedVisibilities
) : DokkaConfiguration.DokkaSourceSet
