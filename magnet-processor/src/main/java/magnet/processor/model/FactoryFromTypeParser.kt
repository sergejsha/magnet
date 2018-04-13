package magnet.processor.model

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.WildcardTypeName
import magnet.BreakGenerationException
import magnet.Classifier
import magnet.Implementation
import magnet.InstanceRetention
import magnet.MagnetProcessorEnv
import magnet.Scope
import magnet.mirrors
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.ElementFilter

private const val ATTR_TYPE = "type"
private const val ATTR_INSTANCE_RETENTION = "instanceRetention"
private const val CLASS_NULLABLE = ".Nullable"

class FactoryFromTypeParser(
    private val env: MagnetProcessorEnv
) {

    fun parse(element: TypeElement): FactoryType {

        val instanceType = ClassName.get(element)
        val instancePackage = instanceType.packageName()
        val instanceName = instanceType.simpleName()
        val factoryType = ClassName.bestGuess("${instancePackage}.Magnet${instanceName}Factory")

        //var interfaceType: ClassName? = null
        var interfaceTypeElement: TypeElement? = null
        var instanceRetention = InstanceRetention.SCOPE.name

        element.annotationMirrors.forEach { annotationMirror ->
            if (annotationMirror.mirrors<Implementation>()) {
                annotationMirror.elementValues.entries.forEach { entry ->
                    val entryName = entry.key.simpleName.toString()
                    val entryValue = entry.value.value.toString()
                    when (entryName) {
                        ATTR_TYPE -> {
                            interfaceTypeElement = env.elements.getTypeElement(entryValue)
                            val isTypeImplemented = env.types.isAssignable(
                                element.asType(),
                                env.types.getDeclaredType(interfaceTypeElement) // erase generic type
                            )
                            if (!isTypeImplemented) {
                                env.reportError(element, "$element must implement $interfaceTypeElement")
                                throw BreakGenerationException()
                            }
                        }
                        ATTR_INSTANCE_RETENTION -> {
                            instanceRetention = entryValue
                        }
                    }
                }
            }
        }

        val interfaceType = if (interfaceTypeElement == null) {
            env.reportError(element, "${Implementation::class.java} must declare 'type' property.")
            throw BreakGenerationException()
        } else {
            ClassName.get(interfaceTypeElement)
        }

        val createMethod = parseConstructor(element)
        val retentionMethod = GetRetentionMethod(instanceRetention)

        return FactoryType(
            element,
            factoryType,
            interfaceType,
            instanceType,
            createMethod,
            retentionMethod
        )
    }

    private fun parseConstructor(element: TypeElement): CreateMethod {

        val constructors = ElementFilter.constructorsIn(element.enclosedElements)
        if (constructors.size != 1) {
            env.reportError(element, "Exactly one constructor is required for $element")
            throw BreakGenerationException()
        }

        val methodParameters = mutableListOf<MethodParameter>()
        constructors[0].parameters.forEach { variable ->
            val methodParameter = parseMethodParameter(element, variable)
            methodParameters.add(methodParameter)
        }

        return CreateMethod(
            methodParameters
        )
    }

    private fun parseMethodParameter(
        element: TypeElement,
        variable: VariableElement
    ): MethodParameter {

        val variableType = variable.asType()
        if (variableType.kind == TypeKind.TYPEVAR) {
            env.reportError(element,
                "Constructor parameter '${variable.simpleName}' is specified using a generic" +
                    " type which is not supported by Magnet. Use a non-parameterized class or interface" +
                    " type instead. To inject current scope instance, use 'Scope' parameter type.")
            throw BreakGenerationException()
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
                throw BreakGenerationException()
            }

            val upperBounds = paramTypeName.upperBounds
            if (upperBounds.size > 1) {
                env.reportError(element,
                    "Only single upper bounds class parameter is supported," +
                        " for example List<${upperBounds[0]}>")
                throw BreakGenerationException()
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

}
