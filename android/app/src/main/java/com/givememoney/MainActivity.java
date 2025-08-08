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

public class MainActivity extends ReactActivity {

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
        }
    }

    private void handleUsbIntent(Intent intent) {
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                Toast.makeText(this, "USB device attached: " + device.getDeviceName(), Toast.LENGTH_SHORT).show();
                UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

                for (UsbDevice usbDevice : deviceList.values()) {
                    int vendorId = usbDevice.getVendorId();
                    int productId = usbDevice.getProductId();
                    String deviceName = usbDevice.getDeviceName();

                    Log.d("USB", "Found device: " + deviceName + " (VID: " + vendorId + ", PID: " + productId + ")");
                }
            }
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
