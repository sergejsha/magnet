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
import magnet.processor.instances.InstanceProcessor
import magnet.processor.registry.RegistryProcessor
import magnet.processor.scopes.ScopeProcessor
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MagnetProcessor : AbstractProcessor() {

    private lateinit var env: MagnetProcessorEnv
    private lateinit var scopeProcessor: ScopeProcessor
    private lateinit var instanceProcessor: InstanceProcessor
    private lateinit var registryProcessor: RegistryProcessor

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        env = MagnetProcessorEnv(processingEnvironment)
        scopeProcessor = ScopeProcessor(env)
        instanceProcessor = InstanceProcessor(env)
        registryProcessor = RegistryProcessor(env)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        return try {
            val instancesProcessed = instanceProcessor.process(roundEnv)
            val scopesProcessed = scopeProcessor.process(roundEnv)
            val registryProcessed = registryProcessor.process(roundEnv)
            instancesProcessed || scopesProcessed || registryProcessed
        } catch (e: Throwable) {
            true
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            Instance::class.java.name,
            Scope::class.java.name,
            Magnetizer::class.java.name
        )
    }

}

class MagnetProcessorEnv(
    private val processEnvironment: ProcessingEnvironment
) {

    val filer: Filer get() = processEnvironment.filer
    val elements: Elements get() = processEnvironment.elementUtils
    val types: Types get() = processEnvironment.typeUtils

    fun compilationError(element: Element, message: String, cause: Throwable? = null): Throwable {
        processEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, message, element)
        return cause ?: CompilationError(element, message)
    }

    fun unexpectedCompilationError(element: Element, message: String? = null, cause: Throwable? = null): Throwable {
        processEnvironment.messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Unexpected compilation error, please report the bug. Message: ${message ?: "none."}",
            element
        )
        return cause ?: CompilationInterruptionError()
    }

}