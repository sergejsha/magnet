package magnet.processor.scopes

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName
import magnet.Classifier
import magnet.processor.CompilationError
import magnet.processor.MagnetProcessorEnv
import magnet.processor.UnexpectedCompilationError
import magnet.processor.common.CommonModel
import magnet.processor.eachAnnotationAttributeOf
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementScanner6

internal class ScopeParser(
    private val env: MagnetProcessorEnv
) {

    private val scopeVisitor = ScopeVisitor()

    fun parse(element: TypeElement): Model.Scope {
        try {
            element.accept(scopeVisitor, null)
            return scopeVisitor.createScope()

        } catch (e: CompilationError) {
            throw env.compilationError(
                element = e.element, message = e.message, cause = e
            )
        } catch (e: UnexpectedCompilationError) {
            throw env.unexpectedCompilationError(
                element = e.element, message = e.message, cause = e
            )
        } catch (e: Throwable) {
            throw env.unexpectedCompilationError(
                element = element, message = e.message, cause = e
            )
        }
    }

}

private class ScopeVisitor : ElementScanner6<Unit, Unit>() {

    private val bindMethods = mutableListOf<Model.BindMethod>()
    private val getterMethods = mutableListOf<Model.GetterMethod>()

    private var methodBuilder: MethodBuilder? = null
    private var scopeType: ClassName? = null

    override fun visitType(e: TypeElement, p: Unit?) {
        clear()
        scopeType = when (val type = TypeName.get(e.asType())) {
            is ClassName -> type
            else -> throw CompilationError(
                element = e,
                message = "Scope declaration must be a not parametrized interface."
            )
        }
        super.visitType(e, p)
    }

    override fun visitExecutable(e: ExecutableElement, p: Unit?) {
        maybeCompleteMethod()
        methodBuilder = MethodBuilder(
            element = e,
            name = e.simpleName.toString()
        )
        super.visitExecutable(e, p)
    }

    override fun visitVariable(e: VariableElement, p: Unit?) {
        methodBuilder?.params?.add(e.toInstance(e.asType()))
        super.visitVariable(e, p)
    }

    private fun maybeCompleteMethod() {
        methodBuilder?.let { method ->
            when (method.element.returnType.kind) {
                TypeKind.VOID -> bindMethods.add(method.toBindMethod())
                else -> getterMethods.add(method.toGetterMethod())
            }
            methodBuilder = null
        }
    }

    fun createScope(): Model.Scope {
        maybeCompleteMethod()
        return Model.Scope(
            type = requireNotNull(scopeType),
            bindParentScopeMethod = null,
            bindMethods = bindMethods.toList(),
            getterMethods = getterMethods.toList()
        ).also {
            clear()
        }
    }

    fun clear() {
        methodBuilder = null
        scopeType = null
        bindMethods.clear()
        getterMethods.clear()
    }

}

private class MethodBuilder(
    val element: ExecutableElement,
    val name: String,
    val params: MutableList<CommonModel.Instance> = mutableListOf()
) {

    fun toGetterMethod(): Model.GetterMethod {
        if (params.isNotEmpty()) {
            throw CompilationError(
                element = element,
                message = "Getter method must have no parameters."
            )
        }
        return Model.GetterMethod(
            name = name,
            instance = element.toInstance(element.returnType)
        )
    }

    fun toBindMethod(): Model.BindMethod {
        if (params.size != 1) {

        }
        return Model.BindMethod(
            name = name,
            instance = params.getOrNull(0)
                ?: throw CompilationError(
                    element = element,
                    message = "Binder method must have exactly one parameter."
                )
        )
    }

}

private fun Element.toInstance(typeMirror: TypeMirror): CommonModel.Instance {
    val typeName = TypeName.get(typeMirror)
    val name = simpleName.toString()
    return when (typeName) {

        TypeName.VOID -> throw UnexpectedCompilationError(
            element = this,
            message = "Returning type must not be void."
        )

        is ParameterizedTypeName -> {
            if (typeName.rawType.reflectionName() == List::class.java.name) {
                val parameterType = typeName.typeArguments.getOrNull(0)
                    ?: throw UnexpectedCompilationError(
                        element = this,
                        message = "Cannot read class parameter of $typeName"
                    )

                CommonModel.Instance(
                    name = name,
                    type = parameterType,
                    classifier = getClassifier(),
                    cardinality = CommonModel.Cardinality.Many
                )
            } else {
                CommonModel.Instance(
                    name = name,
                    type = typeName,
                    classifier = getClassifier(),
                    cardinality = getSingleOrOptionalCardinality()
                )
            }
        }

        is WildcardTypeName -> {
            CommonModel.Instance(
                name = name,
                type = typeName.eraseParameterTypes(this),
                classifier = getClassifier(),
                cardinality = getSingleOrOptionalCardinality()
            )
        }

        else -> CommonModel.Instance(
            name = name,
            type = typeName,
            classifier = getClassifier(),
            cardinality = getSingleOrOptionalCardinality()
        )
    }
}

fun Element.getClassifier(): String {
    eachAnnotationAttributeOf<Classifier> { _, value ->
        return value.toString().removeSurrounding("\"", "\"")
    }
    return Classifier.NONE
}

private fun Element.getSingleOrOptionalCardinality(): CommonModel.Cardinality {
    annotationMirrors.forEach { annotationMirror ->
        val annotationType = annotationMirror.annotationType.toString()
        if (annotationType.endsWith(".Nullable")) {
            return CommonModel.Cardinality.Optional
        }
    }
    return CommonModel.Cardinality.Single
}

private fun WildcardTypeName.eraseParameterTypes(element: Element): TypeName {
    if (lowerBounds.size > 0) {
        throw CompilationError(element,
            "Magnet supports single upper bounds class parameter only," +
                " while lower bounds class parameter was found.")
    }
    if (upperBounds.size > 1) {
        throw CompilationError(element,
            "Magnet supports single upper bounds class parameter only," +
                " for example List<${upperBounds[0]}>")
    }
    return upperBounds[0]
}



