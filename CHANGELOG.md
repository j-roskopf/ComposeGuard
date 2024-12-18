# Changelog

# 2.4.0

* Allow running compose guard and KSP tasks of different flavors at the same time

# 2.3.8

* Add `assumeRuntimeStabilityAsUnstable` under check extension. See README.md for more info

# 2.3.6

* Fix configuration cache issue

# 2.3.5

* Don't configure `KotlinJvmCompile` if `configureKotlinTasks` is disabled
* Check for write / check task in start parameters before setting output up to date with kotlin compile task

# 2.3.4

* Internal updates. Should have no impact to consumers.

# 2.3.3

* Add opt in flag for reporting all errors on check task
```kts
composeGuardCheck {
    reportAllOnMissingBaseline = true // defaults to false
}
```

# 2.3.0

* Move out of alpha versioning (this version is no different from 2.2.7-alpha)

# 2.2.7-alpha

* Added support for `configureKotlinTasks`

# 2.2.3-alpha

* Multiplatform fixes
* Better --configuration-cache support

# 2.0.0

* Multiplatform support

# 1.1.0

* Allow configuration of checks

# 1.0.0

* Initial Release