package com.dumbapp.memorydance;

import android.app.Application;
import android.content.Context;

public class MemoryDanceApplication extends Application {
    private MemoryDanceApplicationComponent memoryDanceApplicationComponent;
    private static MemoryDanceApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        memoryDanceApplicationComponent = DaggerMemoryDanceApplicationComponent.builder().build();
    }

    public MemoryDanceApplicationComponent getMemoryDanceApplicationComponent() {
        return memoryDanceApplicationComponent;
    }

    public static MemoryDanceApplication getInstance() {
        return instance;
    }

    public static Context getMemoryDanceApplicationContext() {
        return instance.getApplicationContext();
    }
}
