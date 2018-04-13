package magnet.processor

import magnet.processor.model.FactoryType

interface CodeGenerator {

    fun generateFrom(factoryType: FactoryType): CodeWriter

}