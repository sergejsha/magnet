package magnet

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import magnet.indexer.IndexGeneratorVisitor
import magnet.indexer.Indexer
import magnet.indexer.model.Impl
import magnet.indexer.model.Index
import magnet.internal.Factory
import magnet.internal.FactoryIndex
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

class MagnetIndexerGenerator {

    private var shouldGenerateRegistry = false

    fun generate(
            annotatedElements: MutableSet<out Any>,
            env: MagnetProcessorEnv
    ): Boolean {
        val alreadyGeneratedMagnetRegistry = env.elements.getTypeElement("magnet.MagnetIndexer")
        if (alreadyGeneratedMagnetRegistry != null) {
            return false
        }

        if (!shouldGenerateRegistry) {
            shouldGenerateRegistry = !annotatedElements.isEmpty()
            return false // wait for next round even if we should generate
        }

        val registryClassName = ClassName.get("magnet", "MagnetIndexer")
        println("  Generating $registryClassName")

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

        val factoryRegistryClassName = ClassName.get(MagnetImplementationManager::class.java)
        val factoryIndexClassName = ClassName.get(FactoryIndex::class.java)

        val impls = mutableListOf<Impl>()
        indexElements.forEach {
            parseFactoryIndexAnnotation(it, factoryIndexClassName) { implTypeName, implFactoryName, implTargetName ->
                impls.add(Impl(implTypeName, implTargetName, implFactoryName))
            }
        }

        val index = Indexer().index(impls)

        return MethodSpec
                .methodBuilder("register")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec
                        .builder(factoryRegistryClassName, "implementationManager")
                        .build())
                .addCode(generateArrayOfFactoriesCodeBlock(index))
                .addCode(generateIndexCodeBlock(index))
                .addStatement("implementationManager.register(factories, index)")
                .build()
    }

    private fun generateIndexCodeBlock(index: Index): CodeBlock {
        val indexGenerator = IndexGeneratorVisitor()
        index.accept(indexGenerator)
        return CodeBlock.builder()
                .addStatement(
                        "\$T<\$T, \$T> index = new \$T<>()",
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
        if (index.implementations.isEmpty()) {
            return CodeBlock.builder()
                    .addStatement("\$T[] factories = new \$T[0]", Factory::class.java, Factory::class.java)
                    .build()

        } else {
            val builder = CodeBlock.builder()
                    .add("\$T[] factories = new \$T[] {", Factory::class.java, Factory::class.java)
                    .indent()

            index.implementations.forEach {
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
                var targetKey: ExecutableElement? = null

                it.elementValues.entries.forEach {
                    when (it.key.simpleName.toString()) {
                        "type" -> interfaceKey = it.key
                        "factory" -> factoryKey = it.key
                        "target" -> targetKey = it.key
                    }
                }

                if (interfaceKey != null && factoryKey != null) {
                    body(
                            it.elementValues[interfaceKey]!!.value.toString(),
                            it.elementValues[factoryKey]!!.value.toString(),
                            it.elementValues[targetKey]!!.value.toString()
                    )
                }
            }
        }
    }

}