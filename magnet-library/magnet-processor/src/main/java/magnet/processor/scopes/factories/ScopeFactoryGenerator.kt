package magnet.processor.scopes.factories

import magnet.processor.scopes.Generator

internal class ScopeFactoryGenerator : Generator(
    classGenerator = ScopeFactoryClassGenerator()
) {

    init {
        registerAspect(CreateMethodGenerator())
        registerAspect(GetTypeMethodGenerator())
    }

}
