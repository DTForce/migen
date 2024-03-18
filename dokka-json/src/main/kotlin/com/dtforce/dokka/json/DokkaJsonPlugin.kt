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

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.Extension
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement
import org.jetbrains.dokka.renderers.Renderer

class DokkaJsonPlugin : DokkaPlugin() {

    private val dokkaBase by lazy { plugin<DokkaBase>() }

    public val renderer: Extension<Renderer, *, *> by extending {
        CoreExtensions.renderer providing {
            DokkaJsonRenderer(it)
        } override dokkaBase.htmlRenderer
    }

    @DokkaPluginApiPreview
    override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement
}
