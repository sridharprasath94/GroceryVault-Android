# Gradle Rules

Use:
- `libs.versions.toml`
- Version catalogs for ALL dependencies/plugins

Do NOT hardcode dependency versions.

IMPORTANT:

Do NOT add:

```kotlin
alias(libs.plugins.kotlin.android)
```

inside:
`app/build.gradle.kts`

AGP 9.x already bundles Kotlin support.

Required Gradle wrapper:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip
```

Required versions (verified working):

```toml
[versions]
agp = "9.2.1"
kotlin = "2.2.10"
ksp = "2.3.6"
hilt = "2.59.2"
hiltNavigationCompose = "1.2.0"

composeBom = "2026.02.01"

coreKtx = "1.15.0"
activityCompose = "1.10.1"
lifecycle = "2.10.0"
coroutines = "1.9.0"
navigation = "2.9.7"

junit = "4.13.2"
junitExt = "1.3.0"
espresso = "3.7.0"
```
