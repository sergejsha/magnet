package magnet.processor.factory

import com.squareup.javapoet.ClassName
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/** Awesome static factory method parser. */
internal class FactoryFromMethodAnnotationParser(
    env: MagnetProcessorEnv
) : AnnotationParser<ExecutableElement>(env, false) {

    override fun parse(element: ExecutableElement): List<FactoryType> {

        if (!element.modifiers.contains(Modifier.STATIC)) {
            throw env.compilationError(element, "Method annotated"
                + " by ${Instance::class.java} must be 'static'")
        }

        if (element.modifiers.contains(Modifier.PRIVATE)) {
            throw env.compilationError(element, "Method annotated"
                + " by ${Instance::class.java} must not be 'private'")
        }

        val annotation = parseAnnotation(element)
        val staticMethodReturnType = element.returnType

        for (type in annotation.types) {
            if (type.toString() != staticMethodReturnType.toString()) {
                throw env.compilationError(element, "Method must return instance"
                    + " of ${type.reflectionName()} as declared"
                    + " by ${Instance::class.java}")
            }
        }

        val staticMethodClassName = ClassName.get(element.enclosingElement as TypeElement)
        val staticMethodName = element.simpleName.toString()
        val uniqueFactoryNameBuilder = StringBuilder()
            .append(staticMethodClassName.packageName())
            .append('.')
            .append(staticMethodClassName.simpleName().capitalize())
            .append(staticMethodName.capitalize())

        val methodParameters = mutableListOf<MethodParameter>()
        element.parameters.forEach { variable ->
            val methodParameter = parseMethodParameter(element, variable)
            methodParameters.add(methodParameter)
            uniqueFactoryNameBuilder.append(methodParameter.name.capitalize())
        }

        val instanceFullName = uniqueFactoryNameBuilder.toString()
        return annotation.types.map {

            val getSiblingTypesMethod = if (annotation.types.size == 1) null
            else GetSiblingTypesMethod(annotation.types - it)

            val factoryFullName = generateFactoryName(annotation, instanceFullName, it)
            FactoryType(
                element = element,
                type = it,
                classifier = annotation.classifier,
                scoping = annotation.scoping,
                disabled = annotation.disabled,
                factoryType = ClassName.bestGuess(factoryFullName),
                createStatement = MethodCreateStatement(staticMethodClassName, staticMethodName),
                createMethod = CreateMethod(methodParameters),
                getScopingMethod = GetScopingMethod(annotation.scoping),
                getSiblingTypesMethod = getSiblingTypesMethod
            )
        }
    }

}
