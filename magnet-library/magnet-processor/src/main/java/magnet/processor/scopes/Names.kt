package magnet.processor.scopes

fun Model.Scope.getGeneratedScopeImplementationName(): String = "MagnetInstance${this.name}"