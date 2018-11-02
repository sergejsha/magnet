package magnet.processor.scopes

import magnet.Scope
import magnet.processor.MagnetProcessorEnv
import magnet.processor.scopes.instances.ScopeInstanceGenerator
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.ElementFilter

class ScopeAnnotationProcessor(
    private val env: MagnetProcessorEnv
) {

    private val scopeParser = ScopeParser(env)
    private val scopeInstanceGenerator = ScopeInstanceGenerator()

    fun process(roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(Scope::class.java)
        if (annotatedElements.isEmpty()) {
            return false
        }

        val elements = ElementFilter.typesIn(annotatedElements)
        elements.forEach { element ->
            val scope = scopeParser.parse(element)
            val codeWriter = scopeInstanceGenerator.generate(scope)
            codeWriter.writeInto(env.filer)
        }

        return !elements.isEmpty()
    }

}