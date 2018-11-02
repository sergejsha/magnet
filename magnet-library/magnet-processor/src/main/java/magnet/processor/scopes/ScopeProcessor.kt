package magnet.processor.scopes

import magnet.Scope
import magnet.processor.MagnetProcessorEnv
import magnet.processor.factory.CodeWriter
import magnet.processor.scopes.factories.ScopeFactoryGenerator
import magnet.processor.scopes.instances.ScopeInstanceGenerator
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.ElementFilter

class ScopeProcessor(
    private val env: MagnetProcessorEnv
) {

    private val scopeParser = ScopeParser(env)
    private val scopeInstanceGenerator = ScopeInstanceGenerator()
    private val scopeFactoryGenerator = ScopeFactoryGenerator()

    fun process(roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(Scope::class.java)
        if (annotatedElements.isEmpty()) {
            return false
        }

        val codeWriters = mutableListOf<CodeWriter>()
        for (element in ElementFilter.typesIn(annotatedElements)) {
            val scope = scopeParser.parse(element)
            codeWriters.add(scopeInstanceGenerator.generate(scope))
            codeWriters.add(scopeFactoryGenerator.generate(scope))
        }

        for (codeWriter in codeWriters) {
            codeWriter.writeInto(env.filer)
        }

        return !codeWriters.isEmpty()
    }

}
