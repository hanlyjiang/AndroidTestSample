# AAR Java8 接口 NoSuchMethodError 错误解决记录

遇到一个初看时非常诡异的问题，现已解决，特记录一下解决过程。

## 🙋‍♀️ 是什么问题？

### 错误日志

APP运行过程中，忽然报了一个莫名其妙的错误： `NoSuchMethodError`, 报错的地方是 rxjava3 的 `Disposable.disposed()`

```shell
E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.github.hanlyjiang.sample, PID: 7357
    java.lang.NoSuchMethodError: No static method disposed()Lio/reactivex/rxjava3/disposables/Disposable; in class Lio/reactivex/rxjava3/disposables/Disposable; or its super classes (declaration of 'io.reactivex.rxjava3.disposables.Disposable' appears in /data/app/~~veR3ZUFYzXjZ48FDcAW0Nw==/com.github.hanlyjiang.sample-bhuZG0wVNdVKs_mfxOh5gg==/base.apk)
        at com.github.hanlyjiang.lib_mod.ViewTest.disposable(ViewTest.java:8)
        at com.github.hanlyjiang.sample.MainActivity.lambda$onCreate$0(MainActivity.java:15)
        at com.github.hanlyjiang.sample.MainActivity$$ExternalSyntheticLambda0.onClick(Unknown Source:0)
        at android.view.View.performClick(View.java:7456)
        at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1119)
        at android.view.View.performClickInternal(View.java:7433)
        at android.view.View.access$3700(View.java:836)
        at android.view.View$PerformClick.run(View.java:28832)
        at android.os.Handler.handleCallback(Handler.java:938)
        at android.os.Handler.dispatchMessage(Handler.java:99)
        at android.os.Looper.loopOnce(Looper.java:201)
        at android.os.Looper.loop(Looper.java:288)
        at android.app.ActivityThread.main(ActivityThread.java:7902)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:933)
```

> **⏰ Tip**
>
> 实际上还有遇到过一个 `AbstractMethodError` 的错误

### 出现场景

- AAR 引入到APP模块
- 打包APK（不通过AndroidStudio直接运行）
- 安装并运行

## 解决方案

对于需要解决问题的，可以直接参考尝试下面两种解决方案。

### 解法1

`gradle.properties` 文件中添加属性：

```properties
android.enableDexingArtifactTransform.desugaring=false
```

### 解法2

修改 aar 的依赖方式，将name+ext的方式修改为 files

```groovy
	implementation(name: 'libmod-release', ext: 'aar')
```

修改之后

```groovy
	implementation(files('libs/libmod-release.aar'))
```

## 案例观察

根据错误信息，找到我们的代码，得到如下调用关系：

```java
ViewTest.disposable -> Disposable.disposed()
```

为了方便分析及演示，我编写了一个测试工程位于： [colorless-hanly/NoSuchMethodError: NoSuchMethodError (github.com)](https://github.com/colorless-hanly/NoSuchMethodError)，运行此工程能复现该问题。

###  class字节码没有问题

我们的AAR提供给APP模块时，是以 class 类文件提供的，通过反编译查看其内容，没有任何问题。



所以我们看看最后运行的APK对应的DEX，并通过AndroidStudio 查看其对应的字节码。

### `ViewTest.disposable`

首先我们根据报错信息找到出错的`ViewTest.disposable`类，并查看其字节码

**Java 代码：**

```java
public class ViewTest {

    public static Disposable disposable() {
        return Disposable.disposed();
    }
}
```

**字节码：**

```java
.class public Lcom/github/hanlyjiang/lib_mod/ViewTest;
.super Ljava/lang/Object;
.source "ViewTest.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .line 5
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static disposable()Lio/reactivex/rxjava3/disposables/Disposable;
    .registers 1

    .line 8
    invoke-static {}, Lio/reactivex/rxjava3/disposables/Disposable;->disposed()Lio/reactivex/rxjava3/disposables/Disposable;

    move-result-object v0

    return-object v0
.end method
```

看起来没有任何问题。

### `Disposable.disposed()`

我们继续查看 Disposable 接口的字节码，发现居然**没有我们调用的 disposed 方法**。

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203132238351.png" alt="image-20220313223827282" style="zoom:50%;" />

但是，很快就在有一个叫做 `Disposable$-CC` 的类中找到了我们的 `disposed` 方法，同时其中还包括了 `Disposable` 接口中的所有静态方法

<img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203132240481.png" alt="image-20220313224048436" style="zoom:50%;" />

这里我们粘贴下 Disposable 接口的定义，可以看到其中有两个接口方法定义，还有若干static方法的定义及实现。而我们的静态方法全部位于字节码中 `Disposable$-CC` 这个类中，而两个接口方法还保留在 `Disposable` 类中。

```java
public interface Disposable {

    void dispose();
    boolean isDisposed();

    static Disposable fromRunnable(@NonNull Runnable run) {
        Objects.requireNonNull(run, "run is null");
        return new RunnableDisposable(run);
    }
    static Disposable fromAction(@NonNull Action action) {
        Objects.requireNonNull(action, "action is null");
        return new ActionDisposable(action);
    }
    static Disposable fromFuture(@NonNull Future<?> future) {
        Objects.requireNonNull(future, "future is null");
        return fromFuture(future, true);
    }
    static Disposable fromFuture(@NonNull Future<?> future, boolean allowInterrupt) {
        Objects.requireNonNull(future, "future is null");
        return new FutureDisposable(future, allowInterrupt);
    }
    static Disposable fromSubscription(@NonNull Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription is null");
        return new SubscriptionDisposable(subscription);
    }
    static Disposable fromAutoCloseable(@NonNull AutoCloseable autoCloseable) {
        Objects.requireNonNull(autoCloseable, "autoCloseable is null");
        return new AutoCloseableDisposable(autoCloseable);
    }
    static AutoCloseable toAutoCloseable(@NonNull Disposable disposable) {
        Objects.requireNonNull(disposable, "disposable is null");
        return disposable::dispose;
    }
    static Disposable empty() {
        return fromRunnable(Functions.EMPTY_RUNNABLE);
    }
    static Disposable disposed() {
        return EmptyDisposable.INSTANCE;
    }
}
```

### 正确的`ViewTest.disposable`字节码

前面我们发现，我们字节码中调用的方法是 `Disposable;->disposed()`，而实际上该方法位于 `Disposable$-CC;->disposed()`,现在我们看下正常的可以运行通过的apk中dex对应的字节码。

- 经过对比，发现Disposable 还是被拆分为了 Disposable 和 Disposable$-CC
- 而 ViewTest.disposable 对应的字节码却有区别，正常可以调用通过的字节码和异常的对比如下：

```java
// 正确的
invoke-static {}, Lio/reactivex/rxjava3/disposables/Disposable$-CC;->disposed()Lio/reactivex/rxjava3/disposables/Disposable;

// 错误的
invoke-static {}, Lio/reactivex/rxjava3/disposables/Disposable;->disposed()Lio/reactivex/rxjava3/disposables/Disposable;
```

可以发现，能正常运行的方法调用确实指向了 `Disposable$-CC;->disposed()`。

### 总结

也就是说在编译的过程中：

- Disposable 会被拆分为 `Disposable` 和 `Disposable$-CC`
    - `Disposable` 包含了接口方法的声明
    - `Disposable$-CC` 中则包含了静态方法的声明及实现
- 对于调用到 Disposable 的静态方法的部分，最终编译完成后，会替换为指向`Disposable$-CC`

而我们出错的情况下，是没有正确指向的，所以确实会出现找不到对应的方法的情况。

## 原因分析

### class -> dex & Java8 语言特性支持

以下为Android 官方文档上的相关描述：

> - **[使用 Java 8 语言功能和 API](https://developer.android.google.cn/studio/write/java8-support)**
    >
    >  Android Gradle 插件对使用某些 Java 8 语言功能以及利用这些功能的第三方库提供内置支持。如图 所示，默认工具链实现新语言功能的方法是在**使用 D8/R8 将类文件编译成 dex 代码的过程中执行字节码转换，这种转换称为 `desugar`**。
>
> <img src="https://gitee.com/hanlyjiang/image-repo/raw/master/image/202203132254856.png" alt="desugar_diagram" style="zoom:50%;" />



> - **[Java 8 及更高版本 API 脱糖支持（Android Gradle 插件 4.0.0 及更高版本）](https://developer.android.google.cn/studio/write/java8-support#library-desugaring)**
>
> 如果您使用 Android Gradle 插件 4.0.0 或更高版本构建应用，插件扩展了对使用多种 Java 8 语言 API 的支持，而无需为应用设置最低 API 级别。
>
> 之所以能够实现对较低平台版本的这种额外支持，是因为脱糖引擎经过插件 4.0.0 及更高版本扩展后，也能使 Java 语言 API 脱糖。因此，您可以在支持较低 Android 版本的应用中添加过去仅在最新 Android 版本中可用的标准语言 API（如 `java.util.streams`）。
>
> 使用 Android Gradle 插件 4.0.0 或更高版本构建应用时，支持下面一组 API：
>
> - 顺序流 (`java.util.stream`)
>- `java.time` 的子集
> - `java.util.function`
>- `java.util.{Map,Collection,Comparator}` 的最近新增内容
> - 可选内容（`java.util.Optional`、`java.util.OptionalInt` 和 `java.util.OptionalDouble`）以及对上述 API 很有用的一些其他新类
>- `java.util.concurrent.atomic` 的一些新增内容（`AtomicInteger`、`AtomicLong` 和 `AtomicReference` 的新方法）
> - `ConcurrentHashMap`（包含 Android 5.0 的问题修复）
>
> 如需查看受支持的 API 的完整列表，请参阅[通过脱糖获得 Java 8 及更高版本 API](https://developer.android.google.cn/studio/write/java8-support-table)。
>
> 为了支持这些语言 API，插件编译了一个单独的 DEX 文件（其中包含缺失 API 的实现），并将其添加到您的应用中。脱糖过程会重新编写应用的代码，以便在运行时改用此库。



> - **[编译使用了 Java 8 语言功能的字节码](https://developer.android.google.cn/studio/command-line/d8)**
>
> `d8` 通过一个叫做“脱糖”的编译过程，使您能够在代码中[使用 Java 8 语言功能](https://developer.android.google.cn/studio/write/java8-support)，此过程会将这些实用的语言功能转换为可以在 Android 平台上运行的字节码。
>
> Android Studio 和 Android Gradle 插件包含了 `d8` 为您启用脱糖所需的类路径资源。
>
> Android Studio 和 Android Gradle 插件包含了 `d8` 为您启用脱糖所需的类路径资源。不过，从命令行使用 `d8` 时，您需要手动添加这些资源。
>
> 其中一个资源就是目标 Android SDK 中的 `android.jar`。此资源包含一组 Android 平台 API，您可以使用 `--lib` 标记来指定该资源的路径。
>
> 另一个资源是您项目的部分已编译的 Java 字节码，您目前不打算将这部分字节码编译为 DEX 字节码，但在将其他类编译为 DEX 字节码时需要用到这些字节码。例如，如果代码使用[默认和静态接口方法](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html)（一种 Java 8 语言功能），则您需要使用此标记来指定您项目的所有 Java 字节码的路径，即使您不打算将所有 Java 字节码都编译为 DEX 字节码也是如此。**这是因为 `d8` 需要根据这些信息来理解您项目的代码并解析对接口方法的调用**。



通过以上描述我们可以知道：

- Android 通过 D8 将 class 转换为可在Android平台上执行的dex，这个过程称为 **desugar （脱糖）**。
- 脱糖过程中，可能会重新编写应用的代码。
- 脱糖时需要指定脱糖所需要的类路径资源。

### 对应我们的问题

> 待补充
>
> 通过 d8 手动对class进行dex转换，然后观察转换差异。

```shell 
d8 MainActivity.class --intermediate --file-per-class --output ~/build/intermediate/dex
--lib android_sdk/platforms/api-level/android.jar
--classpath ~/build/javac/debug
```

将类文件编译成 dex 代码的过程中执行字节码转换，这种转换称为 `desugar`。AGP 使用 D8 完成desugar ，为了能正确的处理class，需要根据POM依赖信息来寻找对应的依赖，然后将所有依赖项目都加入到 desurge classpath，然后才能正确处理。而 aar 引入时不具备这些POM依赖信息，所以无法还原。

## 进一步了解

- [d8  | Android 开发者  | Android Developers (google.cn)](https://developer.android.google.cn/studio/command-line/d8)
- [使用 Java 8 语言功能和 API  | Android 开发者  | Android Developers (google.cn)](https://developer.android.google.cn/studio/write/java8-support)
- [使用 Java 8 语言功能和 API  | Android 开发者  | Android Developers (google.cn)](https://developer.android.google.cn/studio/write/java8-support#library-desugaring)
- [Android Gradle 插件版本说明  | Android 开发者  | Android Developers (google.cn)](https://developer.android.google.cn/studio/releases/gradle-plugin)



