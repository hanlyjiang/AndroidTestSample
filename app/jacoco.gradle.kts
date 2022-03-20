//plugins {
//    id("com.android.application")
//}
apply(plugin = "jacoco")
apply(plugin = "com.android.application")


logger.lifecycle("project is: $project")


project.apply {
    project.plugins.hasPlugin("com.android.application")
//    project.extensions.configure("android") {
//
//    }
}
//android {
//    buildTypes {
//        getByName("debug").apply {
//            isTestCoverageEnabled = true
//        }
//    }
//}
//
//afterEvaluate {
//    tasks.getByName("testDebugUnitTest").apply {
//        (this.extensions.getByName("jacoco") as JacocoTaskExtension).apply {
//            logger.lifecycle("testDebugUnitTest dest: ${this.destinationFile?.absolutePath}")
//        }
//    }
//    tasks.withType(org.gradle.api.tasks.testing.Test::class.java) {
//        logger.lifecycle("-> test task : $name")
//    }
//    tasks.withType(JacocoReport::class.java) {
//        logger.lifecycle("-> JacocoReport task : $name")
//    }
//    tasks.withType(com.android.build.gradle.internal.coverage.JacocoReportTask::class.java) {
//        logger.lifecycle("-> JacocoReportTask task : $name")
//        doFirst {
//            logger.lifecycle("-> JacocoReportTask task : $name , ${jacocoConnectedTestsCoverageDir.get()})}")
//        }
//    }
//    android.applicationVariants.forEach { variant ->
//        if (variant.buildType.isTestCoverageEnabled) {
//            val variantCapName = variant.name.capitalize();
//            tasks.register(
//                "jacocoTest${variantCapName}UnitTestReport",
//                JacocoReport::class.java
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
//            tasks.register("mergedJacoco${variantCapName}TestReport", JacocoReport::class.java) {
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
//            tasks.register("mergeJacoco${variantCapName}Execution", JacocoMerge::class.java) {
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