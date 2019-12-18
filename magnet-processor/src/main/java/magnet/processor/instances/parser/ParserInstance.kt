package magnet.processor.instances.parser

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import magnet.Classifier
import magnet.Scoping
import javax.lang.model.element.TypeElement

data class ParserInstance(
    val declaredType: TypeElement? = null,
    val declaredTypes: List<TypeElement>? = null,
    val types: List<ClassName> = emptyList(),
    val classifier: String = Classifier.NONE,
    val scoping: String = Scoping.TOPMOST.name,
    val limitedTo: String = "",
    val selector: List<String>? = null,
    val factory: TypeName? = null,
    val disposer: String? = null,
    val disabled: Boolean = false
)
