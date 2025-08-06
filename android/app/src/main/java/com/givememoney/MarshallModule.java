// testing the thing based on youtube tutorial
package com.givememoney;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.givememoney.App; // Add this import

public class MarshallModule extends ReactContextBaseJavaModule {

    private static int eventCount = 0;
    ReactApplicationContext context;

    public MarshallModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @ReactMethod
    public void createEventPromise(String name, Promise promise) {
        try {
            Log.d("MarshallModule", "Promise Event Created: " + name);

            if (name.startsWith("PAYMENT:")) {
                String amountStr = name.split(":")[1];
                short amount = (short) (Float.parseFloat(amountStr) * 100);

                App.startPayment(amount, getReactApplicationContext());

                Log.d("MarshallModule", "Payment started for amount: " + amountStr);

                promise.resolve("Payment started for " + amountStr);
            } else {
                promise.resolve("Promise created: " + name);
            }
        } catch (Exception e) {
            promise.reject("ERR", e);
        }
    }

    @Override
    public String getName() {
        return "MarshallModule";
    }

    @ReactMethod
    public void createEvent(String name, Callback callback) {
        Log.d("MarshallModule", "Event Created: " + name);
        callback.invoke("Created: " + name);
    }
}
