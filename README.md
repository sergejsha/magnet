# Magnet
Dependency inversion library for Android.

[Dependency inversion principle][3] helps to decouple high-level modules from the low-level module implementation details and completes [SOLID object-oriented desing][4]. If you are about to modularize your Android application and apply dependency inversion prinsible, then Magnet library would be a good fit for your project as it makes dependency inversion with ease.

# Why?
Let's compare traditional layered design with the desing powered by dependency inversion pronciple.
![Why diagram][1]

Dependency inversion principle requires us to desing our application for extension right away instead of adding feature-specific code here and there spreading it all over the application as the application grows. Needless to say how better extensible, testable and maintainable our appllication becomes when it is desined for extension.

Another advantage of structuring the application in such a modular way is the ability to repackage the app according to new requirements. For instance we want to create a new companion app which uses some of already existing modules or we want to create an Android Instant app. If we did everything right, we will be able to repackage some existing modules into the new app and write missing ones.

# How?
![How diagram][2]

# I am sold!

Kotlin:
```groovy
dependencies {
    implementation "de.halfbit:magnet:0.0.1"
    kapt "de.halfbit:magnet-processor:0.0.1"
}
```

Java:
```groovy
dependencies {
    implementation 'de.halfbit:magnet:0.0.1'
    annotationProcessor 'de.halfbit:magnet-processor:0.0.1'
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

[1]: docs/images/how-diagram.png
[2]: docs/images/why-diagram.png
[3]: https://en.wikipedia.org/wiki/Dependency_inversion_principle
[4]: https://en.wikipedia.org/wiki/SOLID_(object-oriented_design)
