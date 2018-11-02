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

package magnet.processor

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

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

class UnexpectedCompilationError(val element: Element, override val message: String) : Throwable(message)
class CompilationError(val element: Element, override val message: String) : Throwable(message)
