# jacoco 配置
应用 jacoco.gradle  配置即可。

## 存在的问题
library 模块中，仅仅通过 `jacocoTestDebugUnitTestReport` 无法生成合并的报告，会缺失。而application 模块则没有问题。