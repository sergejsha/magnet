package magnet.processor.scopes

fun Model.Scope.getGeneratedScopeImplementationName(): String = "MagnetInstance${this.name}"
fun Model.Scope.getGeneratedScopeFactoryName(): String = "${this.name}MagnetFactory"