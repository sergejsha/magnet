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

package magnet.processor.instances.aspects.index

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import magnet.internal.Generated
import magnet.internal.Index
import magnet.internal.InstanceFactory
import magnet.processor.instances.generator.CodeGenerator
import magnet.processor.instances.generator.CodeWriter
import magnet.processor.instances.FactoryType
import magnet.processor.instances.FactoryTypeVisitor
import javax.lang.model.element.Modifier

class FactoryIndexCodeGenerator : FactoryTypeVisitor, CodeGenerator {

    private lateinit var factoryIndexTypeSpec: TypeSpec
    private lateinit var factoryIndexClassName: ClassName

    override fun exitFactoryClass(factory: FactoryType) {
        val factoryPackage = factory.factoryType.packageName()
        val factoryName = factory.factoryType.simpleName()
        val factoryIndexName = "${factoryPackage.replace('.', '_')}_$factoryName"

        factoryIndexClassName = ClassName.get("magnet.index", factoryIndexName)

        factoryIndexTypeSpec = TypeSpec
            .classBuilder(factoryIndexClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(Generated::class.java)
            .addAnnotation(
                generateFactoryIndexAnnotation(
                    factory.factoryType,
                    factory.interfaceType.reflectionName(),
                    factory.classifier
                )
            )
            .build()
    }

    private fun generateFactoryIndexAnnotation(
        factoryClassName: ClassName,
        instanceType: String,
        classifier: String
    ): AnnotationSpec {
        return AnnotationSpec.builder(Index::class.java)
            .addMember("factoryType", "\$T.class", InstanceFactory::class.java)
            .addMember("factoryClass", "\$T.class", factoryClassName)
            .addMember("instanceType", "\$S", instanceType)
            .addMember("classifier", "\$S", classifier)
            .build()
    }

    override fun generateFrom(factoryType: FactoryType): CodeWriter {
        factoryType.accept(this)
        return CodeWriter(factoryIndexClassName.packageName(), factoryIndexTypeSpec)
    }
}
