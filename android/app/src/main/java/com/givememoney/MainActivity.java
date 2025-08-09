package com.givememoney;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import java.util.HashMap;

/**
 * Main Activity for React Native app with USB device support
 * Handles USB device attachment intents and React Native lifecycle
 */
public class MainActivity extends ReactActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUsbSupport();
        handleUsbIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            handleUsbIntent(intent);
        }
    }

    private void checkUsbSupport() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            Toast.makeText(this, "USB host mode not supported", Toast.LENGTH_LONG).show();
            Log.w(TAG, "USB host mode not supported on this device");
        } else {
            Log.d(TAG, "USB host mode supported");
        }
    }

    private void handleUsbIntent(Intent intent) {
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                String deviceInfo = String.format("USB device attached: %s (VID: %d, PID: %d)", 
                    device.getDeviceName(), device.getVendorId(), device.getProductId());
                
                Toast.makeText(this, deviceInfo, Toast.LENGTH_SHORT).show();
                Log.d(TAG, deviceInfo);
                
                // Log all connected USB devices for debugging
                logAllUsbDevices();
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                String deviceInfo = String.format("USB device detached: %s", device.getDeviceName());
                Toast.makeText(this, deviceInfo, Toast.LENGTH_SHORT).show();
                Log.d(TAG, deviceInfo);
            }
        }
    }

    private void logAllUsbDevices() {
        try {
            UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

            Log.d(TAG, "=== USB Device List ===");
            for (UsbDevice usbDevice : deviceList.values()) {
                int vendorId = usbDevice.getVendorId();
                int productId = usbDevice.getProductId();
                String deviceName = usbDevice.getDeviceName();

                Log.d(TAG, String.format("Device: %s (VID: %d/0x%04X, PID: %d/0x%04X)", 
                    deviceName, vendorId, vendorId, productId, productId));
            }
            Log.d(TAG, "=== End USB Device List ===");
        } catch (Exception e) {
            Log.e(TAG, "Error listing USB devices", e);
        }
    }

    @Override
    protected String getMainComponentName() {
        return "givememoney";
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
        };
    }
}
