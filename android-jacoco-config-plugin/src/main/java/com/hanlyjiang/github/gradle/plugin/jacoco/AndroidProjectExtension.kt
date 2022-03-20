package com.hanlyjiang.github.gradle.plugin.jacoco

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.plugins.ExtensionAware

/**
 * Android 项目相关扩展函数，方便获取android的扩展
 * @author hanlyjiang 5/27/21 11:38 AM
 * @version 1.0
 */

fun Project.getAndroidExtensionAsLibrary() =
    (this as ExtensionAware).extensions.getByName("android") as LibraryExtension

fun Project.getAndroidExtensionAsApp() =
    (this as ExtensionAware).extensions.getByName("android") as BaseAppModuleExtension

fun Project.getAndroidExtension() =
    (this as ExtensionAware).extensions.getByName("android") as BaseExtension

fun Project.isAndroidProject(): Boolean {
    return (this as ExtensionAware).extensions.findByName("android") != null
}

fun Project.getAppVariants(): DomainObjectSet<ApplicationVariant>? {
    if (getAndroidExtension() is BaseAppModuleExtension) {
        return getAndroidExtensionAsApp().applicationVariants;
    }
    return null
}

fun Project.getLibVariants(): DefaultDomainObjectSet<LibraryVariant>? {
    if (getAndroidExtension() is LibraryExtension) {
        return getAndroidExtensionAsLibrary().libraryVariants;
    }
    return null
}

fun Project.isAndroidAppProject(): Boolean {
    if (!isAndroidProject()) {
        return false
    }
    if (getAndroidExtension() is BaseAppModuleExtension) {
        return true
    }
    return false
}

/**
 * 是否为Android库项目
 *
 * @return true - android 库项目
 */
fun Project.isAndroidLibProject(): Boolean {
    if (!isAndroidProject()) {
        return false
    }
    if (getAndroidExtension() is LibraryExtension) {
        return true
    }
    return false
}