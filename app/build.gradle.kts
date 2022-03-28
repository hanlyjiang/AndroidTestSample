import com.github.hanlyjiang.gradle_helper.PropertiesUtils.getBoolProperties
import com.github.hanlyjiang.gradle_helper.PropertiesUtils.writeLocalProperties

plugins {
    id("com.android.application")
    // 使用 plugin 替换手动配置，需要AGP 7.0 及以上
    id("io.github.hanlyjiang.android-jacoco-config") version ("0.0.3")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.github.hanlyjiang.sample"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

}

val useAARPropName = "useAar"
val customGroupName = "custom"

dependencies {

    val useAar = getBoolProperties(project, useAARPropName, false)
    if (useAar) {
        logger.log(LogLevel.LIFECYCLE, "---> use Aar=$useAar")
        // 使用此种方式引入会有问题
        implementation("", name = "lib-mod-release", ext = "aar")
        // 使用此种方式正常
        // implementation(files("../libs/lib-mod-release.aar"))
    } else {
        logger.log(LogLevel.LIFECYCLE, "---> use Project=${!useAar}")
        implementation(project(":lib-mod"))
    }

    // https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava
    implementation("io.reactivex.rxjava3:rxjava:3.1.3")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    val mockitoVersion = "3.3.3"
    val powerMockitoVersion = "2.0.9"
    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.powermock:powermock-api-mockito2:$powerMockitoVersion")
    testImplementation("org.powermock:powermock-api-junit4:$powerMockitoVersion")

    // Android Test Others
    // 架构组件测试
    androidTestImplementation("android.arch.core:core-testing:1.1.1")
    // Mockito-android
    androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
    // Android test and test-ext
    val androidTestVersion = "1.4.0"
    val extTruthVersion = "1.4.0"
    val extJunitVersion = "1.1.3"
    androidTestImplementation("androidx.test:core:$androidTestVersion")
    androidTestImplementation("androidx.test:runner:$androidTestVersion")
    androidTestImplementation("androidx.test:rules:$androidTestVersion")
    androidTestImplementation("androidx.test.ext:truth:$extTruthVersion")
    androidTestImplementation("androidx.test.ext:junit:$extJunitVersion")
    androidTestImplementation("androidx.test.ext:junit-ktx:$extJunitVersion")
    // Espresso dependencies
    val espressoVersion = "3.4.0"
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-web:$espressoVersion")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:$espressoVersion")
    // The following Espresso dependency can be either "implementation"
    // or "androidTestImplementation", depending on whether you want the
    // dependency to appear on your APK's compile classpath or the test APK
    // classpath.
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:$espressoVersion")
}

tasks.create("setUseAAR") {
    group = customGroupName
    doLast {
        writeLocalProperties(project, useAARPropName, "true");
    }
}

tasks.create("unSetUseAAR") {
    group = customGroupName
    doLast {
        writeLocalProperties(project, useAARPropName, "false");
    }
}

afterEvaluate {
    android.applicationVariants.forEach { variant ->
        val groupName = customGroupName
        val variantName = variant.name.capitalize()
        val libProjectName = "lib-mod"
        val appProjectName = "app"
        tasks.findByName("pre${variantName}Build")?.apply {
            group = groupName
            dependsOn(
                ":$libProjectName:copy${variantName}AarToApp",
            )
        }
        tasks.create("runAsAarDependencies${variantName}").apply {
            group = groupName
            dependsOn(
//                ":$libProjectName:copy${variantName}AarToApp",
                ":$appProjectName:assemble${variantName}",
            )
            doLast {
                println("runAsAarDependencies${variantName}")
            }
            finalizedBy(":$appProjectName:install${variantName}")
        }
    }

}


android {
    buildTypes {
        getByName("debug").apply {
            isTestCoverageEnabled = true
        }
    }
}


/*
* 手动配置
**/
//apply(from = "../jacoco.gradle")

//apply(plugin = "jacoco")
//
//afterEvaluate {
//    tasks.getByName("testDebugUnitTest").apply {
//        this.extensions.getByName<JacocoTaskExtension>("jacoco").apply {
//            logger.lifecycle("testDebugUnitTest dest: ${this.destinationFile?.absolutePath}")
//        }
//    }
//    tasks.withType<Test> {
//        logger.lifecycle("-> test task : $name")
//    }
//    tasks.withType<JacocoReport> {
//        logger.lifecycle("-> JacocoReport task : $name")
//    }
//    tasks.withType<com.android.build.gradle.internal.coverage.JacocoReportTask> {
//        logger.lifecycle("-> JacocoReportTask task : $name")
//        doFirst {
//            logger.lifecycle("-> JacocoReportTask task : $name , ${jacocoConnectedTestsCoverageDir.get()})}")
//        }
//    }
//    android.applicationVariants.forEach { variant ->
//        if (variant.buildType.isTestCoverageEnabled) {
//            val variantCapName = variant.name.capitalize();
//            tasks.register<JacocoReport>(
//                "jacocoTest${variantCapName}UnitTestReport"
//            ) {
//                group = "jacoco"
//                description = "Generate jacoco report for test${variantCapName}UnitTest"
//                executionData(tasks.getByName("test${variantCapName}UnitTest"))
//                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
//                classDirectories.from(variant.javaCompileProvider.get().destinationDirectory)
//                dependsOn("test${variantCapName}UnitTest")
//                doLast {
//                    logger.lifecycle("Jacoco Report: " + reports.html.outputLocation.asFile.get().absolutePath)
//                }
//            }
//
//            tasks.register<JacocoReport>("mergedJacoco${variantCapName}TestReport") {
//                group = "jacoco"
//                description =
//                    "Generate merged jacoco report for test${variantCapName}UnitTest and create${variantCapName}AndroidTestCoverageReport"
//                executionData(
//                    tasks.getByName("test${variantCapName}UnitTest"),
//                )
//                val androidCoverageTask =
//                    tasks.getByName("create${variantCapName}AndroidTestCoverageReport")
//                if (androidCoverageTask is com.android.build.gradle.internal.coverage.JacocoReportTask) {
//                    executionData(androidCoverageTask.jacocoConnectedTestsCoverageDir.asFileTree)
//                }
//                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
//                classDirectories.from(variant.javaCompileProvider.get().destinationDirectory)
//                dependsOn(
//                    "test${variantCapName}UnitTest",
//                    "create${variantCapName}AndroidTestCoverageReport"
//                )
//                doLast {
//                    logger.lifecycle("Jacoco Report: " + reports.html.outputLocation.asFile.get().absolutePath)
//                }
//            }
//
//            tasks.register<JacocoMerge>("mergeJacoco${variantCapName}Execution") {
//                group = "jacoco"
//                description =
//                    "Generate merged jacoco execution for test${variantCapName}UnitTest and create${variantCapName}AndroidTestCoverageReport"
//                executionData(
//                    tasks.getByName("test${variantCapName}UnitTest"),
//                )
//                val androidCoverageTask =
//                    tasks.getByName("create${variantCapName}AndroidTestCoverageReport")
//                if (androidCoverageTask is com.android.build.gradle.internal.coverage.JacocoReportTask) {
//                    executionData(androidCoverageTask.jacocoConnectedTestsCoverageDir.asFileTree)
//                }
//                doLast {
//                    logger.lifecycle("Jacoco Execution: " + destinationFile.absolutePath)
//                }
//            }
//        }
//    }
//}

