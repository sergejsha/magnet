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

package magnet.processor.index

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import magnet.internal.FactoryIndex
import magnet.internal.InstanceFactory
import magnet.processor.MagnetProcessorEnv
import magnet.processor.index.model.Index
import magnet.processor.index.model.Inst
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

private const val INSTANCE_MANAGER = "instanceManager"
private const val MAGNET_INDEXER_CLASS = "magnet.internal.MagnetIndexer"

class MagnetIndexerGenerator {

    private var shouldGenerateRegistry = false

    fun generate(
        annotatedElements: MutableSet<out Any>,
        env: MagnetProcessorEnv
    ): Boolean {
        val alreadyGeneratedMagnetRegistry = env.elements.getTypeElement(MAGNET_INDEXER_CLASS)
        if (alreadyGeneratedMagnetRegistry != null) {
            return false
        }

        if (!shouldGenerateRegistry) {
            shouldGenerateRegistry = !annotatedElements.isEmpty()
            return false // wait for next round even if we should generate
        }

        val registryClassName = ClassName.bestGuess(MAGNET_INDEXER_CLASS)
        val indexElements = env.elements.getPackageElement("magnet.index")?.enclosedElements ?: listOf()
        val magnetRegistryTypeSpec = generateMagnetRegistry(indexElements, registryClassName)

        val packageName = registryClassName.packageName()
        JavaFile.builder(packageName, magnetRegistryTypeSpec)
            .skipJavaLangImports(true)
            .build()
            .writeTo(env.filer)

        return true
    }

    private fun generateMagnetRegistry(
        indexElements: MutableList<out Element>,
        registryClassName: ClassName
    ): TypeSpec {
        return TypeSpec
            .classBuilder(registryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(generateRegisterFactoriesMethod(indexElements))
            .build()
    }

    private fun generateRegisterFactoriesMethod(indexElements: MutableList<out Element>): MethodSpec {

        val factoryRegistryClassName = ClassName.get("magnet.internal", "MagnetInstanceManager")
        val factoryIndexClassName = ClassName.get(FactoryIndex::class.java)

        val impls = mutableListOf<Inst>()
        indexElements.forEach {
            parseFactoryIndexAnnotation(it, factoryIndexClassName) { implTypeName, implFactoryName, implTargetName ->
                impls.add(Inst(implTypeName, implTargetName, implFactoryName))
            }
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
                builder.add("\nnew \$T(),", ClassName.bestGuess(it.factory))
            }

            return builder
                .unindent()
                .add("\n};\n")
                .build()
        }
    }

    private fun <T> parseFactoryIndexAnnotation(
        element: Element,
        annotationClassName: ClassName,
        body: (implTypeName: String, implFactoryName: String, implTargetName: String) -> T) {

        element.annotationMirrors.forEach {
            val itClassName = ClassName.get(it.annotationType)
            if (itClassName == annotationClassName) {
                var interfaceKey: ExecutableElement? = null
                var factoryKey: ExecutableElement? = null
                var classifierKey: ExecutableElement? = null

                it.elementValues.entries.forEach {
                    when (it.key.simpleName.toString()) {
                        "type" -> interfaceKey = it.key
                        "factory" -> factoryKey = it.key
                        "classifier" -> classifierKey = it.key
                    }
                }

                if (interfaceKey != null && factoryKey != null) {
                    body(
                        it.elementValues[interfaceKey]!!.value.toString(),
                        it.elementValues[factoryKey]!!.value.toString(),
                        it.elementValues[classifierKey]!!.value.toString()
                    )
                }
            }
        }
    }

}