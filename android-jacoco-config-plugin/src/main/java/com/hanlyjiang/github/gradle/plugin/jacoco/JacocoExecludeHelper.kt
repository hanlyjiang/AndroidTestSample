package com.hanlyjiang.github.gradle.plugin.jacoco

import org.gradle.api.Project
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object JacocoExecludeHelper {
    fun loadJacocoExcludes(project: Project): Collection<String> {
        val androidDataBindingExcludes = listOf(
            "android/databinding/**/*.class",
            "**/android/databinding/*Binding.class",
            "**/databinding/*Binding.class",
            "**/databinding/*BindingImpl.class",
            "**/databinding/*Sw600dpImpl.class",
            "**/DataBinder*.class",
            "**/DataBindingTriggerClass.class",
            "**/BR.*"
        )

        val androidExcludes = listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
        )

        val butterKnifeExcludes = listOf(
            "**/*\$ViewInjector*.*",
            "**/*\$ViewBinder*.*"
        )

        val dagger2Excludes = listOf(
            "**/*_MembersInjector.class",
            "**/*_Factory.class",
            "**/Dagger*Component.class",
            "**/Dagger*Component$*.class",
            "**/Dagger*Component\$Builder.class",
            "**/*Module_*Factory.class",
            // dagger-android
            "**/*_ContributeModule_*.*"
        )

        val aRouterExcludes = listOf(
            "com/alibaba/android/arouter/routes/**"
        )

        val file = File("${project.projectDir}/jacoco-excludes")
        val excludeList = ArrayList<String>()
        if (file.exists()) {
            project.logger.debug("${file.absolutePath} found!")
            try {
                with(BufferedReader(FileReader(file))) {
                    var line: String?
                    do {
                        line = readLine()
                        line?.run {
                            if (isEmpty()) {
                                return@run
                            }
                            if (line.trim().startsWith("#")) {
                                excludeList.add(line.trim() + "")
                            }
                        }
                    } while (
                        line != null
                    )
                }
                excludeList.forEach {
                    project.logger.debug("add exclude: $it")
                }
            } catch (ignored: Exception) {
                project.logger.error("add exclude failed! + ${ignored.message}")
            }
        } else {
            project.logger.warn("${file.absolutePath} not found!")
        }

        return androidDataBindingExcludes +
                androidExcludes +
                butterKnifeExcludes +
                dagger2Excludes +
                aRouterExcludes +
                excludeList
    }
}