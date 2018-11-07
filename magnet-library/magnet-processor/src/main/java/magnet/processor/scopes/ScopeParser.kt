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

package magnet.processor.scopes

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName
import magnet.Classifier
import magnet.Scope
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.CommonModel
import magnet.processor.common.CompilationException
import magnet.processor.common.ValidationException
import magnet.processor.common.eachAnnotationAttributeOf
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
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
        scopeVisitor.startType(element)

        var interfaces = element.interfaces
        while (interfaces.isNotEmpty()) {
            interfaces = visitInterfaces(interfaces)
        }

        return scopeVisitor.createScope()
    }

    private fun visitInterfaces(interfaces: List<TypeMirror>): List<TypeMirror> {
        if (interfaces.isNotEmpty()) {
            val moreInterfaces = mutableListOf<TypeMirror>()
            for (superTypeMirror in interfaces) {
                env.types.asElement(superTypeMirror)?.let { superElement ->
                    if (superElement is TypeElement) {
                        if (superElement.getAnnotation(Scope::class.java) != null) {
                            throw ValidationException(
                                element = superElement,
                                message = "Scope cannot inherit from another scope."
                            )
                        }
                        scopeVisitor.continueType(superElement)
                        moreInterfaces.addAll(superElement.interfaces)
                    }
                }
            }
            return moreInterfaces
        }
        return emptyList()
    }

}

private class ScopeVisitor : ElementScanner6<Unit, Unit>() {

    private val bindMethods = mutableListOf<Model.BindMethod>()
    private val getterMethods = mutableListOf<Model.GetterMethod>()
    private var createSubscopeMethod: Model.CreateSubscopeMethod? = null

    private var methodBuilder: MethodBuilder? = null
    private var scopeType: ClassName? = null

    override fun visitType(e: TypeElement, p: Unit?) {
        if (scopeType == null) {
            scopeType = when (val type = TypeName.get(e.asType())) {
                is ClassName -> {
                    if (e.kind != ElementKind.INTERFACE) throw ValidationException(
                        element = e,
                        message = "Scope must be declared as an interface."
                    )
                    type
                }
                else -> throw ValidationException(
                    element = e,
                    message = "Scope declaration interface must not be parametrized."
                )
            }
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
        methodBuilder?.let {
            val typeMirror = e.asType()
            it.params.add(e.toInstance(typeMirror))
        }
        super.visitVariable(e, p)
    }

    private fun maybeCompleteMethod() {
        methodBuilder?.let { method ->
            when (method.element.returnType.kind) {
                TypeKind.VOID -> bindMethods.add(method.toBindMethod())
                else -> {
                    if (method.name == "createSubscope" && method.params.size == 1) {
                        createSubscopeMethod = method.toCreateSubscopeMethod()
                    } else {
                        getterMethods.add(method.toGetterMethod())
                    }
                }
            }
            methodBuilder = null
        }
    }

    fun createScope(): Model.Scope {
        maybeCompleteMethod()
        return Model.Scope(
            type = requireNotNull(scopeType),
            bindMethods = bindMethods.toList(),
            getterMethods = getterMethods.toList(),
            createSubscopeMethod = createSubscopeMethod
        )
    }

    fun startType(element: TypeElement) {
        methodBuilder = null
        scopeType = null
        bindMethods.clear()
        getterMethods.clear()
        element.accept(this, null)
    }

    fun continueType(element: Element) {
        element.accept(this, null)
    }

}

private class MethodBuilder(
    val element: ExecutableElement,
    val name: String,
    val params: MutableList<CommonModel.Instance> = mutableListOf()
) {

    fun toGetterMethod(): Model.GetterMethod {
        if (params.isNotEmpty()) {
            throw ValidationException(
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
        return Model.BindMethod(
            name = name,
            instance = params.getOrNull(0)
                ?: throw ValidationException(
                    element = element,
                    message = "Binder method must have exactly one parameter."
                )
        )
    }

    fun toCreateSubscopeMethod(): Model.CreateSubscopeMethod {
        return Model.CreateSubscopeMethod()
    }

}

private fun Element.toInstance(typeMirror: TypeMirror): CommonModel.Instance {
    val typeName = TypeName.get(typeMirror)
    val name = simpleName.toString()
    return when (typeName) {

        TypeName.VOID -> throw CompilationException(
            element = this,
            message = "Returning type must not be void."
        )

        is ParameterizedTypeName -> {
            if (typeName.rawType.reflectionName() == List::class.java.name) {
                val parameterType = typeName.typeArguments.getOrNull(0)
                    ?: throw CompilationException(
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
        throw ValidationException(element,
            "Magnet supports single upper bounds class parameter only," +
                " while lower bounds class parameter was found.")
    }
    if (upperBounds.size > 1) {
        throw ValidationException(element,
            "Magnet supports single upper bounds class parameter only," +
                " for example List<${upperBounds[0]}>")
    }
    return upperBounds[0]
}
