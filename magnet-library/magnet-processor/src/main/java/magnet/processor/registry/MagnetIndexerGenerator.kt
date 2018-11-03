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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import magnet.internal.InstanceFactory
import magnet.processor.MagnetProcessorEnv
import magnet.processor.registry.model.Index
import magnet.processor.registry.model.Inst
import javax.lang.model.element.Modifier

private const val INSTANCE_MANAGER = "instanceManager"

class MagnetIndexerGenerator(
    private val env: MagnetProcessorEnv
) {

    fun generate(registry: Model.Registry) {
        val registryClassName = ClassName.bestGuess(REGISTRY_CLASS_NAME)
        val magnetRegistryTypeSpec = generateMagnetRegistry(registry, registryClassName)

        val packageName = registryClassName.packageName()
        JavaFile.builder(packageName, magnetRegistryTypeSpec)
            .skipJavaLangImports(true)
            .build()
            .writeTo(env.filer)
    }

    private fun generateMagnetRegistry(
        registry: Model.Registry,
        registryClassName: ClassName
    ): TypeSpec {
        return TypeSpec
            .classBuilder(registryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(generateRegisterFactoriesMethod(registry))
            .build()
    }

    private fun generateRegisterFactoriesMethod(
        registry: Model.Registry
    ): MethodSpec {

        val factoryRegistryClassName = ClassName.get("magnet.internal", "MagnetInstanceManager")

        val impls = registry.instanceFactories.map {
            Inst(
                type = it.instanceType.toQualifiedName(),
                classifier = it.classifier,
                factory = it.factoryClass
            )
        }

        val index = Indexer().index(impls)

        return MethodSpec
            .methodBuilder("register")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ParameterSpec
                .builder(factoryRegistryClassName, INSTANCE_MANAGER)
                .build())
            .addCode(generateArrayOfFactoriesCodeBlock(index))
            .addCode(generateIndexCodeBlock(index))
            .addStatement("\$L.register(factories, index, null)", INSTANCE_MANAGER)
            .build()
    }

    private fun generateIndexCodeBlock(index: Index): CodeBlock {
        val indexGenerator = IndexGeneratorVisitor()
        index.accept(indexGenerator)

        val mapSize = Math.max(Math.round(index.instances.size / 0.75f), 16)
        return CodeBlock.builder()
            .addStatement(
                "\$T<\$T, \$T> index = new \$T<>($mapSize)",
                Map::class.java,
                Class::class.java,
                Object::class.java,
                HashMap::class.java
            )
            .add(indexGenerator.targetsBuilder.build())
            .add(indexGenerator.indexBuilder.build())
            .build()
    }

    private fun generateArrayOfFactoriesCodeBlock(index: Index): CodeBlock {
        if (index.instances.isEmpty()) {
            return CodeBlock.builder()
                .addStatement("\$T[] factories = new \$T[0]", InstanceFactory::class.java, InstanceFactory::class.java)
                .build()

        } else {
            val builder = CodeBlock.builder()
                .add("\$T[] factories = new \$T[] {", InstanceFactory::class.java, InstanceFactory::class.java)
                .indent()

            index.instances.forEach {
                builder.add("\nnew \$T(),", it.factory)
            }

            return builder
                .unindent()
                .add("\n};\n")
                .build()
        }
    }

}

private fun ClassName.toQualifiedName(): String =
    "${this.packageName()}.${this.simpleName()}"
