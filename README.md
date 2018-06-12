[![Build Status](https://travis-ci.org/beworker/magnet.svg?branch=master)](https://travis-ci.org/beworker/magnet)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.2.41-blue.svg)](http://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Magnet

Magnet is a minimalistic dependency injection and dependency inversion library for Android helping to write truly modular applications. If you want a fast, stable, non intrusive and easy to configure dependency injection framework supporting automatic transitive dependency injection, then Magnet is a good choice for you.

Magnet implements annotation processor which analyses your code and generates easy-to-read and easy-to-debug factories for your classes. At the same time Magnet allows building modular applications where dependencies can be injected dynamically at runtime (see [dependency inversion][1]). This dynamic behavior comes with its costs - Magnet cannot ensure full consistency of the dependency-graph of your application at compile time like Dagger2 does. Nevertheless it checks as much as possible at compile time and only the rest gets checked at runtime. This gives some good balance between fully statical and fully dynamical dependency injection.

Magnet does *not* use reflection for objects creation. It generates and uses factory classes instead. By doing this Magnet stays fast and easy to debug. It also provides a very simple DSL when used with Kotlin. Magnet classes are well documented and covered by unit tests.

# Design
Magnet has a very minimalistic, almost naive, design. It deals with just two concepts - `Scopes` and `Instances`. The whole design can be described by four simple statements:

1. `Scopes` are containers for object `Instances`.
2. `Scopes` can build up hierarchies.
3. `Instances` can be put into (bound) and taken from `Scopes`.
4. `Instances` can depend on other instances.

<img src="documentation/images/design-diagram.png" width="480" />

# Getting started

1. Initialize Magnet - create an empty marker interface in your main application module and annotate it. This allows Magnet to find all `@Instance`-annotated classes located in either main or library modules, which makes [dependency inversion][1] between modules possible.

```kotlin
@Magnetizer
interface AppMagnetizer
```

2. Write and annotate your implementation classes.

```kotlin
Repository.kt

interface Repository {
    fun getHelloMessage(): String
}

@Instance(type = Repository::class)
internal class DefaultRepository(): Repository {
    override fun getHelloMessage() = "Hello Magnet!"
}
```

```kotlin
Presenter.kt

@Instance(type = Presenter::class)
class Presenter(private val repository: Repository) {
    fun presentHelloMessage() {
        println(repository.getHelloMessage())
    }
}
```

3. Create root scope and create objects.

```kotlin
val root = Magnet.createRootScope()
val presenter = root.getSingle<Presenter>()

presenter.presentHelloMessage()
```

Magnet will create instance of `Presenter` and `Repository` for you. `Repository` instance will be injected into `Presenter`'s constructor automatically.

# Documentation

1. Check out sample application to see Magnet in action.
2. Additional documentation and examples can be found in Javadoc.
3. [Dependency inversion][1]
4. [Dependency auto-scoping][2]

# Support

Magnet is provided for free, without any support. If you consider using Magnet in your commercial product and you need support or training, feel free to <a href="mailto:info@halfbit.de?subject=Magnet, technical support">contact me</a>.

# Gradle

Kotlin
```gradle
dependencies {
    api "de.halfbit:magnet-kotlin:2.0-RC7"
    kapt "de.halfbit:magnet-processor:2.0-RC7"
}
```

Java
```gradle
dependencies {
    api 'de.halfbit:magnet:2.0-RC6'
    annotationProcessor 'de.halfbit:magnet-processor:2.0-RC7'
}
```

# Proguard & R8
```proguard 
-keep class magnet.internal.MagnetIndexer { *; }
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

[1]: https://github.com/beworker/magnet/wiki/Dependency-inversion
[2]: https://github.com/beworker/magnet/wiki/Dependency-auto-scoping
