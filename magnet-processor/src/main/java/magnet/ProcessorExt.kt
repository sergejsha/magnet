package magnet

import javax.lang.model.element.AnnotationMirror

inline fun <reified T> AnnotationMirror.mirrors(): Boolean {
    return this.annotationType.toString() == T::class.java.name
}