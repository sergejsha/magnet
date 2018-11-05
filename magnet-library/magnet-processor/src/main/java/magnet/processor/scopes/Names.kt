package magnet.processor.scopes

fun Model.Scope.getGeneratedScopeImplementationName(): String = "Magnet${this.name}Implementation"
fun Model.Scope.getGeneratedScopeFactoryName(): String = "Magnet${this.name}Factory"