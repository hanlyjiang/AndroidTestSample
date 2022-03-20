plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    // gradle plugin api
    `java-gradle-plugin`
    // plugin publish
    id("com.gradle.plugin-publish") version "0.15.0"
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly("com.android.tools.build:gradle:7.1.2")
    // https://mvnrepository.com/artifact/com.android.tools.build/gradle
//    implementation 'com.android.tools.build:gradle:7.1.2'
//    compileOnly("com.android.tools.build:gradle:4.1.3")
}

// 用于指定所有的group
group = "com.github.hanlyjiang"
// 用于指定当前仓库本身的版本
// 多个插件分别指定插件的版本在本地测试时会有插件仓库自身和插件的版本匹配问题，我们在这里统一指定
// 问题在于：
// 1. 插件本身并不包括jar，仅仅是通过pom指向这个插件仓库的jar，如gradlePlugins.jar;
// 2. 打包插件时及引入插件时，我们都不能单独为 gradlePlugins.jar 指定版本；
// 3. 如果几个插件版本不同，指向的插件仓库的jar就会有问题；
version = "0.0.3"


// 多个插件时，gradlePlugin需要放置到pluginBundle前面，避免出现pluginBundle中找不到插件config的情况
gradlePlugin {
    plugins {
        create("android-jacoco-config") {
            id = "com.github.hanlyjiang.android-jacoco-config"
            implementationClass = "com.hanlyjiang.github.gradle.plugin.jacoco.AndroidJacocoConfigPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/hanlyjiang/AndroidTestSample"
    vcsUrl = "https://github.com/hanlyjiang/AndroidTestSample.git"
    //单个插件时，使用这里的定义
    tags = listOf("android", "library", "maven")

    // 定义多个插件时需要
    // ref: https://plugins.gradle.org/docs/publish-plugin
    (plugins){
        "android-jacoco-config" {
            displayName = "android-jacoco-config plugin"
            description = "Plugin for simplify android-jacoco-config"
            tags = listOf("android", "library", "jacoco")
        }
    }
}


publishing {
    repositories {
        maven {
            name = "projectLocalPluginRepo"
            url = uri(File(rootProject.rootDir, "local-maven-repo/plugins"))
        }
    }
}