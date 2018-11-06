package magnet.processor.scopes

import com.squareup.javapoet.ClassName
import magnet.processor.common.CommonModel

class Model private constructor() {

    class Scope(
        val type: ClassName,
        val bindMethods: List<BindMethod>,
        val getterMethods: List<GetterMethod>,
        val createSubscopeMethod: CreateSubscopeMethod?
    ) {

        val name: String get() = type.simpleName()
        val packageName: String get() = type.packageName()

        fun accept(visitor: Visitor) {
            visitor.visitScope(this)
            for (method in bindMethods) {
                visitor.visitBindMethod(method)
            }
            for (method in getterMethods) {
                visitor.visitGetterMethod(method)
            }
            createSubscopeMethod?.let {
                visitor.visitCreateSubscopeMethod(it)
            }
        }
    }

    class BindMethod(
        val name: String,
        val instance: CommonModel.Instance
    )

    class GetterMethod(
        val name: String,
        val instance: CommonModel.Instance
    )

    class CreateSubscopeMethod

    interface Visitor {
        fun visitScope(scope: Scope) {}
        fun visitBindMethod(method: BindMethod) {}
        fun visitGetterMethod(method: GetterMethod) {}
        fun visitCreateSubscopeMethod(method: CreateSubscopeMethod) {}
    }

}
