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

package magnet.processor.common

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.SimpleAnnotationValueVisitor6

inline fun <reified T> AnnotationMirror.isOfAnnotationType(): Boolean =
    this.annotationType.toString() == T::class.java.name

inline fun <reified A> Element.eachAnnotationAttributeOf(
    block: (name: String, value: AnnotationValue) -> Unit
) {
    for (annotationMirror in annotationMirrors) {
        if (annotationMirror.isOfAnnotationType<A>()) {
            for (entry in annotationMirror.elementValues.entries) {
                block(entry.key.simpleName.toString(), entry.value)
            }
        }
    }
}

inline fun Element.eachAnnotationAttribute(
    block: (name: String, value: AnnotationValue) -> Unit
) {
    for (annotationMirror in annotationMirrors) {
        for (entry in annotationMirror.elementValues.entries) {
            block(entry.key.simpleName.toString(), entry.value)
        }
    }
}

class ValidationException(val element: Element, message: String) : Throwable(message)
class CompilationException(val element: Element, message: String) : Throwable(message)

class AnnotationValueExtractor(
    private val elements: Elements
) : SimpleAnnotationValueVisitor6<Void?, Void>() {

    private var value: Any? = null

    override fun visitString(s: String, p: Void?): Void? {
        value = s
        return p
    }

    override fun visitType(t: TypeMirror, p: Void?): Void? {
        value = elements.getTypeElement(t.toString())
        return p
    }

    fun getStringValue(value: AnnotationValue): String {
        this.value = null
        value.accept(this, null)
        return this.value as String
    }

    fun getTypeElement(value: AnnotationValue): TypeElement {
        this.value = null
        value.accept(this, null)
        return this.value as TypeElement
    }

}
