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

import magnet.Implementation
import magnet.Magnetizer
import magnet.processor.factory.CodeWriter
import magnet.processor.factory.FactoryCodeGenerator
import magnet.processor.factory.FactoryFromClassAnnotationParser
import magnet.processor.factory.FactoryFromMethodAnnotationParser
import magnet.processor.factory.FactoryIndexCodeGenerator
import magnet.processor.factory.FactoryType
import magnet.processor.index.MagnetIndexerGenerator
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
    private lateinit var factoryCodeGenerator: FactoryCodeGenerator
    private lateinit var factoryIndexCodeGenerator: FactoryIndexCodeGenerator

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        env = MagnetProcessorEnv(processingEnvironment)
        factoryFromClassAnnotationParser = FactoryFromClassAnnotationParser(env)
        factoryFromMethodAnnotationParser = FactoryFromMethodAnnotationParser(env)
        factoryCodeGenerator = FactoryCodeGenerator()
        factoryIndexCodeGenerator = FactoryIndexCodeGenerator()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        return try {
            val implementationProcessed = processImplementationAnnotation(roundEnv)
            val indexCreated = processFactoryIndexAnnotation(env, roundEnv)

            implementationProcessed || indexCreated
        } catch (e: CompilationException) {
            true
        }
    }

    private fun processImplementationAnnotation(
        roundEnv: RoundEnvironment
    ): Boolean {

        val annotatedElements = roundEnv.getElementsAnnotatedWith(Implementation::class.java)
        if (annotatedElements.isEmpty()) {
            return false
        }

        val factoryTypes = mutableListOf<FactoryType>()
        ElementFilter.typesIn(annotatedElements).forEach { element ->
            val factoryType = factoryFromClassAnnotationParser.parse(element)
            if (!factoryType.annotation.disabled) {
                factoryTypes.add(factoryType)
            }
        }
        ElementFilter.methodsIn(annotatedElements).forEach { element ->
            val factoryType = factoryFromMethodAnnotationParser.parse(element)
            if (!factoryType.annotation.disabled) {
                factoryTypes.add(factoryType)
            }
        }

        factoryTypes.sortBy { factoryName(it) }

        val codeWriters = mutableListOf<CodeWriter>()
        factoryTypes.forEach { factoryType ->
            codeWriters.add(factoryCodeGenerator.generateFrom(factoryType))
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
            Implementation::class.java.name,
            Magnetizer::class.java.name
        )
    }

}

private fun factoryName(factoryType: FactoryType): String = factoryType.factoryType.simpleName()