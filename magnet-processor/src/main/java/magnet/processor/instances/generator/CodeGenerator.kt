package magnet.processor.instances.generator

import magnet.processor.instances.FactoryType

interface CodeGenerator {

    fun generateFrom(factoryType: FactoryType): CodeWriter
}
