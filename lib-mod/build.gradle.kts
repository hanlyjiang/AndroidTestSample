plugins {
    id("com.android.library")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31
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

dependencies {

    // https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava
    implementation("io.reactivex.rxjava3:rxjava:3.1.3")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}


afterEvaluate {
    android.libraryVariants.forEach { variant ->
        val variantName = variant.name
        tasks.create("copy${variantName.capitalize()}AarToApp").apply {
            group = "custom"
            dependsOn(
                ":${project.name}:assemble${variantName.capitalize()}",
            )
            doLast {
                println(name)
                copy {
                    from(
                        File(
                            project.buildDir,
                            "/outputs/aar/${project.name}-${variantName}.aar"
                        ).absolutePath
                    )
                    into(rootProject.rootDir.absolutePath + "/libs/")
                }
            }
        }
    }
}

apply(from = "../jacoco.gradle")