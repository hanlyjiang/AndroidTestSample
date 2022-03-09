package com.github.hanlyjiang.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.hanlyjiang.lib_mod.ViewTest;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(v -> ViewTest.disposable());
    }
}