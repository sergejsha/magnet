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

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import magnet.internal.FactoryIndex
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

class FactoryIndexGenerator {

    fun generate(element: ExecutableElement, env: MagnetProcessorEnv) {
        // todo
    }

    fun generate(implTypeElement: TypeElement, env: MagnetProcessorEnv) {
        val implClassName = ClassName.get(implTypeElement)
        val annotationsClassName = ClassName.get(Implementation::class.java)
        parseImplementationAnnotation(implTypeElement, annotationsClassName) { type, classifier ->

            val factoryTypeSpec = generateFactoryIndex(
                implClassName,
                type,
                classifier
            )

            JavaFile.builder("magnet.index", factoryTypeSpec)
                .skipJavaLangImports(true)
                .build()
                .writeTo(env.filer)
        }
    }

    private fun <T> parseImplementationAnnotation(
        element: Element,
        annotationClassName: ClassName,
        body: (type: String, classifier: String) -> T
    ) {
        element.annotationMirrors.forEach {
            val itClassName = ClassName.get(it.annotationType)
            if (itClassName == annotationClassName) {
                var typeKey: ExecutableElement? = null
                var classifierKey: ExecutableElement? = null

                it.elementValues.entries.forEach {
                    when (it.key.simpleName.toString()) {
                        "type" -> typeKey = it.key
                        "classifier" -> classifierKey = it.key
                    }
                }

                if (typeKey != null) {
                    body(
                        it.elementValues[typeKey]!!.value.toString(),
                        if (classifierKey != null) it.elementValues[classifierKey]!!.value.toString() else ""
                    )
                }
            }
        }
    }

    private fun generateFactoryIndex(
        implClassName: ClassName,
        implType: String,
        implClassifier: String = Classifier.NONE
    ): TypeSpec {


        val factoryPackage = implClassName.packageName()
        val factoryName = "Magnet${implClassName.simpleName()}Factory"
        val factoryClassName = ClassName.bestGuess("$factoryPackage.$factoryName")

        val factoryIndexPackage = "magnet.index"
        val factoryIndexName = "${factoryPackage.replace('.', '_')}_$factoryName"
        val factoryIndexClassName = ClassName.get(factoryIndexPackage, factoryIndexName)

        return TypeSpec
            .classBuilder(factoryIndexClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(
                generateFactoryIndexAnnotation(
                    factoryClassName,
                    implType,
                    implClassifier
                )
            )
            .build()
    }

    private fun generateFactoryIndexAnnotation(
        factoryClassName: ClassName,
        implType: String,
        implClassifier: String
    ): AnnotationSpec {
        return AnnotationSpec.builder(FactoryIndex::class.java)
            .addMember("factory", "\$T.class", factoryClassName)
            .addMember("type", "\$S", implType)
            .addMember("classifier", "\$S", implClassifier)
            .build()
    }

}