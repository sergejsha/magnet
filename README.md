[![Build Status](https://travis-ci.org/beworker/magnet.svg?branch=master)](https://travis-ci.org/beworker/magnet)[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.2.41-blue.svg)](http://kotlinlang.org/)[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Magnet

Magnet is a minimalistic dependency injection and dependency inversion framework for Android and Java. If you like the idea of grouping objects in hierarchical scopes, automatic transitive dependency injection and declarative configuration, then Magnet can be a good choice for you.

Magnet implements annotation processor which analyses your code and generates easy-to-read and easy-to-debug factories for your implementation classes. At the same time Magnet allows building modular applications, where dependencies can be injected dynamically at runtime (see dependency inversion). This dynamic behavior comes with its costs - Magnet cannot fully ensure consistency of the dependency-graph of your application at compile time. Nevertheless it does as much as possible at compile time and only the rest gets checked at runtime. If you prefer fully statical graph binding at compile time and you can resign on dynamic modularization and dependency inversion, you should rather go for Dagger2. You will also loose Magnet's simplicity, but the choice is yours.

Magnet does not use reflection for objects creation. It generates and uses factories instead. By doing this Magnet stays fast and easy to debug. It also provides a very simple DSL when used with Kotlin. Magnet classes are well documented and covered by unit tests.

# Design
Magnet has a very minimalistic, almost naive, design. It deals with two concepts - `scopes` and `implementations`. The whole design can be described by just three simple rules:

1. `Scopes` are containers for `implementation` instances.
2. `Scopes` can build up hierarchies.
3. `Implementations` can depend on each other.

# Getting started
1. Write and annotate your implementation classes.

```kotlin
Repository.kt

interface Repository {
    fun getHelloMessage(): String
}

@Implementation(type = Repository::class)
inner class DefaultRepository(): Repository {
    override fun getHelloMessage() = "Hello Magnet!"
}
```

```kotlin
Presenter.kt

@Implementation(type = Presenter::class)
class Presenter(private val repository: Repository) {

    fun presentHelloMessage() {
        println(repository.getHelloMessage())
    }
    
}
```

2. Create scope and inject objects.

```kotlin
val root = Magnet.createScope()
val presenter = root.getSingle<Presenter>()

presenter.presentHelloMessage()
```

Magnet will create `Presenter` and  `Repository` objects for you. 

# Documentation

Additional information and examples can be found in Javadoc.

# Gradle

Kotlin
```gradle
dependencies {
    api "de.halfbit:magnet-kotlin:2.0-RC2"
    kapt "de.halfbit:magnet-processor:2.0-RC2"
}
```

Java
```gradle
dependencies {
    api 'de.halfbit:magnet:2.0-RC2'
    annotationProcessor 'de.halfbit:magnet-processor:2.0-RC2'
}
```

# License
```
Copyright 2018 Sergej Shafarenka, www.halfbit.de

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[1]: docs/images/why-diagram.png
[2]: docs/images/how-diagram.png
[3]: https://en.wikipedia.org/wiki/Dependency_inversion_principle
[4]: https://en.wikipedia.org/wiki/SOLID_(object-oriented_design)
