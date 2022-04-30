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
import magnet.Registry
import magnet.Scope
import magnet.processor.common.AnnotationValueExtractor
import magnet.processor.common.CompilationException
import magnet.processor.common.ValidationException
import magnet.processor.instances.InstanceProcessor
import magnet.processor.registry.RegistryProcessor
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_11)
class MagnetProcessor : AbstractProcessor() {

    private lateinit var env: MagnetProcessorEnv
    private lateinit var instanceProcessor: InstanceProcessor
    private lateinit var registryProcessor: RegistryProcessor

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        env = MagnetProcessorEnv(processingEnvironment)
        instanceProcessor = InstanceProcessor(env)
        registryProcessor = RegistryProcessor(env)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        return try {
            val instancesProcessed = instanceProcessor.process(roundEnv)
            val registryProcessed = registryProcessor.process(roundEnv)
            instancesProcessed || registryProcessed
        } catch (e: ValidationException) {
            env.reportError(e)
            false
        } catch (e: CompilationException) {
            env.reportError(e)
            false
        } catch (e: Throwable) {
            env.reportError(e)
            e.printStackTrace()
            false
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            Instance::class.java.name,
            Scope::class.java.name,
            Registry::class.java.name
        )
    }
}

class MagnetProcessorEnv(
    private val processEnvironment: ProcessingEnvironment
) {

    val filer: Filer get() = processEnvironment.filer
    val elements: Elements get() = processEnvironment.elementUtils
    val types: Types get() = processEnvironment.typeUtils
    val annotation = AnnotationValueExtractor(elements)

    fun reportError(e: ValidationException) {
        processEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, e.message, e.element)
    }

    fun reportError(e: CompilationException) {
        processEnvironment.messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Unexpected compilation error," +
                    " please file the bug at https://github.com/beworker/magnet/issues." +
                    " Message: ${e.message ?: "none."}",
            e.element
        )
    }

    fun reportError(e: Throwable) {
        processEnvironment.messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Unexpected compilation error," +
                    " please file the bug at https://github.com/beworker/magnet/issues." +
                    " Message: ${e.message ?: "none."}"
        )
    }
}
