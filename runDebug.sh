#!/usr/bin/env bash

./gradlew app:runAsAarDependenciesDebug
adb shell am start-activity  com.github.hanlyjiang.sample/.MainActivity