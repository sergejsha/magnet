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

package magnet.processor

import magnet.Instance
import magnet.Magnetizer
import magnet.Scope
import magnet.processor.index.MagnetIndexerGenerator
import magnet.processor.instances.InstanceProcessor
import magnet.processor.scopes.ScopeProcessor
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MagnetProcessor : AbstractProcessor() {

    private val magnetIndexerGenerator = MagnetIndexerGenerator()

    private lateinit var env: MagnetProcessorEnv
    private lateinit var scopeProcessor: ScopeProcessor
    private lateinit var instanceProcessor: InstanceProcessor

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        env = MagnetProcessorEnv(processingEnvironment)
        scopeProcessor = ScopeProcessor(env)
        instanceProcessor = InstanceProcessor(env)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        return try {
            val instancesProcessed = instanceProcessor.process(roundEnv)
            val scopesProcessed = scopeProcessor.process(roundEnv)
            val indexCreated = processFactoryIndexAnnotation(env, roundEnv)
            instancesProcessed || scopesProcessed || indexCreated
        } catch (e: Throwable) {
            true
        }
    }

    private fun processFactoryIndexAnnotation(
        env: MagnetProcessorEnv,
        roundEnv: RoundEnvironment
    ): Boolean {
        return magnetIndexerGenerator.generate(
            roundEnv.getElementsAnnotatedWith(Magnetizer::class.java),
            env
        )
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            Instance::class.java.name,
            Scope::class.java.name,
            Magnetizer::class.java.name
        )
    }

}