[![Build Status](https://travis-ci.org/beworker/magnet.svg?branch=master)](https://travis-ci.org/beworker/magnet)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.2.40-blue.svg)](http://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Magnet
<img src="docs/images/logo.png" width="100" />

[Dependency inversion principle][3] helps to decouple high-level modules from the low-level module implementation details and completes [SOLID object-oriented design][4]. If you are about to modularize your Android application and apply dependency inversion principle, then Magnet library would be a good fit for your project as it makes dependency inversion a fun task.

# Release 2.0 (candidate)

Magnet 2.0 adds new advanced injection capabilities. If you are mainly interested in dependency inversion feature, use version 1.0.0 of the library. Here is a short description of how version 2.0 can be used with Android.

1. Create the root scope for your objects inside of you `Application`. Created scope has same lifespan as your application.

```kotlin
val appScope = Magnet.createScope()
```

2. Inside of your `Activity` create another scope with a shorter lifespan, corresponding to the lifespan of your activity. Lets create and initialize it with an instance of `Resources` which will reside inside this scope.

```kotlin
val activityScope = App.appScope.createSubscope {
    bind(resources)
}
```

Created scope holds a reference to its parent scope which is `appScope`.

3. Lets create a thirt empty scope with a `Fragment` lifespan.

```kotlin
val fragmentScope = activityScope.createSubscope()
```

`fragmentScope` holds reference to `activityScop`. Any object created in `fragmentScope` can depend on the objects registered in the same scope or any parent scope up to the root scope, meaning in either `activityScope` or `appScope`.

Our scope setup look like this 
```
appScope ()
     ^
     |
activityScope (resources)
     ^
     |
fragmentScope ()
```

`appScope` and `fragmentScope` are empty and `activityScope` has a single `Resources` object in it.

4. Let's imagine we need to create following instances with dependencies between them.

```
FragmentPresenter -> ItemRepository -> ItemDataSource -> Resources
```

First we need to declare them using `@Implementation` annotation.

```kotlin
@Implementation(type = FragmentPresenter::class, scoping = Scoping.DIRECT)
class FragmentPresenter(
    private val itemRepository: ItemRepository
)

@Implementation(type = ItemRepository::class)
class ItemRepository(
    private val dataSource: ItemsDataSource
)

@Implementation(type = ItemDataSource::class)
class ItemDataSource(
    private val resources: Resources
)
```

Magnet parses constructors of annotated classes and detects dependencies between them.

5. Now we can ask Magnet to provide instance of `FragmentPresenter` class from `fragmentScope` like following.

```kotlin
val fragmentPresenter = fragmentScope.getSingle<FragmentPresenter>()
```

Magnet will resolve dependencies, create objects and bind them into our scopes automatically.

```
appScope ()
     ^
     |
activityScope (itemRepository, itemDataSource, resources)
     ^
     |
fragmentScope (fragmentPresenter)
```

Here is the explanation of how Magnet managed auto-scoping of objects.

a) `fragmentPresenter` was bound to `fragmentScope` because of `scoping = Scoping.DIRECT` directive forcing Magnet to register objects in the same scope, in which they were created. Magnet did it because we called `getSingle()` at `fragmentScope`.

b) `itemDataSource` was bound to `activityScope` because this is the top most scope where its dependency (`resources`) is viaible. This is controlled by `scoping = Scoping.TOPMOST` directive, which is default scoping value. If `resources` would reside inside `appScope`, then Magnet would put `itemDataSource` into `appScope` too.

c) `itemRepository` was bound to `activityScope` because its dependency `itemDataSource` resides inside `activityScope`. Same as case (b).

Auto-scoping and dependency inversion (see description below) are some of the concepts differentiating Magnet from the other injection frameworks. Those concepts are new and they have to be validated by using them in smaller projects first. Feel free to check Magnet 2.0 and share your opinion.

```gradle
dependencies {
    api "de.halfbit:magnet-kotlin:2.0-RC1"
    kapt "de.halfbit:magnet-processor:2.0-RC1"
}
```

# Why depenedncy inversion?
Let's compare traditional layered design to the design based on dependency inversion principle. As an example we take an application consisting of a navigation bar with three tabbed pages.
![Why diagram][1]

Dependency inversion principle requires us to design our application for extension right away instead of adding feature-specific code here and there uncontrolled, by spreading it all over the application as the application grows. Needless to say how more extensible, testable and maintainable our application becomes when it is designed for extension.

Another advantage of structuring the application in such a modular way is the ability to repackage the app according to new requirements. For instance we want to create a new companion app which uses some of the already existing modules or we want to create an Android Instant app. If we did everything right, we will be able to repackage some of existing modules into the new app and write missing ones.

# How?
As the name of the principle says we need to invert dependencies. If we simply invert dependencies then our library modules will depend on the application module. At the same time application module has to dependent on the library modules because it has to include them into the apk. We run into a circular dependencies situation which is not allowed in gradle builds. To resolve this circular dependency we have to avoid any dependency onto the application module. It means none of the library modules may depend on the application module. This would be only possible if our application module would have no code or resources the other modules need. This brings us to the design depicted below.

![How diagram][2]

Application module becomes nearly empty and has the single role - it assembles all modules together into a single apk. Any features our application has, have to be moved to the library modules. 

Our sample application defines `app-main` library module which can host tabbed pages. We make it extensible by exposing `Page` interface to its extension. Then we create thee more library modules which extend `app-main` module by providing `HomePage`, `DashboardPage` and `NotificationsPage` implementations of `Page` interface. 

Now we can ask Magnet to put all these pieces together.
1. We add `@Implementation` annotation to the implementations of `Page` interface. This will allow Magnet to find those implementations at build-time.
2. In `MainActivity.onCreate()` method in `app-main` module we ask Magnet to instantiate all available implementations of the `Page` interface at runtime.
3. Last but not least, we create an empty marker interface inside the `app` module and annotate it with `@MagnetizeImplementations`. This will force Magnet to collect and index all implementations registered in the app.

Sample application located in this repo implements exactly the logic described above. You can build, run and debug it. The fun part is, that now you can remove any of the implementation modules from the build by simply commenting out respective `implementation` dependency inside the `build.gradle` file of the `sample-app` module. Just rerun the app and commented out page will disappear. Have fun and happy coding!

# Version 1.0

Kotlin:
```gradle
dependencies {
    api "de.halfbit:magnet-kotlin:1.0.0"
    kapt "de.halfbit:magnet-processor:1.0.0"
}
```

Java:
```gradle
dependencies {
    api 'de.halfbit:magnet:1.0.0'
    annotationProcessor 'de.halfbit:magnet-processor:1.0.0'
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
