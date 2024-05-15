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