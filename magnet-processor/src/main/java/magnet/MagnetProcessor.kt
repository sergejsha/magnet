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

package magnet

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MagnetProcessor : AbstractProcessor() {

    private val factoryGenerator: FactoryGenerator = FactoryGenerator()
    private val factoryIndexGenerator: FactoryIndexGenerator = FactoryIndexGenerator()
    private val magnetIndexerGenerator: MagnetIndexerGenerator = MagnetIndexerGenerator()

    private lateinit var processEnvironment: ProcessingEnvironment
    private var round = 0

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        this.processEnvironment = processingEnvironment
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {

        val env = MagnetProcessorEnv(processEnvironment)

        return try {
            val extensionsProcessed = processExtensionAnnotation(env, roundEnv)
            val registryCreated = processExtensionRegistryAnnotation(env, roundEnv)

            extensionsProcessed || registryCreated
        } catch (e: BreakGenerationException) {
            true
        }
    }

    private fun processExtensionAnnotation(
        env: MagnetProcessorEnv,
        roundEnv: RoundEnvironment
    ): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(Implementation::class.java)
        if (annotatedElements.isEmpty()) {
            return false
        }

        var processed = false
        val annotatedTypes = ElementFilter.typesIn(annotatedElements)
        annotatedTypes.forEach {
            factoryGenerator.generate(it, env)
            factoryIndexGenerator.generate(it, env)
            processed = true
        }

        return processed
    }

    private fun processExtensionRegistryAnnotation(
        env: MagnetProcessorEnv,
        roundEnv: RoundEnvironment
    ): Boolean {
        return magnetIndexerGenerator.generate(
            roundEnv.getElementsAnnotatedWith(Magnetizer::class.java),
            env
        )
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf<String>(
            Implementation::class.java.name,
            Magnetizer::class.java.name
        )
    }

}