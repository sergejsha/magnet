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

    fun generate(implTypeElement: TypeElement, env: MagnetProcessorEnv) {
        val implClassName = ClassName.get(implTypeElement)
        val annotationsClassName = ClassName.get(Implementation::class.java)
        parseImplementationAnnotation(implTypeElement, annotationsClassName) { forType, forTarget ->

            val factoryTypeSpec = generateFactoryIndex(
                implClassName,
                forType,
                forTarget
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
        body: (forType: String, forTarget: String) -> T
    ) {
        element.annotationMirrors.forEach {
            val itClassName = ClassName.get(it.annotationType)
            if (itClassName == annotationClassName) {
                var forTypeKey: ExecutableElement? = null
                var forTargetKey: ExecutableElement? = null

                it.elementValues.entries.forEach {
                    when (it.key.simpleName.toString()) {
                        "forType" -> forTypeKey = it.key
                        "forTarget" -> forTargetKey = it.key
                    }
                }

                if (forTypeKey != null) {
                    body(
                        it.elementValues[forTypeKey]!!.value.toString(),
                        if (forTargetKey != null) it.elementValues[forTargetKey]!!.value.toString() else ""
                    )
                }
            }
        }
    }

    private fun generateFactoryIndex(
        implClassName: ClassName,
        implType: String,
        implTarget: String = ""
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
                    implTarget
                )
            )
            .build()
    }

    private fun generateFactoryIndexAnnotation(
        factoryClassName: ClassName,
        implType: String,
        implTarget: String
    ): AnnotationSpec {
        return AnnotationSpec.builder(FactoryIndex::class.java)
            .addMember("factory", "\$T.class", factoryClassName)
            .addMember("type", "\$S", implType)
            .addMember("target", "\$S", implTarget)
            .build()
    }

}