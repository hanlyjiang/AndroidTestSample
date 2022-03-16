package com.github.hanlyjiang.lib_mod.ui;

import static org.junit.Assert.*;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LibActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void onCreate() {
        ActivityScenario<LibActivity> launch = ActivityScenario.launch(LibActivity.class);
        Assert.assertEquals(Lifecycle.State.RESUMED, launch.getState());
    }
}