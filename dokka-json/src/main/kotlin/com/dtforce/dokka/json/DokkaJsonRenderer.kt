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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.DokkaException
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.renderers.OutputWriter
import org.jetbrains.dokka.model.DClass
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.asPrintableTree
import org.jetbrains.dokka.model.dfs
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import org.jetbrains.dokka.transformers.pages.PageTransformer

class DokkaJsonRenderer(private val context: DokkaContext) : Renderer {

    protected val outputWriter: OutputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }

    protected val preprocessors: Iterable<PageTransformer> = emptyList()

    private val objectMapper = DokkaJsonResolver.createObjectMapper()

    override fun render(root: RootPageNode) {
        val newRoot = preprocessors.fold(root) { acc, t -> t(acc) }

        val locationProvider =
            context.plugin<DokkaBase>().querySingle { locationProviderFactory }.getLocationProvider(newRoot)

        val path by lazy {
            locationProvider.resolve(newRoot, skipExtension = true)
                ?: throw DokkaException("Cannot resolve path for ${newRoot.name}")
        }
        if (root is ModulePageNode) {
            runBlocking(Dispatchers.Default) {
                val dModule = root.documentables.get(0) as DModule
                val moduleDocs = convertModule(dModule)
                outputWriter.write(path, objectMapper.writeValueAsString(moduleDocs), ".json")
            }
        }
    }

    private fun convertModule(dModule: DModule): DokkaJsonModule {
        return DokkaJsonModule(
            name = dModule.name,
            dri = dModule.dri.toString(),
            documentation = convertMultiDocs(dModule.documentation),
            packages = dModule.packages.map { convertPackage(it) }
        )
    }

    private fun convertPackage(dPackage: DPackage): DokkaJsonPackage {
        return DokkaJsonPackage(
            name = dPackage.name,
            dri = dPackage.dri.toString(),
            documentation = convertMultiDocs(dPackage.documentation),
            functions = dPackage.functions.map { convertFunction(it) },
            classlikes = dPackage.classlikes.map { convertClassLike(it) },
            properties = dPackage.properties.map { convertProperty(it) }
        )
    }

    private fun convertClassLike(dClassLike: DClasslike): DokkaJsonClasslike {
        return DokkaJsonClasslike(
            name = dClassLike.name,
            dri = dClassLike.dri.toString(),
            documentation = convertMultiDocs(dClassLike.documentation),
            functions = dClassLike.functions.map { convertFunction(it) },
            classlikes = dClassLike.classlikes.map { convertClassLike(it) },
            properties = dClassLike.properties.map { convertProperty(it) },
            constructors = listOf()
        )
            .let {
                if (dClassLike is DClass) {
                    it.copy(constructors = dClassLike.constructors.map { convertFunction(it) })
                } else {
                    it
                }
            }
    }

    private fun convertProperty(dProperty: DProperty): DokkaJsonProperty {
        return DokkaJsonProperty(
            name = dProperty.name,
            dri = dProperty.dri.toString(),
            documentation = convertMultiDocs(dProperty.documentation),
            getter = dProperty.getter?.let { convertFunction(it) },
            setter = dProperty.setter?.let { convertFunction(it) }
        )
    }

    private fun convertMultiDocs(sourceSet: SourceSetDependent<DocumentationNode>): DokkaDocNode? {
        return sourceSet.entries.singleOrNull()?.value?.let {
            docNodeToText(it)
        }
    }

    private fun docNodeToText(it: DocumentationNode): DokkaDocNode? {

        val description = it.children.singleOrNull { it is Description } as Description?

        if (description == null) {
            return null
        }

        val docParts: List<DokkaDocParagraph> = description.children
            .filter { it is P }
            .map {
                val parts: List<DokkaDocPart?> = it.children.map {
                    if (it is DocumentationLink) {
                        DokkaDocLink(
                            it.dri.toString(),
                            docTagAsText(it)!!
                        )
                    } else if (it is Text) {
                        docTagAsText(it)?.let { it1 -> DokkaDocText(it1) }
                    } else {
                        null
                    }
                }
                return@map parts.filterNotNull()
            }
            .map { DokkaDocParagraph(it) }

        if (docParts.isEmpty()) {
            return null
        }

        return DokkaDocNode(
            docTagAsText(description)!!,
            docParts
        )
    }

    private fun docTagAsText(it: WithChildren<*>): String? {
        val allText = it.withDescendants()
            .filter { it is Text }
            .map { it as Text }
            .map { it.body }
            .joinToString("\n")

        if (allText.isEmpty()) {
            return null
        }
        return allText
    }

    private fun convertFunction(dFunction: DFunction): DokkaJsonFunction {
        return DokkaJsonFunction(
            name = dFunction.name,
            dri = dFunction.dri.toString(),
            documentation = convertMultiDocs(dFunction.documentation),
            isConstructor = dFunction.isConstructor,
            parameters = dFunction.parameters.map { convertParameter(it) }
        )
    }

    private fun convertParameter(dParameter: DParameter): DokkaJsonParameter {
        return DokkaJsonParameter(
            name = dParameter.name,
            dri = dParameter.dri.toString(),
            documentation = convertMultiDocs(dParameter.documentation),
        )
    }
}
