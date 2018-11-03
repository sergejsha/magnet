package magnet.processor.registry

import com.squareup.javapoet.ClassName

interface Model {

    class Registry(
        val instanceFactories: List<InstanceFactory>,
        val scopeFactories: List<ScopeFactory>
    )

    class InstanceFactory(
        val factoryClass: ClassName,
        val instanceType: ClassName,
        val classifier: String
    )

    class ScopeFactory(
        val factoryClass: ClassName,
        val instanceType: ClassName
    )

}

