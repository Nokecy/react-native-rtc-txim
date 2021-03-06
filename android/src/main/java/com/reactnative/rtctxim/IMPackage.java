package com.reactnative.rtctxim;

import android.os.Looper;

import androidx.annotation.MainThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;

import com.facebook.react.uimanager.ViewManager;
import com.reactnative.rtctxim.module.InitializeModule;
import com.reactnative.rtctxim.module.MessageModule;


/**
 * @author Kurisu
 */
public class IMPackage implements ReactPackage {

    public static InitializeModule initializeModule;

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        if (initializeModule == null) {
            initializeModule = new InitializeModule(reactContext);
        }
        modules.add(initializeModule);
        modules.add(new MessageModule(reactContext));
        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(
            ReactApplicationContext reactContext) {
        init(reactContext);
        return Collections.emptyList();
    }

    @MainThread
    protected void init(ReactApplicationContext reactContext) {
        if (Looper.myLooper()==null)
            Looper.prepare();
        if (initializeModule != null) {
            initializeModule.init(0);
        }
    }

}
