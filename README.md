[![Build Status](https://travis-ci.org/beworker/magnet.svg?branch=master)](https://travis-ci.org/beworker/magnet)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.2.71-blue.svg)](http://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

<img src="https://halfbit.de/images/magnet/scopes.png" width="80" />
<hr1> 

Magnet is a concise dependency injection and [dependency inversion][1] library for Android, designed for highly modular applications. Magnet operates on hierarchical dependency scopes where a child scope extends its parent scope by keeping a reference to it.

<img src="documentation/images/scopes.png" width="680" />

An instance is typically injected within a scope. Instance can depend on other instances whithin the same or a parent scope. Magnet will take care for injecting the instance in that scope, which is absolutely required for satisfying all required dependencies. Thus you don't have to declare any modules and componets, and then bind them together. You just define dependencies in your class constructor, optionally declare how to scope the instance and Magnet will do the rest. 

Here is an slightly simplified example of how Magnet would build Dagger's coffe maker.

```kotlin
@Instance(type = Pump::class)
class Thermosiphon(private val heater: Heater) : Pump

@Instance(type = Heater::class)
class ElectricHeater(): Heater

@Instance(type = CoffeMaker::class)
class CoffeeMaker(
   private val heater: Heater,
   private val pump: Pump
)

val scope = Magnet.createScope()
val coffeMaker: CoffeeMaker = scope.getSingle()
```

 This desing makes dependency injection so easy that it becomes hard not to use it.

# Documentation

1. [Developer Guide](https://www.halfbit.de/magnet/developer-guide/)
2. [Dependency Inversion][1]
3. [Hierarchical Scopes][2]

# Gradle

Kotlin
```gradle
dependencies {
    api 'de.halfbit:magnet-kotlin:2.5'
    kapt 'de.halfbit:magnet-processor:2.5'
}
```

Java
```gradle
dependencies {
    api 'de.halfbit:magnet:2.5'
    annotationProcessor 'de.halfbit:magnet-processor:2.5'
}
```

# Proguard & R8
```proguard 
-keep class magnet.internal.MagnetIndexer { *; }
```

# Support

Magnet is provided for free, without any support. If you consider using Magnet in your commercial product and you need support or training, feel free to <a href="mailto:info@halfbit.de?subject=Magnet,%20Technical%20support">contact me</a>.

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
