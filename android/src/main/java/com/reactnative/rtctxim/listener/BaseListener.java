package com.reactnative.rtctxim.listener;

import com.reactnative.rtctxim.module.BaseModule;

public abstract class BaseListener {

    protected BaseModule module;

    public BaseListener(BaseModule module) {
        if (this.module == null)
            this.module = module;
    }
}
