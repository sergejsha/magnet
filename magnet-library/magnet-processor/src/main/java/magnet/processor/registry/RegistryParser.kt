package magnet.processor.registry

import com.squareup.javapoet.ClassName
import magnet.internal.Index
import magnet.internal.InstanceFactory
import magnet.internal.ScopeFactory
import magnet.processor.MagnetProcessorEnv
import magnet.processor.isOfAnnotationType
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.SimpleAnnotationValueVisitor6

class RegistryParser(env: MagnetProcessorEnv) {

    private val extractor = IndexAnnotationExtractor(env.elements)

    fun parse(element: PackageElement): Model.Registry {

        val instanceFactories = mutableListOf<Model.InstanceFactory>()
        val scopeFactories = mutableListOf<Model.ScopeFactory>()

        val factoryIndexElements = element.enclosedElements ?: emptyList()
        for (factoryIndexElement in factoryIndexElements) {
            factoryIndexElement.annotationValues { factoryType, factoryClass, instanceType, classifier ->
                when {
                    factoryType.isOfType(InstanceFactory::class.java) ->
                        instanceFactories.add(
                            Model.InstanceFactory(
                                factoryClass = factoryClass,
                                instanceType = instanceType,
                                classifier = classifier
                            )
                        )
                    factoryType.isOfType(ScopeFactory::class.java) ->
                        scopeFactories.add(
                            Model.ScopeFactory(
                                factoryClass = factoryClass,
                                instanceType = instanceType
                            )
                        )
                }
            }
        }

        return Model.Registry(
            instanceFactories = instanceFactories,
            scopeFactories = scopeFactories
        )
    }

    private inline fun AnnotatedConstruct.annotationValues(block: (
        factoryType: ClassName,
        factoryClass: ClassName,
        instanceType: ClassName,
        classifier: String
    ) -> Unit) {

        var factoryType: TypeElement? = null
        var factoryClass: TypeElement? = null
        var instanceType: String? = null
        var classifier: String? = null

        for (annotationMirror in annotationMirrors) {
            if (annotationMirror.isOfAnnotationType<Index>()) {
                for (entry in annotationMirror.elementValues.entries) {
                    val entryName = entry.key.simpleName.toString()
                    val entryValue = entry.value
                    when (entryName) {
                        "factoryType" -> factoryType = extractor.getTypeElement(entryValue)
                        "factoryClass" -> factoryClass = extractor.getTypeElement(entryValue)
                        "instanceType" -> instanceType = extractor.getStringValue(entryValue)
                        "classifier" -> classifier = extractor.getStringValue(entryValue)
                    }
                }
                break
            }
        }

        block(
            ClassName.get(requireNotNull(factoryType)),
            ClassName.get(requireNotNull(factoryClass)),
            ClassName.bestGuess(requireNotNull(instanceType)),
            requireNotNull(classifier)
        )
    }

}

private fun ClassName.isOfType(type: Class<*>): Boolean =
    packageName() == type.`package`.name && simpleName() == type.simpleName

private class IndexAnnotationExtractor(
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
        value.accept(this, null)
        return this.value as String
    }

    fun getTypeElement(value: AnnotationValue): TypeElement {
        value.accept(this, null)
        return this.value as TypeElement
    }

}
