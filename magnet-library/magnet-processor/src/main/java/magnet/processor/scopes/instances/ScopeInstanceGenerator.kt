package magnet.processor.scopes.instances

import magnet.processor.scopes.Generator

internal class ScopeInstanceGenerator : Generator(
    classGenerator = ScopeClassGenerator()
) {

    init {
        registerAspect(ConstructorGenerator())
        registerAspect(GetterMethodsGenerator())
        registerAspect(BinderMethodsGenerator())
        registerAspect(CreateSubscopeGenerator())
    }

}
