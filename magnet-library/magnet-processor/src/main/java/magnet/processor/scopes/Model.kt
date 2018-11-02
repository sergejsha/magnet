package magnet.processor.scopes

import com.squareup.javapoet.ClassName
import magnet.processor.common.CommonModel

interface Model {

    class Scope(
        val type: ClassName,
        val bindParentScopeMethod: BindMethod?,
        val bindMethods: List<BindMethod>,
        val getterMethods: List<GetterMethod>
    ) {

        val name: String get() = type.simpleName()
        val packageName: String get() = type.packageName()

        fun accept(visitor: Visitor) {
            visitor.visitScope(this)
            bindParentScopeMethod?.let {
                visitor.visitBindParentScope(bindParentScopeMethod)
            }
            for (method in bindMethods) {
                visitor.visitBindMethod(method)
            }
            for (method in getterMethods) {
                visitor.visitGetterMethod(method)
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

    interface Visitor {
        fun visitScope(scope: Scope) {}
        fun visitBindParentScope(method: BindMethod) {}
        fun visitBindMethod(method: BindMethod) {}
        fun visitGetterMethod(method: GetterMethod) {}
    }

}
