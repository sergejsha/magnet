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
import magnet.processor.instances.CodeWriter
import magnet.processor.instances.FactoryFromClassAnnotationParser
import magnet.processor.instances.FactoryFromMethodAnnotationParser
import magnet.processor.instances.FactoryIndexCodeGenerator
import magnet.processor.instances.FactoryType
import magnet.processor.instances.FactoryTypeCodeGenerator
import magnet.processor.index.MagnetIndexerGenerator
import magnet.processor.scopes.ScopeProcessor
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MagnetProcessor : AbstractProcessor() {

    private val magnetIndexerGenerator = MagnetIndexerGenerator()

    private lateinit var env: MagnetProcessorEnv
    private lateinit var factoryFromClassAnnotationParser: FactoryFromClassAnnotationParser
    private lateinit var factoryFromMethodAnnotationParser: FactoryFromMethodAnnotationParser
    private lateinit var scopeProcessor: ScopeProcessor
    private lateinit var factoryTypeCodeGenerator: FactoryTypeCodeGenerator
    private lateinit var factoryIndexCodeGenerator: FactoryIndexCodeGenerator

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        env = MagnetProcessorEnv(processingEnvironment)
        factoryFromClassAnnotationParser = FactoryFromClassAnnotationParser(env)
        factoryFromMethodAnnotationParser = FactoryFromMethodAnnotationParser(env)
        scopeProcessor = ScopeProcessor(env)
        factoryTypeCodeGenerator = FactoryTypeCodeGenerator()
        factoryIndexCodeGenerator = FactoryIndexCodeGenerator()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        return try {
            val instanceProcessed = processInstanceAnnotation(roundEnv)
            val scopesProcessed = scopeProcessor.process(roundEnv)
            val indexCreated = processFactoryIndexAnnotation(env, roundEnv)
            instanceProcessed || scopesProcessed || indexCreated
        } catch (e: Throwable) {
            true
        }
    }

    private fun processInstanceAnnotation(
        roundEnv: RoundEnvironment
    ): Boolean {

        val annotatedElements = roundEnv.getElementsAnnotatedWith(Instance::class.java)
        if (annotatedElements.isEmpty()) {
            return false
        }

        val factoryTypes = mutableListOf<FactoryType>()
        ElementFilter.typesIn(annotatedElements).forEach { element ->
            val parsedFactoryTypes = factoryFromClassAnnotationParser.parse(element)
            for (factoryType in parsedFactoryTypes) {
                if (!factoryType.disabled) {
                    factoryTypes.add(factoryType)
                }
            }
        }
        ElementFilter.methodsIn(annotatedElements).forEach { element ->
            val parsedFactoryTypes = factoryFromMethodAnnotationParser.parse(element)
            for (factoryType in parsedFactoryTypes) {
                if (!factoryType.disabled) {
                    factoryTypes.add(factoryType)
                }
            }
        }

        factoryTypes.sortBy { factoryName(it) }

        val codeWriters = mutableListOf<CodeWriter>()
        factoryTypes.forEach { factoryType ->
            codeWriters.add(factoryTypeCodeGenerator.generateFrom(factoryType))
            codeWriters.add(factoryIndexCodeGenerator.generateFrom(factoryType))
        }

        codeWriters.forEach { codeWriter ->
            codeWriter.writeInto(env.filer)
        }

        return true
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

private fun factoryName(factoryType: FactoryType): String = factoryType.factoryType.simpleName()