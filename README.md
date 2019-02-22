# okhttp3 && okhttp3-android

[![Build Status](https://cloud.drone.io/api/badges/v7lin/android-network/status.svg)](https://cloud.drone.io/v7lin/android-network)
[ ![Download](https://api.bintray.com/packages/v7lin/maven/okhttp3/images/download.svg) ](https://bintray.com/v7lin/maven/okhttp3/_latestVersion)
[ ![Download](https://api.bintray.com/packages/v7lin/maven/okhttp3-android/images/download.svg) ](https://bintray.com/v7lin/maven/okhttp3-android/_latestVersion)

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
    latestVersion = '1.0.0-SNAPSHOT'
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
    latestVersion = '1.0.0'
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
