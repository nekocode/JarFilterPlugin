# README
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://jitpack.io/v/nekocode/JarFilterPlugin.svg)](https://jitpack.io/#nekocode/JarFilterPlugin)

This plugin can filter files (such as class files) inside a jar. This is very useful when you want to modify some classes in a third-party library but do not want to download and import all of its source code. Just copy the source files you want to modify into your project. And then use this plugin to remove the corresponding class in the jar. Finally, the build tool will package the compiled class of your copied source into the archive.

You can see the [example](example) to learn how to use it. In addition, this plugin supports incremental work. So its performance is good.

## Intergation

Intergate this gralde plugin:

```gradle
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath "com.github.nekocode:JarFilterPlugin:${lastest-verion}"
    }
}
```

Apply and configure the plugin:

```gralde
apply plugin: 'jar-filter'

jarFilters {
    "com.android.support:appcompat-v7:(.*)" {
        excludes = [
                'android/support/v7/app/AppCompatActivity.class',
                'android/support/v7/app/AppCompatActivity\\$(.*).class'
        ]
    }

    // Local jar
    "android.local.jars:xxx.jar:(.*)" {
        includes = [
                'xxx'
        ]
    }
}
```

## Build

If you want to build in your local. You need to run the following command after cloning.

```sh
touch local first-build && ./gradlew uploadArchives
```