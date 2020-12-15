package com.sogoservices.pointme;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.sogoservices.pointme.sdk.offline.config.PNMSDK;
import com.sogoservices.pointme.sdk.offline.config.PNMSDKConfig;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            CrashActivity.start(this, e);
            oldHandler.uncaughtException(t, e);
        });
        initPNMSDK();
    }

    private void initPNMSDK() {
        PNMSDK.INSTANCE.initDataModule(new PNMSDKConfig() {
            @NonNull
            @Override
            public Context getContext() {
                return getApplicationContext();
            }
        });
    }

}
