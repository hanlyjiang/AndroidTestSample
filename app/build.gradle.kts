import com.github.hanlyjiang.gradle_helper.PropertiesUtils.getBoolProperties
import com.github.hanlyjiang.gradle_helper.PropertiesUtils.writeLocalProperties

plugins {
    id("com.android.application")
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

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
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

apply(from = "../jacoco.gradle")
