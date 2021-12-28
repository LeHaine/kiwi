# NO LONGER MAINTAINED - SWITCHED IN FAVOR OF [LittleKt](https://github.com/LittleKtOrg/LittleKt)

[![Release](https://jitpack.io/v/com.lehaine/kiwi.svg)](https://jitpack.io/#com.lehaine/kiwi)

## About

My personal library for use with Kotlin Multiplatform and Korge that I call **Kiwi**. Lots of the functionality is based off of Deepnights `gameBase` which I've used extensively (and loved) for awhile but just kotlinized.

### Versioning

The version isn't following semver or some sort of structured versioning. You can grab a specific commit using the commit hash or just use the nightly. See [Install][] section.

### Install

In your `build.gradle.kts`:

Add the **Jitpack** repository:
```Kotlin
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add the dependency:
```Kotlin
	dependencies {
        implementation 'com.lehaine.kiwi:kiwi:${versionOrCommitHash}'
    }
```


If you want the nightly / most recent commit:

```Kotlin
	dependencies {
        implementation 'com.lehaine.kiwi:kiwi:master-SNAPSHOT'
    }
```
