package magnet.processor.registry.instances

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import magnet.internal.InstanceFactory
import magnet.processor.registry.Model.Registry

internal class InstanceIndexGenerator {

    fun generate(registry: Registry): CodeBlock {

        val index = Indexer().index(
            registry.instanceFactories.map {
                Model.Inst(
                    type = it.instanceType.toQualifiedName(),
                    classifier = it.classifier,
                    factory = it.factoryClass
                )
            }
        )

        return CodeBlock.builder()
            .add(generateArrayOfFactoriesCodeBlock(index))
            .add(generateIndexCodeBlock(index))
            .build()
    }

    private fun generateIndexCodeBlock(index: Model.Index): CodeBlock {
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

    private fun generateArrayOfFactoriesCodeBlock(index: Model.Index): CodeBlock {
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
