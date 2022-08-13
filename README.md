![Build Magnet](https://github.com/sergejsha/magnet/actions/workflows/android.yml/badge.svg)
![](https://img.shields.io/badge/production-ready-brightgreen.svg)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.6.21-blue.svg)](http://kotlinlang.org/)
[![Maven Central](http://img.shields.io/maven-central/v/de.halfbit/magnet.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.halfbit%22%20a%3A%22magnet%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# 🧲 Magnet

Magnet is a concise, scope tree based Dependency Injection (DI) library designed for highly modular Android applications. It consists of two parts: an annotation processor (Kotlin) and a reflection free runtime library (Java + Kotlin).

# Design

Magnet defines and opetates on two core concepts: `Scopes` and `Instances`.

<img height="400" src="https://github.com/beworker/magnet/blob/master/documentation/images/readme-diagram.png" />

`Scope` is a container for instances. Scopes can be combined into a hierarchical tree by referencing parent scopes. The most top scope of the tree hierarchy, which has no parent scope, is called the root scope.

`Instance` is a concrete occurance of an injected type. Instances can be allocated in scopes (scoped instances) or outside of scopes (unscoped instances).

# The Dependency Rule

Scopes depend on each other using the strong dependency rule - *scope dependency can only point torwards its parent scope*. The dependency direction between two scopes enforces the direction of dependencies between instances allocated in those scopes. Instances allocated in a parent scope can know nothing about instances allocated in its child scopes. This simple design rule helps preventing memory leaks and allows safe disposal of child scopes and garbage collecting instances allocated there.

# Getting Started

In the example below we will compose a very naive `MediaPlayer` which loads media using a `MediaLoader` and then plays the media.

```kotlin
fun main() {
   val rootScope = MagnetScope.createRootScope()
   val playerScope = rootScope.createSubscope {
      bind(Uri.parse("https://my-media-file"))
   }
   
   // mark 1
   val mediaPlayer = playerScope.getSingle<MediaPlayer>()
   mediaPlayer.playWhenReady()
   
   // mark 2
   Thread.sleep(5000)
   playerScope.dispose()
   
   // mark 3
}

// MediaPlayer.kt
interface MediaPlayer {
   fun playWhenReady()
}

@Instance(type = MediaPlayer::class, disposer = "dispose")
internal class DefaultMediaPlayer(
   private val assetId: Uri,
   private val mediaLoader: MediaLoader
) : MediaPlayer {
   override fun playWhenReady() { ... }
   fun dispose() { ... }
}

// MediaLoader.kt
interface MediaLoader {
   fun load(mediaUri: Uri): Single<Media>
}

@Instance(type = MediaLoader::class)
internal class DefaultMediaLoader() : MediaLoader {
   override fun load(mediaUri: Uri): Single<Media> { ... }
}
```

The diagram below shows how Magnet manages the scope hierarchy when different marks of the main function are reached.

At `Mark 1`, two scopes are created and the `Uri` instance gets bound into the `playerScope`.

<img width="450" src="https://github.com/beworker/magnet/blob/master/documentation/images/readme-mark1.png" />

At `Mark 2`, `mediaPlayer` and `mediaLoader` instances get allocated in respective scopes. `mediaPlayer` is allocated in the `playerScope` because one of its dependencies, the `Uri`,  is located in `playerScope`. Magnet cannot move `mediaPlayer` up to the `rootScope` because this would break the dependency rule described above. `mediaLoader` has no dependencies, that's why it is allocated in the `rootScope`. This instance allocation logic is specific to Magnet DI and is called auto-scoping. See developer documentation for more detail.

<img width="450" src="https://github.com/beworker/magnet/blob/master/documentation/images/readme-mark2.png" />

At `Mark 3`, the `playerScope` gets disposed and all its instances are garbage collected.

<img width="450" src="https://github.com/beworker/magnet/blob/master/documentation/images/readme-mark3.png" />

For more information refer to Magnet documentation.

# Documentation

1. [Developer Guide](https://www.halfbit.de/magnet/developer-guide/)
2. [Dependency auto-scoping](https://github.com/beworker/magnet/wiki/Dependency-auto-scoping)
3. [Scope Inspection](https://github.com/beworker/magnet/wiki/Scope-Inspection)
4. [How to Inject Android ViewModels](https://github.com/beworker/magnet/issues/69#issuecomment-468033997)
5. [Blog: Magnet - an alternative to Dagger](https://www.thomaskeller.biz/blog/2019/10/09/magnet-an-alternative-to-dagger/)
6. [Co2Monitor sample app](https://github.com/beworker/co2monitor/tree/master/android-client)
7. [Another sample app](https://github.com/beworker/g1)

# Features

- Minimalistic API
- Auto-scoping of instances
- Hierarchical, disposable scopes
- Kotlin friendly annotation
- Injection into Kotlin constructors with default arguments
- Injection from binary libraries
- Dependency inversion
- No direct references to Magnet generated code
- No reflection for injection, apt generated factory classes
- Extensible - some `magnetx` extensions are available
- Customizable - custom factories and instance selectors

# Why Magnet?

Magnet was crafted with simplicity and development speed in mind. It lets developers spend less time on DI configuration and do more other stuff, also more mistakes when used inattentively. Magnet motivates you writing highly modular apps because it makes DI so simple. It can even inject instances from the libraries added in build scripts without necessity to adapt source code. Magnet could be interesting for those, who needs an easy to configure and simple DI with more runtime control.

# Why not Magnet?

If compile time consistency validation is your highest priority, I recommend using awesome [Dagger2](https://github.com/google/dagger) instead. You will spend slightly more time on DI configuration but Dagger2 lets you keep it highly consistent and error prone (in most cases) very early in the development cycle - at compile time.

Peace ✌️ and have fun.

# Gradle

Kotlin

```gradle
repositories {
   mavenCentral()
}
dependencies {
   api 'de.halfbit:magnet-kotlin:<version>'
   kapt 'de.halfbit:magnet-processor:<version>'
}
```

Java

```gradle
repositories {
   mavenCentral()
}
dependencies {
   api 'de.halfbit:magnet:<version>'
   annotationProcessor 'de.halfbit:magnet-processor:<version>'
}
```

# Compatibility

Kotlin Version | Magnet Version
-----------|-----------
| 1.7.x | 3.7 (snapshot) |
| 1.6.x | 3.6 |
| 1.5.x | 3.5 |
| 1.4.x | 3.4 |
| 1.3.x | 3.4 |

# Proguard & R8

```
-keep class magnet.internal.MagnetIndexer { *; }
```

# Build from Sources

1. Set JAVA_HOME variable to JDK 11.
2. Import project into Android Studio.
3. Set Gradle → Gradle Settings... → Gradle JDK to JDK 11.
4. To build the project run `./gradlew build`
5. To release the project run `./gradlew publish`

# Maven repository configurations

Repository | Configuration
------------|--------------
Central  | mavenCentral()
Snapshot | maven { url = "https://oss.sonatype.org/content/repositories/snapshots/" }
Local    | mavenLocal()

# License

```
Copyright 2018-2022 Sergej Shafarenka, www.halfbit.de

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
