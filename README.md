<div align="center">
  <img src="assets/compose_guard_icon.png" width="512">
  <h1>Compose Guard</h1>
</div>

<p align="center">
  <a href="https://central.sonatype.com/namespace/com.joetr.compose.guard"><img alt="Maveb Central" src="https://img.shields.io/maven-central/v/com.joetr.compose.guard/com.joetr.compose.guard.gradle.plugin"/></a>
  <a href="https://opensource.org/license/mit/"><img alt="License" src="https://img.shields.io/badge/License-MIT-blue.svg"/></a>
  <a href="https://github.com/j-roskopf/ComposeGuard/actions/workflows/release.yml"><img alt="Release Workflow" src="https://github.com/j-roskopf/ComposeGuard/actions/workflows/release.yml/badge.svg"/></a>
  <a href="https://androidweekly.net/issues/issue-624"><img alt="Android Weekly" src="https://img.shields.io/badge/News-Android_Weekly_%23624-palevioletred?logo=android"/></a>
  <a href="https://blog.canopas.com/android-stack-weekly-issue-126-e892cc8bf543"><img alt="Android Stack Weekly" src="https://img.shields.io/badge/News-Android_Stack_Weekly_%23126-palevioletred?logo=android"/></a>
  <a href="https://mailchi.mp/kotlinweekly/kotlin-weekly-408"><img alt="Kotlin Weekly" src="https://img.shields.io/badge/News-Kotlin_Weekly_%23408-palevioletred?logo=kotlin"/></a>
  <a href="https://jetc.dev/issues/215#github-j-roskopf--composeguard"><img alt="Jetpack Compose Newsletter" src="https://img.shields.io/badge/News-Jetpack_Compose_Newsletter_%23215-palevioletred?logo=jetpackcompose"/></a>
  <a href="https://jetc.dev/issues/216#compose-guard-detecting-regressions-in-jetpack-compose"><img alt="Jetpack Compose Newsletter" src="https://img.shields.io/badge/News-Jetpack_Compose_Newsletter_%23216-palevioletred?logo=jetpackcompose"/></a>
</p><br>


A gradle plugin for detecting regressions in Jetpack Compose / Compose Multiplatform:
* New restartable but not skippable @Composables are added
* New unstable classes are added (only triggers if they are used as a @Composable parameter)
* New @dynamic properties are added
* New unstable parameters are added to a @Composable

In an Android project, Compose Guard adds 3 tasks:
* `<variant>ComposeCompilerGenerate` (example `./gradlew releaseComposeCompilerGenerate`)
  - Generate golden compose metrics to compare against
* `<variant>ComposeCompilerCheck` (example `./gradlew releaseComposeCompilerCheck`)
  - Generates new metrics and compares against golden values
* `./gradlew composeCompilerClean`
  - Deletes all compiler metrics

In a Multiplatform project, Compose Guard adds the same 2 `Check` and `Generate` tasks (as well as a root `composeCompilerClean` task) for each supported target following the pattern `<target><variant if applicable>ComposeCompilerGenerate`
* `<target><variant>ComposeCompilerGenerate` 
  - Examples: `./gradlew androidReleaseComposeCompilerGenerate`, `./gradlew jvmComposeCompilerGenerate`, ` ./gradlew iosArm64ComposeCompilerGenerate`, `./gradlew jsComposeCompilerGenerate`, `./gradlew wasmJsComposeCompilerGenerate`
  - Generate golden compose metrics to compare against
* `<target><variant>ComposeCompilerCheck` 
  - Examples: `./gradlew androidReleaseComposeCompilerCheck`, `./gradlew jvmComposeCompilerCheck`, `./gradlew iosArm64ComposeCompilerCheck`, `./gradlew jsComposeCompilerCheck`, `./gradlew wasmJsComposeCompilerCheck`
  - Generates new metrics and compares against golden values
* `./gradlew composeCompilerClean`
  - Deletes all compiler metrics

## Platforms
![](https://img.shields.io/badge/Android-black.svg?style=for-the-badge&logo=android) | ![](https://img.shields.io/badge/iOS-black.svg?style=for-the-badge&logo=apple) | ![](https://img.shields.io/badge/Desktop-black.svg?style=for-the-badge&logo=apple) | ![](https://img.shields.io/badge/Web-black.svg?style=for-the-badge&logo=google-chrome)
:----: | :----: |:----------------------------------------------------------------------------------:| :----:
✅ | ✅ |                                         ✅                                          | ✅


## Adding To Your Project

Available via Maven Central - ![Maven Central Version](https://img.shields.io/maven-central/v/com.joetr.compose.guard/com.joetr.compose.guard.gradle.plugin)

In your root build file:

```kotlin
plugins {
    id("com.joetr.compose.guard") version "<latest version>" apply false
}
```

In any module you want to apply checks:

```kotlin
plugins {
    id("com.joetr.compose.guard")
}
```

## Configuring Compose Guard

Each check that is performed has the ability to be turned off in case it is not useful to you.

```kts
composeGuardCheck {
    errorOnNewDynamicProperties = false // defaults to true
    errorOnNewRestartableButNotSkippableComposables = false // defaults to true
    errorOnNewUnstableClasses = false // defaults to true
    errorOnNewUnstableParams = false // defaults to true
    /**
     * In strong skipping mode (https://developer.android.com/develop/ui/compose/performance/stability/strongskipping)
     * you may not care about new unstable params if the composable is already skippable
     */
    ignoreUnstableParamsOnSkippableComposables = true // defaults to false
}
```

The output directory of the golden metrics has the ability to be configured as well.

```kotlin
composeGuardGenerate {
    outputDirectory = layout.projectDirectory.dir("custom_dir").asFile
}
```

Additionally, if you would prefer complete control over how the metrics are generated, you can disable this plugin from configuring the Kotlin compile task.
This is intended advanced users that prefer compiler performance over easy baseline generation.
With this set, the plugin no longer configures anything in the Kotlin compile task. 
So an end user would be responsible for generating the golden metrics and the check metrics before running the check task.

```kotlin
composeGuard { 
    configureKotlinTasks = false // defaults to true
}
```

## Signing locally

This is required to test.

* Install gnupg - https://formulae.brew.sh/formula/gnupg
* Generate a key - https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair
  * `gpg --full-generate-key` 
* List keys and grab newly generated key (40 digits)
  * `gpg --list-keys`
* `gpg --export-secret-keys THE_KEY_THAT_YOU_JUST_GENERATED > composeguard.gpg`
* Modify your gradle home `gradle.properties` with the following:
```
signing.keyId=LAST_8_DIGITS_OF_KEY
signing.password=PASSWORD_USED_TO_GENERATE_KEY
signing.secretKeyRingFile=/Users/YOURUSERNAME/.gnupg/composeguard.gpg (or wherever you stored the keyring you generated earlier)
```

## Binary Compatibility Validator

This project uses [this](https://github.com/Kotlin/binary-compatibility-validator) tool to ensure the public binary API wasn't changed in a way that makes it binary incompatible.

The tool allows dumping binary API of a JVM part of a Kotlin library that is public in the sense of Kotlin visibilities.

To generate a new binary dump, run `./gradlew apiDump` in the root of the project.


