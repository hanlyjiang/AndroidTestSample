package com.github.hanlyjiang.lib_mod;

import io.reactivex.rxjava3.disposables.Disposable;

public class ViewTest {
    private ViewTest() {
    }

    public static Disposable disposable() {
        return Disposable.disposed();
    }
}
