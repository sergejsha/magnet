package magnet.processor.instances.kotlin

import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmConstructorVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmValueParameterVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.TypeElement

interface MethodMetadata {
    fun isParameterNullable(paramName: String, typeDepth: Int): TypeMeta
}

internal class KotlinConstructorMetadata(
    private val element: TypeElement
) : MethodMetadata {

    private var types: Map<String, List<TypeMeta>> = emptyMap()

    override fun isParameterNullable(paramName: String, typeDepth: Int): TypeMeta {
        if (types.isEmpty()) {
            types = parseNullability()
        }
        val types = checkNotNull(types[paramName]) {
            "Cannot find parameter '$paramName' in metadata of $element"
        }
        if (typeDepth >= types.size) {
            error("Cannot find TypeMeta depth of $typeDepth in $types")
        }
        return types[typeDepth]
    }

    private fun parseNullability(): Map<String, List<TypeMeta>> {
        val metadata = element.getAnnotation(Metadata::class.java)?.let {
            KotlinClassMetadata.read(
                KotlinClassHeader(
                    it.kind,
                    it.metadataVersion,
                    it.bytecodeVersion,
                    it.data1,
                    it.data2,
                    it.extraString,
                    it.packageName,
                    it.extraInt
                )
            )
        }

        if (metadata !is KotlinClassMetadata.Class) {
            error("Expecting 'KotlinClassMetadata.Class' while $metadata was received.")
        }

        val primaryConstructorVisitor = PrimaryConstructorVisitor()
        metadata.accept(primaryConstructorVisitor)

        return primaryConstructorVisitor.nullability
    }
}

private class PrimaryConstructorVisitor : KmClassVisitor() {
    val nullability = mutableMapOf<String, List<TypeMeta>>()

    override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
        return if (flags.isPrimaryConstructor) {
            object : KmConstructorVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(flags) { typeMeta ->
                                nullability[name] = typeMeta
                            }
                        }
                    }
                }
            }
        } else null
    }
}

class TypeExtractorVisitor(
    private val flags: Flags,
    private val typeMeta: MutableList<TypeMeta> = mutableListOf(),
    private val onVisitEnd: OnVisitEnd? = null
) : KmTypeVisitor() {


    override fun visitClass(name: ClassName) {
        typeMeta.add(TypeMeta(name, flags.isNullableType))
    }

    override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? =
        TypeExtractorVisitor(flags, typeMeta)

    override fun visitEnd() {
        onVisitEnd?.invoke(typeMeta)
    }
}

typealias OnVisitEnd = (List<TypeMeta>) -> Unit

data class TypeMeta(
    val type: String,
    val nullable: Boolean
)

internal val Flags.isPrimaryConstructor: Boolean get() = Flag.Constructor.IS_PRIMARY(this)
internal val Flags.isNullableType: Boolean get() = Flag.Type.IS_NULLABLE(this)
