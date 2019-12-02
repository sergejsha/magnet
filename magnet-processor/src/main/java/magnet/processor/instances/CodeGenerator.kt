package magnet.processor.instances

interface CodeGenerator {

    fun generateFrom(factoryType: FactoryType): CodeWriter
}
