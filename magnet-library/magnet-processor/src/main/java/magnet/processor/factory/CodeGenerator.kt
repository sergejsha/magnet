package magnet.processor.factory

interface CodeGenerator {

    fun generateFrom(factoryType: FactoryType): CodeWriter

}