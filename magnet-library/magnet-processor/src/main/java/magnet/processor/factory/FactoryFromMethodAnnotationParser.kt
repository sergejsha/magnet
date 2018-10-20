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
) : AnnotationParser(env) {

    fun parse(element: ExecutableElement): FactoryType {

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

        if (annotation.type.toString() != staticMethodReturnType.toString()) {
            throw env.compilationError(element, "Method must return instance"
                + " of ${annotation.type.reflectionName()} as declared"
                + " by ${Instance::class.java}")
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

        uniqueFactoryNameBuilder.append("MagnetFactory")

        return FactoryType(
            element,
            annotation,
            ClassName.bestGuess(uniqueFactoryNameBuilder.toString()),
            MethodCreateStatement(staticMethodClassName, staticMethodName),
            CreateMethod(methodParameters),
            GetScopingMethod(annotation.scoping),
            GetSiblingTypesMethod(null)
        )
    }

}
