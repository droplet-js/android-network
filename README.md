# android-network

### json

````
源码摘自 'org.json:json:20180813'
````

### okhttp3

1. [issues#4597](https://github.com/square/okhttp/issues/4597)
2. [java8-support](https://developer.android.com/studio/write/java8-support)

````
jdk >= JavaVersion.VERSION_1_8
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

### release(feature)

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