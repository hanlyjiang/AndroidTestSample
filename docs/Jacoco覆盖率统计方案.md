# Android Jacoco覆盖率统计配置

## 如何让测试任务生成 Jacoco 覆盖率统计数据？

这里我们仅仅从Gradle任务来说，不考虑 AndroidStudio/IDEA。

### 本地单元测试（Test）

对于本地单元测试来说，原先有一个 `testDebugUnitTest` 的测试任务，如果不做配置，该任务只会生成测试通过情况的报告。只要应用 jacoco 插件，然后运行 `testDebugUnitTest` 任务时，就会同时生成jacoco覆盖率统计**执行数据文件**。

```kotlin
// build.gradle 
apply(plugin = "jacoco")
```

之所以能这样是因为 jacoco 插件会给所有 Test 类型的任务添加 jacoco 的配置。

可以通过如下方式输出其执行数据文件路径：

```kotlin
afterEvaluate {
    tasks.getByName("testDebugUnitTest").apply {
        doLast {
            (this.extensions.getByName("jacoco") as JacocoTaskExtension).apply {
                logger.lifecycle("testDebugUnitTest dest: ${this.destinationFile?.absolutePath}")
            }
        }
    }
}
```

执行情况如下：

```shell
testDebugUnitTest dest: /Users/hanlyjiang/Wksp/project/AndroidTestSample/app/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec
```

### 仪器单元测试（AndroidTest）

仪器单元测试覆盖率数据的统计需要打开`testCoverageEnabled`开关，然后会有一个`createDebugCoverageReport` 的任务生成，同时也会生成html的报告。

```groovy
// groovy
android {
    buildTypes {
        debug {
            testCoverageEnabled = true
        }
    }
}

// kotlin
android {
    buildTypes {
        getByName("debug").apply {
            isTestCoverageEnabled = true
        }
    }
}
```

连接设备执行该任务即可生成对应的执行数据文件及对应的覆盖率报告。

通过在build.gradle中添加如下配置可以在执行时输出其执行数据文件在本机的位置：

```kotlin
    tasks.withType(com.android.build.gradle.internal.coverage.JacocoReportTask::class.java) {
        logger.lifecycle("-> JacocoReportTask task : $name")
        doFirst {
            logger.lifecycle("-> JacocoReportTask task : $name , ${jacocoConnectedTestsCoverageDir.get()})}")
        }
    }
```

然后执行`createDebugCoverageReport`, 输出如下 ：

```shell
> Task :app:createDebugAndroidTestCoverageReport
-> JacocoReportTask task : createDebugAndroidTestCoverageReport , /Users/hanlyjiang/Wksp/project/AndroidTestSample/app/build/outputs/code_coverage/debugAndroidTest/connected)}

> Task :app:createDebugCoverageReport
```

### 小结

通过以上信息我们可知：

1. 应用 jacoco 插件之后，本地单元测试任务（`testDebugUnitTest`）就会生成 jacoco 覆盖率execution数据文件，但是不会生成html报告；
2. 开启 isTestCoverageEnabled 开关，会生成 `createDebugCoverageReport` 任务，该任务会执行仪器单元测试，同时生成execution数据文件，并生成html报告。



## 生成 HTML 报告

由于androidTest 已经生成了html报告，接下来我们需要要为我们的本地单元测试生成HTML报告。

要生成html报告，我们需要一个类型为 JacocoReport 的任务，我们在gradle 中添加如下配置，用于生成 `jacocoTestDebugUnitTestReport` 任务

```kotlin
apply(plugin = "jacoco")

android {
    buildTypes {
        getByName("debug").apply {
            isTestCoverageEnabled = true
        }
    }
}
afterEvaluate {
	// 由于我们需要获取对应的源码及class目录，所以使用 android.applicationVariants forEach 来获取变体
	android.applicationVariants.forEach { variant ->
        if (variant.buildType.isTestCoverageEnabled) {
            val variantCapName = variant.name.capitalize();
            tasks.register(
                "jacocoTest${variantCapName}UnitTestReport",
                JacocoReport::class.java
            ) {
                group = "jacoco"
              	// 依赖测试任务
	              dependsOn("test${variantCapName}UnitTest")
                
              	// 根据执行数据生成报告，直接传输task即可
                executionData(tasks.getByName("test${variantCapName}UnitTest"))
              	// 报告中会包含源码，可以查看源码的对应的覆盖情况
                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
                // 没有 class 数据报告会是空的
                classDirectories.from(variant.javaCompileProvider.get().destinationDirectory)
              
              	doLast {
                    logger.lifecycle(reports.html.outputLocation.asFile.get().absolutePath)
                }
            }
        }
	}
}
```

添加之后 sync gradle，即可生成一个 `jacocoTestDebugUnitTestReport` 的任务，执行它即可生成测试报告，生成的测试报告位于： `build/reports/jacoco/jacocoTestDebugUnitTestReport`中。

下图就是我们生成的报告，可以看到StringUtils已经能够统计覆盖率了。而MainActivity还没有数据。

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203192203816.png" alt="image-20220319220339791" style="zoom:50%;" />

## 生成合并HTML报告

我们已经可以生成本地单元测试的覆盖率报告，现在我们需要生成androidTest + test 的合并报告。

之前我们已经知道：

- createDebugCoverageReport 任务即可生成 execution 文件并且生成html报告
- 我们已经添加了一个任务可以生成本地单元测试的报告。

现在我们要做的是将它们合并，但是我们的合并并不是针对html报告，而是针对execution数据。

让我们添加如下配置来生成一个合并报告的gradle任务：

```kotlin
apply(plugin = "jacoco")

android {
    buildTypes {
        getByName("debug").apply {
            isTestCoverageEnabled = true
        }
    }
}

afterEvaluate {
    android.applicationVariants.forEach { variant ->
        if (variant.buildType.isTestCoverageEnabled) {
            val variantCapName = variant.name.capitalize();
            tasks.register("mergedJacoco${variantCapName}TestReport", JacocoReport::class.java) {
                group = "jacoco"
                executionData(
                    tasks.getByName("test${variantCapName}UnitTest"),
                )
                val androidCoverageTask =
                    tasks.getByName("create${variantCapName}AndroidTestCoverageReport")
                if (androidCoverageTask is com.android.build.gradle.internal.coverage.JacocoReportTask) {
                    executionData(androidCoverageTask.jacocoConnectedTestsCoverageDir.asFileTree)
                }
                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
                classDirectories.from(variant.javaCompileProvider.get().destinationDirectory)
                dependsOn(
                    "test${variantCapName}UnitTest",
                    "create${variantCapName}AndroidTestCoverageReport"
                )
              	doLast {
                    logger.lifecycle(reports.html.outputLocation.asFile.get().absolutePath)
                }
            }
        }
    }
}
```

这样，我们便有了一个 `mergedJacocoDebugTestReport` 的任务，执行后即可在`build/reports/jacoco/mergedJacocoDebugTestReport/html` 目录中找到我们的 report 。

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203192204139.png" alt="image-20220319220451116" style="zoom:50%;" />

现在可以看到，我们的MainActivity（AndroidTest）及StringUtils（test）可以在一份报告中显示覆盖率数据了。

## 生成合并 Execution 数据文件

到现在为止，我们已经生成了HTML版本的合并报告，并且可以在其中看到源码没一行覆盖的情况。

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203192206236.png" alt="image-20220319220605201" style="zoom:50%;" />

但是，我们希望能够在AndroidStudio的编辑器中显示覆盖率的情况，向下面这样👇：

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203192215319.png" alt="image-20220319221553776" style="zoom:50%;" />

实际上，我们可以通过AndroidStudio的`Menu-Run-Show Covarage Data`加载 execution 文件，然后在AndroidStudio中显示覆盖率数据。

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203192208217.png" alt="image-20220319220850184" style="zoom:50%;" />

执行的数据文件位于类似如下目录 ： 

- build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec
- build/outputs/code_coverage/debugAndroidTest/connected/Pixel_5_API_Tiramisu(AVD) - 12/coverage.ec

但是这里有两个问题：

1. debugAndroidTest 目录中的 `.ec` 文件无法被AndroidStudio识别。
2. 这两个文件是分开的，每一个文件只包含一种测试的覆盖数据。



现在，让我们添加一个合并任务：

```kotlin

afterEvaluate {
    tasks.getByName("testDebugUnitTest").apply {
        (this.extensions.getByName("jacoco") as JacocoTaskExtension).apply {
            logger.lifecycle("testDebugUnitTest dest: ${this.destinationFile?.absolutePath}")
        }
    }
    tasks.withType(org.gradle.api.tasks.testing.Test::class.java) {
        logger.lifecycle("-> test task : $name")
    }
    tasks.withType(JacocoReport::class.java) {
        logger.lifecycle("-> JacocoReport task : $name")
    }
    tasks.withType(com.android.build.gradle.internal.coverage.JacocoReportTask::class.java) {
        logger.lifecycle("-> JacocoReportTask task : $name")
        doFirst {
            logger.lifecycle("-> JacocoReportTask task : $name , ${jacocoConnectedTestsCoverageDir.get()})}")
        }
    }
    android.applicationVariants.forEach { variant ->
        if (variant.buildType.isTestCoverageEnabled) {
            val variantCapName = variant.name.capitalize();
            tasks.register(
                "jacocoTest${variantCapName}UnitTestReport",
                JacocoReport::class.java
            ) {
                group = "jacoco"
                executionData(tasks.getByName("test${variantCapName}UnitTest"))
                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
                classDirectories.from(variant.javaCompileProvider.get().destinationDirectory)
                dependsOn("test${variantCapName}UnitTest")
                doLast {
                    logger.lifecycle("Jacoco Report: " + reports.html.outputLocation.asFile.get().absolutePath)
                }
            }

            tasks.register("mergedJacoco${variantCapName}TestReport", JacocoReport::class.java) {
                group = "jacoco"
                executionData(
                    tasks.getByName("test${variantCapName}UnitTest"),
                )
                val androidCoverageTask =
                    tasks.getByName("create${variantCapName}AndroidTestCoverageReport")
                if (androidCoverageTask is com.android.build.gradle.internal.coverage.JacocoReportTask) {
                    executionData(androidCoverageTask.jacocoConnectedTestsCoverageDir.asFileTree)
                }
                sourceDirectories.from(variant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories })
                classDirectories.from(variant.javaCompileProvider.get().destinationDirectory)
                dependsOn(
                    "test${variantCapName}UnitTest",
                    "create${variantCapName}AndroidTestCoverageReport"
                )
                doLast {
                    logger.lifecycle("Jacoco Report: " + reports.html.outputLocation.asFile.get().absolutePath)
                }
            }

            tasks.register("mergeJacoco${variantCapName}Execution", JacocoMerge::class.java) {
                group = "jacoco"
                executionData(
                    tasks.getByName("test${variantCapName}UnitTest"),
                )
                val androidCoverageTask =
                    tasks.getByName("create${variantCapName}AndroidTestCoverageReport")
                if (androidCoverageTask is com.android.build.gradle.internal.coverage.JacocoReportTask) {
                    executionData(androidCoverageTask.jacocoConnectedTestsCoverageDir.asFileTree)
                }
                doLast {
                    logger.lifecycle("Jacoco Execution: " + destinationFile.absolutePath)
                }
            }

        }
    }
}
```

执行之后位于：`build/jacoco/mergeJacocoDebugExecution.exec`, 通过AndroidStudio 加载之后，显示如下，两种测试的结果已经合并显示了。

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203192219808.png" alt="image-20220319221952765" style="zoom:50%;" />