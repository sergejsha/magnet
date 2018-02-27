package magnet

import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class MagnetProcessorEnv(private val processEnvironment: ProcessingEnvironment) {

    val filer: Filer
        get() = processEnvironment.filer

    val elements: Elements
        get() = processEnvironment.elementUtils

    fun reportError(element: Element, message: String) {
        processEnvironment.messager
                .printMessage(Diagnostic.Kind.ERROR, message, element)
    }

}