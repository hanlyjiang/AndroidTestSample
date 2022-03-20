package com.hanlyjiang.github.gradle.plugin.jacoco

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.coverage.JacocoReportTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoMerge
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.*

/**
 * AndroidJacocoConfigPlugin
 * @author hanlyjiang
 * @version 1.0
 */
class AndroidJacocoConfigPlugin : Plugin<Project> {

    private var jacocoExcludes: MutableCollection<String> = mutableListOf<String>()

    companion object {
        const val GROUP_NAME = "jacoco"
    }

    override fun apply(target: Project) {
        target.run {
            if (!isAndroidProject(this)) {
                logger.warn("Not android project!!!")
                return
            }
            pluginManager.apply("jacoco")
            afterEvaluate {
//                setIsTestCoverageEnabled(it)
                jacocoExcludes.addAll(JacocoExecludeHelper.loadJacocoExcludes(project))
                getAppVariants()?.forEach { variant ->
                    registerTasks(variant)
                }
                getLibVariants()?.forEach {
                    registerTasks(it)
                }
                tasks.create("jacocoExcludes").apply {
                    group = GROUP_NAME
                    description = "print jacoco exclude rules"
                    doLast {
                        for (item in jacocoExcludes) {
                            project.logger.lifecycle(item)
                        }
                    }
                }
            }
        }
    }

    private fun Project.registerTasks(variant: BaseVariant) {
        if (variant.buildType.isTestCoverageEnabled) {
            val variantCapName =
                variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() };
            tasks.create(
                "jacocoTest${variantCapName}UnitTestReport", JacocoReport::class.java
            ).apply {
                group = GROUP_NAME
                description = "Generate jacoco report for test${variantCapName}UnitTest"
                executionData(tasks.getByName("test${variantCapName}UnitTest"))
                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
                classDirectories.from(
                    project.fileTree(variant.javaCompileProvider.get().destinationDirectory).apply {
                        setExcludes(jacocoExcludes)
                    })
                dependsOn("test${variantCapName}UnitTest")
                doLast {
                    logger.lifecycle("Jacoco Report: " + reports.html.outputLocation.asFile.get().absolutePath)
                }
            }

            tasks.create(
                "mergedJacoco${variantCapName}TestReport",
                JacocoReport::class.java
            ).apply {
                group = GROUP_NAME
                description =
                    "Generate merged jacoco report for test${variantCapName}UnitTest and create${variantCapName}AndroidTestCoverageReport"
                executionData(
                    tasks.getByName("test${variantCapName}UnitTest"),
                )
                val androidCoverageTask =
                    tasks.getByName("create${variantCapName}AndroidTestCoverageReport")
                if (androidCoverageTask is JacocoReportTask) {
                    executionData(androidCoverageTask.jacocoConnectedTestsCoverageDir.asFileTree)
                }
                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
                classDirectories.from(fileTree(variant.javaCompileProvider.get().destinationDirectory) {
                    it.setExcludes(jacocoExcludes)
                })
                dependsOn(
                    "test${variantCapName}UnitTest",
                    "create${variantCapName}AndroidTestCoverageReport"
                )
                doLast {
                    logger.lifecycle("Jacoco Report: " + reports.html.outputLocation.asFile.get().absolutePath)
                }
            }

            tasks.create(
                "mergeJacoco${variantCapName}Execution",
                JacocoMerge::class.java
            ).apply {
                group = GROUP_NAME
                description =
                    "Generate merged jacoco execution for test${variantCapName}UnitTest and create${variantCapName}AndroidTestCoverageReport"
                executionData(
                    tasks.getByName("test${variantCapName}UnitTest"),
                )
                val androidCoverageTask =
                    tasks.getByName("create${variantCapName}AndroidTestCoverageReport")
                if (androidCoverageTask is JacocoReportTask) {
                    executionData(androidCoverageTask.jacocoConnectedTestsCoverageDir.asFileTree)
                }
                doLast {
                    logger.lifecycle("Jacoco Execution: " + destinationFile.absolutePath)
                }
            }
        }
    }

    private fun isAndroidProject(target: Project): Boolean {
        return target.pluginManager.hasPlugin("com.android.application") ||
                target.pluginManager.hasPlugin("com.android.library")
    }

    private fun setIsTestCoverageEnabled(it: Project) {
        it.run {
            getAndroidExtension().apply {
                buildTypes.getByName("debug").apply {
                    isTestCoverageEnabled = true
                }
            }
        }
    }
}



