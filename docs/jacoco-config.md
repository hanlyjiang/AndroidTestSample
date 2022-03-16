# jacoco 配置
应用 jacoco.gradle 配置即可。

## 存在的问题
library 模块中，仅仅通过 `jacocoTestDebugUnitTestReport` 无法生成合并的报告，会缺失。而application 模块则没有问题。
问题进一步描述：
1. app 模块没有问题；
2. library 模块中，jacocoTestReleaseReport 没有问题；
3. 生成的 coverage 文件非常小，只有几十-100 多个字节（而app模块的则至少几十KB）

升级AGP到7.2.0-beta版本之后，问题解决，可参考：
- https://issuetracker.google.com/issues/195860510#comment12
- https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/issues/36#issuecomment-977241070