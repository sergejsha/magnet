[![Build Status](https://travis-ci.org/beworker/magnet.svg?branch=master)](https://travis-ci.org/beworker/magnet)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.2.50-blue.svg)](http://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

<img src="https://halfbit.de/images/magnet/magnet-logo.png" width="80" />
<hr1> 

Magnet is a minimalist dependency injection and [dependency inversion][1] library for Android helping to write modular applications. If you want a fast, stable, non-intrusive and easy-to-configure dependency injection framework, then Magnet is a good choice for you.

Magnet does **not** use reflection for object creation. It is based on annotation processor which prepares and partly validates dependency graph of your application at build-time. Final creation and validation of the dependency graph happens at runtime. This deliberate design decision esures a fair balance between fully statical and fully dynamic dependency injection. Properly designed Magnet-application allows adding and removing application functionality by adding and removing modules in application's build script whithout any additional programming effort. In that respect Magnet enforces modular design and helps modules be developed and tested in isolation.

Magnet is well documented and covered by unit tests.

# Design
Magnet has a very minimalist, almost naive, design. It deals with just two concepts - `Scopes` and `Instances`. The whole design can be described by four simple statements:

1. `Scopes` are containers for object `Instances`.
2. `Scopes` can build up hierarchies.
3. `Instances` can be put into (bound) and taken from `Scopes`.
4. `Instances` can depend on other instances.

<img src="documentation/images/design-diagram.png" width="480" />

# Documentation

1. [Developer Guide](https://www.halfbit.de/magnet/developer-guide/)
2. [Dependency inversion][1]
3. [Dependency auto-scoping][2]

# Support

Magnet is provided for free, without any support. If you consider using Magnet in your commercial product and you need support or training, feel free to <a href="mailto:info@halfbit.de?subject=Magnet, technical support">contact me</a>.

# Gradle

Kotlin
```gradle
dependencies {
    api "de.halfbit:magnet-kotlin:2.2"
    kapt "de.halfbit:magnet-processor:2.2"
}
```

Java
```gradle
dependencies {
    api 'de.halfbit:magnet:2.2'
    annotationProcessor 'de.halfbit:magnet-processor:2.2'
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
