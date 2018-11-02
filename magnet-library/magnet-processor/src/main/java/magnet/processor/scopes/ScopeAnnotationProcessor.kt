package magnet.processor.scopes

import magnet.Scope
import magnet.processor.MagnetProcessorEnv
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.ElementFilter

class ScopeAnnotationProcessor(
    private val env: MagnetProcessorEnv
) {

    private val scopeAnnotationParser = ScopeParser(env)

    fun process(roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(Scope::class.java)
        if (annotatedElements.isEmpty()) {
            return false
        }

        val elements = ElementFilter.typesIn(annotatedElements)
        elements.forEach { element ->
            val scope = scopeAnnotationParser.parse(element)
            // todo
        }

        return !elements.isEmpty()
    }

}