package com.givememoney;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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
    public void getUsbManager(Promise promise) {
        try {
            UsbManager usbManager = (UsbManager) reactContext.getSystemService(Context.USB_SERVICE);
            if (usbManager != null) {
                promise.resolve("UsbManager is available");
            } else {
                promise.reject("USB_MANAGER_ERROR", "UsbManager not available");
            }
        } catch (Exception e) {
            promise.reject("USB_MANAGER_ERROR", e);
        }
    }
}
