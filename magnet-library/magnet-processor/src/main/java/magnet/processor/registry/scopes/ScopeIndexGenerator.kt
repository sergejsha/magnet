package magnet.processor.registry.scopes

import com.squareup.javapoet.CodeBlock
import magnet.internal.ScopeFactory
import magnet.processor.registry.Model

class ScopeIndexGenerator {

    val variableName = "scopeFactories"

    fun generate(registry: Model.Registry): CodeBlock {
        if (registry.scopeFactories.isEmpty()) {
            return CodeBlock.builder()
                .addStatement(
                    "\$T<\$T, \$T> scopeFactories = null",
                    Map::class.java, Class::class.java, ScopeFactory::class.java
                )
                .build()
        }
        TODO()
    }

}