package com.givememoney;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

public class AppModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public AppModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "AppModule";
    }

    @ReactMethod
    public void startPayment(int amount, Promise promise) {
        try {
            App.startPayment((short) amount, reactContext);
            promise.resolve("Payment started successfully.");
        } catch (Exception e) {
            promise.reject("PAYMENT_ERROR", e);
        }
    }

    @ReactMethod
    public void createEvent(String name, Promise promise) {
        try {
            Log.d("AppModule", "Event Created: " + name);
            promise.resolve("Event created: " + name);
        } catch (Exception e) {
            promise.reject("EVENT_CREATION_ERROR", e);
        }
    }

    @ReactMethod
    public void getUsbManager(Promise promise) {
        try {
            UsbManager usbManager = (UsbManager) reactContext.getSystemService(Context.USB_SERVICE);
            // UsbManager is not directly serializable to JS, return some info instead
            if (usbManager != null) {
                promise.resolve("UsbManager is available");
            } else {
                promise.reject("USB_MANAGER_ERROR", "UsbManager not available");
            }
        } catch (Exception e) {
            promise.reject("USB_MANAGER_ERROR", e);
        }
    }

    @ReactMethod
    public void getDeviceName(Promise promise) {
        try {
            String deviceName = android.os.Build.MODEL;
            promise.resolve(deviceName);
        } catch (Exception e) {
            promise.reject("DEVICE_NAME_ERROR", e);
        }
    }

    @ReactMethod
    public void getAppVersion(Promise promise) {
        try {
            String versionName = reactContext.getPackageName();
            promise.resolve(versionName);
        } catch (Exception e) {
            promise.reject("APP_VERSION_ERROR", e);
        }
    }
}