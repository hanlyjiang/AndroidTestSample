package com.github.hanlyjiang.sample.utils;


import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty("not empty"));
    }
}