package magnet

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import magnet.internal.Factory
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

class FactoryGenerator {

    private lateinit var env: MagnetProcessorEnv

    fun generate(implTypeElement: TypeElement, env: MagnetProcessorEnv) {
        this.env = env
        val implClassName = ClassName.get(implTypeElement)

        implTypeElement.annotationMirrors.forEach {
            val extensionClass = it.elementValues.entries.find { "forType" == it.key.simpleName.toString() }?.value

            extensionClass?.let {
                val implTypeClassName = ClassName.bestGuess(it.value.toString())
                val factoryTypeSpec = generateFactory(implClassName, implTypeClassName, implTypeElement)

                val packageName = implClassName.packageName()
                JavaFile.builder(packageName, factoryTypeSpec)
                        .skipJavaLangImports(true)
                        .build()
                        .writeTo(env.filer)
            }
        }
    }

    private fun generateFactory(
            implClassName: ClassName,
            implTypeClassName: ClassName,
            implTypeElement: TypeElement
    ): TypeSpec {

        val factoryPackage = implClassName.packageName()
        val factoryName = "Magnet${implClassName.simpleName()}Factory"
        val factoryClassName = ClassName.bestGuess("$factoryPackage.$factoryName")

        println("  Generating $factoryClassName")

        val extensionFactorySuperInterface = ParameterizedTypeName.get(
                ClassName.get(Factory::class.java),
                implTypeClassName)

        return TypeSpec
                .classBuilder(factoryClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(extensionFactorySuperInterface)
                //.addField(generateComponentRegistryField())
                //.addMethod(generateConstructor())
                .addMethod(
                        generateCreateMethod(
                                implClassName,
                                implTypeClassName,
                                implTypeElement
                        )
                )
                .build()
    }

    private fun generateCreateMethod(
            implClassName: ClassName,
            implTypeClassName: ClassName,
            implTypeElement: TypeElement
    ): MethodSpec {
        val dependencyScopeClassName = ClassName.get(DependencyScope::class.java)

        // We have following cases:
        // 1. No parameters -> empty constructor
        // 2. One or many parameters -> DependencyScope used "as is" others are required() from scope

        val constructors = ElementFilter.constructorsIn(implTypeElement.enclosedElements)
        if (constructors.size != 1) {
            env.reportError(implTypeElement, "Exactly one constructor is required for $implTypeElement")
            throw BreakGenerationException()
        }

        val parameters = constructors[0].parameters
        val codeBlockBuilder = CodeBlock.builder()
        val methodParamsBuilder = StringBuilder()

        parameters.forEach {

            val isDependencyScopeParam = it.asType().toString() == DependencyScope::class.java.name
            val paramName = if (isDependencyScopeParam) "dependencyScope" else it.simpleName.toString()

            if (!isDependencyScopeParam) {
                val paramClassName = ClassName.get(it.asType())

                val nullableAnnotation = it.annotationMirrors.find {
                    it.toString().endsWith(".Nullable")
                }

                val getMethodName = if (nullableAnnotation != null) "get" else "require"

                codeBlockBuilder.addStatement(
                        "\$T $paramName = dependencyScope.$getMethodName(\$T.class)",
                        paramClassName,
                        paramClassName
                )
            }

            methodParamsBuilder.append(paramName).append(", ")
        }
        if (methodParamsBuilder.isNotEmpty()) {
            methodParamsBuilder.setLength(methodParamsBuilder.length - 2)
        }

        return MethodSpec
                .methodBuilder("create")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec
                        .builder(dependencyScopeClassName, "dependencyScope")
                        .build())
                .returns(implTypeClassName)
                .addCode(codeBlockBuilder.build())
                .addStatement("return new \$T($methodParamsBuilder)", implClassName)
                .build()
    }

}