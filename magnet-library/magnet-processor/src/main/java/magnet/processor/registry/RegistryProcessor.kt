/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package magnet.processor.registry

import magnet.Magnetizer
import magnet.processor.MagnetProcessorEnv
import javax.annotation.processing.RoundEnvironment

const val REGISTRY_CLASS_NAME = "magnet.internal.MagnetIndexer"
const val INDEX_PACKAGE = "magnet.index"

class RegistryProcessor(
    private val env: MagnetProcessorEnv
) {

    private val registryParser by lazy { RegistryParser(env) }
    private val magnetIndexerGenerator by lazy { RegistryGenerator() }

    private var generateRegistryOnNextRound = false

    fun process(roundEnv: RoundEnvironment): Boolean {

        val generatedRegistryElement = env.elements.getTypeElement(REGISTRY_CLASS_NAME)
        if (generatedRegistryElement != null) {
            return false
        }

        val annotatedRegistryElement = roundEnv.getElementsAnnotatedWith(Magnetizer::class.java)
        if (!generateRegistryOnNextRound) {
            generateRegistryOnNextRound = annotatedRegistryElement.isNotEmpty()
            return false
        }

        val packageElement = env.elements.getPackageElement(INDEX_PACKAGE)
        val registry = if (packageElement != null) registryParser.parse(packageElement)
        else Model.Registry(instanceFactories = emptyList())

        magnetIndexerGenerator
            .generate(registry)
            .writeInto(env.filer)

        return true
    }
}