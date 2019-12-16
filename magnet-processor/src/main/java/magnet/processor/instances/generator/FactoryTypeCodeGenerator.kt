/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package magnet.processor.instances.generator

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import magnet.internal.Generated
import magnet.internal.InstanceFactory
import magnet.processor.instances.CreateMethod
import magnet.processor.instances.FactoryType
import magnet.processor.instances.FactoryTypeVisitor
import magnet.processor.instances.GetLimitMethod
import magnet.processor.instances.GetScopingMethod
import magnet.processor.instances.GetSelectorMethod
import magnet.processor.instances.GetSiblingTypesMethod
import magnet.processor.instances.MethodParameter
import magnet.processor.instances.aspects.limitedto.GetLimitMethodGenerator
import magnet.processor.instances.aspects.selector.GetSelectorMethodGenerator
import magnet.processor.instances.aspects.disposer.DisposeMethodGenerator
import magnet.processor.instances.aspects.disposer.IsDisposableMethodGenerator
import magnet.processor.instances.aspects.factory.CreateMethodGenerator
import magnet.processor.instances.aspects.factory.DefaultCreateMethodGenerator
import magnet.processor.instances.aspects.scoping.GetScopingMethodGenerator
import magnet.processor.instances.aspects.siblings.GetSiblingTypesMethodGenerator
import javax.lang.model.element.Modifier

interface AspectGenerator {
    fun generate(classBuilder: TypeSpec.Builder)
    fun reset()
}

internal class Aspect<out G : AspectGenerator>(
    private val generator: G
) {
    private var visited: Boolean = false

    inline fun visit(block: G.() -> Unit) {
        block(generator)
        visited = true
    }

    fun generate(classBuilder: TypeSpec.Builder) {
        if (visited) {
            generator.generate(classBuilder)
        }
        generator.reset()
        visited = false
    }
}

class FactoryTypeCodeGenerator : FactoryTypeVisitor, CodeGenerator {

    private var factoryTypeSpec: TypeSpec? = null
    private var factoryClassName: ClassName? = null
    private var generateGettersInCreateMethod = false

    private val aspectGetSiblingTypes = Aspect(GetSiblingTypesMethodGenerator())
    private val aspectGetScoping = Aspect(GetScopingMethodGenerator())
    private val aspectGetLimit = Aspect(GetLimitMethodGenerator())
    private val aspectGetSelector = Aspect(GetSelectorMethodGenerator())
    private val createMethodGenerator: CreateMethodGenerator = DefaultCreateMethodGenerator()
    private val isDisposableMethodGenerator = IsDisposableMethodGenerator()
    private val disposeMethodGenerator = DisposeMethodGenerator()

    override fun enterFactoryClass(factoryType: FactoryType) {
        generateGettersInCreateMethod = factoryType.customFactoryType == null
        createMethodGenerator.visitFactoryClass(factoryType)
        isDisposableMethodGenerator.visitFactoryClass(factoryType)
        disposeMethodGenerator.visitFactoryClass(factoryType)
    }

    override fun enterCreateMethod(createMethod: CreateMethod) {
        factoryTypeSpec = null
        createMethodGenerator.enterCreateMethod(createMethod)
    }

    override fun visitCreateMethodParameter(parameter: MethodParameter) {
        createMethodGenerator.visitCreateMethodParameter(parameter)
    }

    override fun exitCreateMethod(createMethod: CreateMethod) {
        createMethodGenerator.exitCreateMethod()
    }

    override fun visit(method: GetScopingMethod) {
        aspectGetScoping.visit { visit(method) }
    }

    override fun visit(method: GetLimitMethod) {
        aspectGetLimit.visit { visit(method) }
    }

    override fun enterSiblingTypesMethod(method: GetSiblingTypesMethod) {
        aspectGetSiblingTypes.visit { enterSiblingTypesMethod(method) }
    }

    override fun visitSiblingType(type: ClassName) {
        aspectGetSiblingTypes.visit { visitSiblingType(type) }
    }

    override fun exitSiblingTypesMethod(method: GetSiblingTypesMethod) {
        aspectGetSiblingTypes.visit { exitSiblingTypesMethod() }
    }

    override fun enterGetSelectorMethod(method: GetSelectorMethod) {
        aspectGetSelector.visit { enterGetSelectorMethod(method) }
    }

    override fun visitSelectorArgument(argument: String) {
        aspectGetSelector.visit { visitSelectorArgument(argument) }
    }

    override fun exitGetSelectorMethod(method: GetSelectorMethod) {
        aspectGetSelector.visit { exitGetSelectorMethod() }
    }

    override fun exitFactoryClass(factory: FactoryType) {

        factoryClassName = factory.factoryType

        val classBuilder: TypeSpec.Builder = TypeSpec
            .classBuilder(factoryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(Generated::class.java)
            .superclass(generateFactorySuperInterface(factory))

        createMethodGenerator.generate(classBuilder)

        aspectGetScoping.generate(classBuilder)
        aspectGetLimit.generate(classBuilder)
        aspectGetSiblingTypes.generate(classBuilder)
        aspectGetSelector.generate(classBuilder)
        isDisposableMethodGenerator.generate(classBuilder)
        disposeMethodGenerator.generate(classBuilder)

        classBuilder
            .addMethod(generateGetTypeMethod(factory))

        factoryTypeSpec = classBuilder.build()
    }

    private fun generateGetTypeMethod(factoryType: FactoryType): MethodSpec {
        return MethodSpec
            .methodBuilder("getType")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(Class::class.java)
            .addStatement("return \$T.class", factoryType.interfaceType)
            .build()
    }

    private fun generateFactorySuperInterface(factoryType: FactoryType): TypeName {
        return ParameterizedTypeName.get(
            ClassName.get(InstanceFactory::class.java),
            factoryType.interfaceType
        )
    }

    override fun generateFrom(factoryType: FactoryType): CodeWriter {
        factoryType.accept(this)
        return CodeWriter(this.factoryClassName!!.packageName(), factoryTypeSpec!!)
    }
}
