package com.github.hanlyjiang.sample.utils;

public class StringUtils {

    private StringUtils() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
