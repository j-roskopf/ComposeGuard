# Compose Guard

A gradle plugin for detecting regressions in Jetpack Compose:
* New restartable but not skippable composables are added
* New unstable classes are added

Adds 3 tasks:
* `<variant>ComposeCompilerGenerate` (example `releaseComposeCompilerGenerate`)
  - Generate golden compose metrics to compare against
* `<variant>ComposeCompilerCheck` (example `releaseComposeCompilerCheck`)
  - Generates new metrics and compares against golden values
* `composeCompilerClean`
  - Deletes all compiler metrics

## Signing locally

This is required to test.

* Install gnupg - https://formulae.brew.sh/formula/gnupg
* Generate a key - https://central.sonatype.org/publish/requirements/gpg/#installing-gnupg
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

TODO
* https://github.com/Kotlin/binary-compatibility-validator
* explicitApi()
* detekt