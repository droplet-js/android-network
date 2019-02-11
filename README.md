# android-network

### json

````
源码摘自 'org.json:json:20180813'
````

### okhttp3

````

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
implementation "io.github.v7lin:okhttp3:${latestVersion}"
````

android
````
implementation "io.github.v7lin:okhttp3:${latestVersion}"
implementation "io.github.v7lin:okhttp3-android:${latestVersion}"
````

### example

[android example](./app/src/main/java/io/github/v7lin/network/MainActivity.java)