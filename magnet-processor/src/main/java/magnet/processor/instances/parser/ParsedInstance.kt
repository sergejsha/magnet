package magnet.processor.instances.parser

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName

data class ParsedInstance(
    val types: List<ClassName>,
    val classifier: String,
    val scoping: String,
    val limitedTo: String,
    val selector: List<String>?,
    val factory: TypeName?,
    val disposer: String?,
    val disabled: Boolean
)
