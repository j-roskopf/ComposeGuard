# Compose Guard

![Maven Central Version](https://img.shields.io/maven-central/v/com.joetr.compose.guard/com.joetr.compose.guard.gradle.plugin)

A gradle plugin for detecting regressions in Jetpack Compose:
* New restartable but not skippable @Composables are added
* New unstable classes are added (only triggers if they are used as a @Composable parameter)
* New @dynamic properties are added
* TODO - New unstable parameters are added to a @Composable

Adds 3 tasks:
* `<variant>ComposeCompilerGenerate` (example `releaseComposeCompilerGenerate`)
  - Generate golden compose metrics to compare against
* `<variant>ComposeCompilerCheck` (example `releaseComposeCompilerCheck`)
  - Generates new metrics and compares against golden values
* `composeCompilerClean`
  - Deletes all compiler metrics

## Adding To Your Project

Available via Maven Central

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

## Adding To Your Project

In your root build file

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

## Binary Compatibility Validator

This project uses [this](https://github.com/Kotlin/binary-compatibility-validator) tool to ensure the public binary API wasn't changed in a way that makes it binary incompatible.

The tool allows dumping binary API of a JVM part of a Kotlin library that is public in the sense of Kotlin visibilities.

To generate a new binary dump, run `./gradlew apiDump` in the root of the project.