# android-network

[![Build Status](https://cloud.drone.io/api/badges/v7lin/android-network/status.svg)](https://cloud.drone.io/v7lin/android-network)
[![GitHub tag](https://img.shields.io/github/tag/v7lin/android-network.svg)](https://github.com/v7lin/android-network/releases)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

### okhttp3

1. OkHttp适用于Android 5.0+（API级别21+）和Java 8+。
2. [issues#4597](https://github.com/square/okhttp/issues/4597)
3. [java8-support](https://developer.android.com/studio/write/java8-support)

````
jdk >= JavaVersion.VERSION_1_8
android.defaultConfig.minSdkVersion >= 21
````

### snapshot

````
ext {
    latestVersion = '1.0.2-SNAPSHOT'
}

allprojects {
    repositories {
        ...
        maven {
            url 'https://oss.jfrog.org/artifactory/oss-snapshot-local'
        }
        ...
    }
}
````

### release

````
ext {
    latestVersion = '1.0.2'
}

allprojects {
    repositories {
        ...
        jcenter()
        ...
    }
}
````

### usage

java
````
...
dependencies {
    ...
    implementation "io.github.v7lin:okhttp3:${latestVersion}"
    ...
}
...
sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8
...
````

android
````
...
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    ...
}
...
dependencies {
    ...
//    implementation "io.github.v7lin:okhttp3:${latestVersion}"
    implementation "io.github.v7lin:okhttp3-android:${latestVersion}"
    ...
}
...
````

### example

[android example](./app/src/main/java/io/github/v7lin/network/MainActivity.java)
