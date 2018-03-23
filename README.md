[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.halfbit/magnet/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.halfbit/magnet/)
[![Build Status](https://travis-ci.org/beworker/magnet.svg?branch=master)](https://travis-ci.org/beworker/magnet)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.2.30-blue.svg)](http://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Magnet

<img src="docs/images/logo.png" width="112">

Dependency inversion library for Android.

[Dependency inversion principle][3] helps to decouple high-level modules from the low-level module implementation details and completes [SOLID object-oriented design][4]. If you are about to modularize your Android application and apply dependency inversion principle, then Magnet library would be a good fit for your project as it makes dependency inversion a fun task.

# Why?
Let's compare traditional layered design to the design based on dependency inversion principle. As an example we take an application consisting of a navigation bar with three tabbed pages.
![Why diagram][1]

Dependency inversion principle requires us to design our application for extension right away instead of adding feature-specific code here and there uncontrolled, by spreading it all over the application as the application grows. Needless to say how more extensible, testable and maintainable our application becomes when it is designed for extension.

Another advantage of structuring the application in such a modular way is the ability to repackage the app according to new requirements. For instance we want to create a new companion app which uses some of the already existing modules or we want to create an Android Instant app. If we did everything right, we will be able to repackage some of existing modules into the new app and write missing ones.

# How?
As the name of the principle says we need to invert dependencies. If we simply invert dependencies then our library modules will depend on the application module. At the same time application module has to dependend on the library modules because it has to include them into the apk. We run into a circular dependencies situation which is not allowed in gradle builds. To resolve this circular dependency we have to avoid any dependency onto the application module. It means none of the library modules may depend on the application module. This would be only possible if our application module would have no code or resources the other modules need. This brings us to the design depicted below.

![How diagram][2]

Application module becomes nearly empty and has the single role - it assembles all modules together into a single apk. Any features our application has, have to be moved to the library modules. 

Our sample application defines `app-main` library module which can host tabbed pages. We make it extensible by exposing `Page` interface to its extension. Then we create thee more library modules which extend `app-main` module by providing `HomePage`, `DashboardPage` and `NotificationsPage` implementations of `Page` interface. 

Now we can ask Magnet to put all these pieces together.
1. We add `@Implementation` annotation to the implementations of `Page` interface. This will allow Magnet to find those implementations at build-time.
2. In `MainActivity.onCreate()` method in `app-main` module we ask Magnet to instantiate all available implementations of the `Page` interface at runtime.
3. Last but not least, we create an empty marker interface inside the `app` module and annotate it with `@MagnetizeImplementations`. This will force Magnet to collect and index all implementations registred in the app.

Sample application located in this repo implements exactly the logic described above. You can build, run and debug it. The fun part is, that now you can remove any of the implementation modules from the build by simply commenting out respective `implementation` dependency inside the `build.gradle` file of the `sample-app` module. Just rerun the app and commented out page will disappear. Have fun and happy coding!

# Gradle build

Kotlin:
```gradle
dependencies {
    api "de.halfbit:magnet-kotlin:0.0.6"
    kapt "de.halfbit:magnet-processor:0.0.6"
}
```

Java:
```gradle
dependencies {
    api 'de.halfbit:magnet:0.0.6'
    annotationProcessor 'de.halfbit:magnet-processor:0.0.6'
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
