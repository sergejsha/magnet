package magnet.processor.factory

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.WildcardTypeName
import magnet.Classifier
import magnet.Implementation
import magnet.InstanceRetention
import magnet.Scope
import magnet.processor.CompilationException
import magnet.processor.MagnetProcessorEnv
import magnet.processor.mirrors
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind

private const val CLASS_NULLABLE = ".Nullable"
private const val ATTR_TYPE = "type"
private const val ATTR_INSTANCE_RETENTION = "instanceRetention"
private const val ATTR_CLASSIFIER = "classifier"

internal open class AnnotationParser(
    protected val env: MagnetProcessorEnv
) {

    protected fun parseMethodParameter(
        element: Element,
        variable: VariableElement
    ): MethodParameter {

        val variableType = variable.asType()
        if (variableType.kind == TypeKind.TYPEVAR) {
            env.reportError(element,
                "Constructor parameter '${variable.simpleName}' is specified using a generic" +
                    " type which is not supported by Magnet. Use a non-parameterized class or interface" +
                    " type instead. To inject current scope instance, use 'Scope' parameter type.")
            throw CompilationException()
        }

        val paramSpec = ParameterSpec.get(variable)
        val paramName = paramSpec.name

        val isScopeParam = variableType.toString() == Scope::class.java.name
        if (isScopeParam) {
            return MethodParameter(
                PARAM_SCOPE_NAME,
                ClassName.get(Scope::class.java),
                Classifier.NONE,
                GetterMethod.GET_SCOPE
            )
        }

        var paramTypeName = paramSpec.type
        var getterMethod: GetterMethod? = null

        paramTypeName = if (paramTypeName is ParameterizedTypeName) {
            if (paramTypeName.rawType.reflectionName() == List::class.java.typeName) {
                getterMethod = GetterMethod.GET_MANY
                paramTypeName.typeArguments[0]
            } else {
                paramTypeName.rawType
            }
        } else {
            ClassName.get(variableType)
        }

        if (paramTypeName is WildcardTypeName) {
            if (paramTypeName.lowerBounds.size > 0) {
                env.reportError(element,
                    "Only single upper bounds class parameter is supported," +
                        " for example List<${paramTypeName.lowerBounds[0]}>")
                throw CompilationException()
            }

            val upperBounds = paramTypeName.upperBounds
            if (upperBounds.size > 1) {
                env.reportError(element,
                    "Only single upper bounds class parameter is supported," +
                        " for example List<${upperBounds[0]}>")
                throw CompilationException()
            }

            paramTypeName = upperBounds[0]
        }

        var hasNullableAnnotation = false
        var classifier: String = Classifier.NONE

        variable.annotationMirrors.forEach { annotationMirror ->
            if (annotationMirror.mirrors<Classifier>()) {
                val declaredClassifier: String? = annotationMirror.elementValues.values.firstOrNull()?.value.toString()
                declaredClassifier?.let {
                    classifier = it.removeSurrounding("\"", "\"")
                }

            } else {
                val annotationType = annotationMirror.annotationType.toString()
                if (annotationType.endsWith(CLASS_NULLABLE)) {
                    hasNullableAnnotation = true
                }
            }
        }

        if (getterMethod == null) {
            getterMethod = if (hasNullableAnnotation) GetterMethod.GET_OPTIONAL else GetterMethod.GET_SINGLE
        }

        return MethodParameter(
            paramName,
            paramTypeName,
            classifier,
            getterMethod
        )
    }

    protected fun parseAnnotation(element: Element, checkInheritance: Boolean = false): Annotation {

        var interfaceTypeElement: TypeElement? = null
        var retention = InstanceRetention.SCOPE.name
        var classifier = Classifier.NONE

        element.annotationMirrors.forEach { annotationMirror ->
            if (annotationMirror.mirrors<Implementation>()) {
                annotationMirror.elementValues.entries.forEach { entry ->
                    val entryName = entry.key.simpleName.toString()
                    val entryValue = entry.value.value.toString()
                    when (entryName) {
                        ATTR_TYPE -> {
                            interfaceTypeElement = env.elements.getTypeElement(entryValue)
                            if (checkInheritance) {
                                val isTypeImplemented = env.types.isAssignable(
                                    element.asType(),
                                    env.types.getDeclaredType(interfaceTypeElement) // erase generic type
                                )
                                if (!isTypeImplemented) {
                                    throw env.compilationError(element,
                                        "$element must implement $interfaceTypeElement")
                                }
                            }
                        }
                        ATTR_INSTANCE_RETENTION -> {
                            retention = entryValue
                        }
                        ATTR_CLASSIFIER -> {
                            classifier = entryValue
                        }
                    }
                }
            }
        }

        val interfaceType = if (interfaceTypeElement == null) {
            throw env.compilationError(element, "${Implementation::class.java} must declare 'type' property.")
        } else {
            ClassName.get(interfaceTypeElement)
        }

        return Annotation(
            interfaceType,
            classifier,
            retention
        )
    }

}

internal data class Annotation(
    val type: ClassName,
    val classifier: String,
    val retention: String
)