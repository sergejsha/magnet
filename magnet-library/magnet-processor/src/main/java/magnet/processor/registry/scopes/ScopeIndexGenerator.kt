package magnet.processor.registry.scopes

import com.squareup.javapoet.CodeBlock
import magnet.internal.ScopeFactory
import magnet.processor.registry.Model

private const val LOADING_FACTOR = .75f

class ScopeIndexGenerator {

    val variableName = "scopeFactories"

    fun generate(registry: Model.Registry): CodeBlock {
        val capacity = Math.ceil(registry.scopeFactories.size.toDouble() / LOADING_FACTOR).toInt()
        var builder = CodeBlock.builder()
            .addStatement(
                "\$T<\$T, \$T> $variableName = new \$T($capacity)",
                Map::class.java, Class::class.java, ScopeFactory::class.java, HashMap::class.java
            )

        for (factory in registry.scopeFactories) {
            builder = builder.addStatement(
                "$variableName.put(\$T.getType(), new \$T())",
                factory.factoryClass, factory.factoryClass
            )
        }

        return builder.build()
    }

}