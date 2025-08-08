package com.givememoney;

import android.os.Handler;
import android.os.Looper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import android.hardware.usb.UsbDeviceConnection;

public class MarshallModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext context;
    private UsbManager usbManager;
    private static final String TAG = "MarshallModule";

    public MarshallModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        Log.d(TAG, "MarshallModule constructor called - Module is being initialized!");

        // Delay USB initialization for Android 8 compatibility!
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                usbManager = (UsbManager) reactContext.getSystemService(Context.USB_SERVICE);
                if (usbManager == null) {
                    Log.e(TAG, "USB Service not available");
                }
            } catch (Exception e) {
                Log.e(TAG, "USB initialization error", e);
            }
        }, 1000); // 1 second delay
    }

    private boolean isAndroid8WithUsbIssues() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O
                && !checkAndroid8UsbSupport();
    }

    private boolean checkAndroid8UsbSupport() {
        try {
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            return manager != null && manager.getDeviceList() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUsbSupported() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
    }
    // the error is here in this method somewhere...

    @ReactMethod
    public void startPayment(double amount, Promise promise) {
        try {
            if (!isUsbSupported()) {
                promise.reject("USB_NOT_SUPPORTED", "USB host mode not supported");
                return;
            }

            if (usbManager == null) {
                promise.reject("USB_SERVICE_UNAVAILABLE", "USB service not available");
                return;
            }

            UsbDevice device = findUsbDevice();
            if (device == null) {
                promise.reject("NO_DEVICE", "Payment device not found");
                return;
            }

            if (!usbManager.hasPermission(device)) {
                requestUsbPermission(device, amount, promise);
            } else {
                processPayment(amount, promise);
            }
        } catch (Exception e) {
            Log.e(TAG, "Payment error", e);
            promise.reject("UNKNOWN_ERROR", e.getMessage());
        }
    }

    private void requestUsbPermission(UsbDevice device, double amount, Promise promise) {
        final String ACTION_USB_PERMISSION = "com.givememoney.USB_PERMISSION";

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(ACTION_USB_PERMISSION),
                flags);

        BroadcastReceiver usbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        context.unregisterReceiver(this);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            processPayment(amount, promise);
                        } else {
                            promise.reject("PERMISSION_DENIED", "USB permission denied");
                        }
                    }
                }
            }
        };

        context.registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        usbManager.requestPermission(device, permissionIntent);
    }

    private void processPayment(double amount, Promise promise) {
        try {
            Log.d(TAG, "Processing payment for amount: " + amount);
            
            // Find and open USB serial connection to payment terminal
            UsbSerialProber prober = CustomProber.getCustomProber();
            List<UsbSerialDriver> drivers = prober.findAllDrivers(usbManager);
            
            if (drivers.isEmpty()) {
                promise.reject("NO_DEVICE", "No USB serial drivers found for payment terminal");
                return;
            }
            
            UsbSerialDriver driver = drivers.get(0);
            UsbDevice device = driver.getDevice();
            
            // Verify this is our payment terminal device (VID=1027, PID=24597)
            if (device.getVendorId() != 1027 || device.getProductId() != 24597) {
                Log.w(TAG, "Found device VID=" + device.getVendorId() + ", PID=" + device.getProductId() + " but expected VID=1027, PID=24597");
            }
            
            UsbDeviceConnection connection = usbManager.openDevice(device);
            if (connection == null) {
                promise.reject("CONNECTION_ERROR", "Could not open USB device connection");
                return;
            }
            
            if (driver.getPorts() == null || driver.getPorts().isEmpty()) {
                promise.reject("NO_PORTS", "No serial ports available on payment device");
                return;
            }
            
            UsbSerialPort port = driver.getPorts().get(0);
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            
            // Convert amount to cents for payment protocol
            short cents = (short) (amount * 100);
            
            // Send payment command to terminal
            // This is a basic implementation - you may need to adjust the protocol based on your payment terminal
            String paymentCommand = String.format("PAY:%d\r\n", cents);
            byte[] commandBytes = paymentCommand.getBytes();
            
            Log.d(TAG, "Sending payment command: " + paymentCommand.trim());
            port.write(commandBytes, 5000); // 5 second timeout
            
            // Read response from payment terminal
            byte[] buffer = new byte[1024];
            int bytesRead = port.read(buffer, 10000); // 10 second timeout for payment processing
            
            port.close();
            
            if (bytesRead > 0) {
                String response = new String(buffer, 0, bytesRead).trim();
                Log.d(TAG, "Payment terminal response: " + response);
                
                // Parse response - adjust this based on your payment terminal's protocol
                if (response.contains("SUCCESS") || response.contains("APPROVED")) {
                    promise.resolve("Payment successful - amount: $" + String.format("%.2f", amount) + " - Response: " + response);
                } else if (response.contains("DECLINED") || response.contains("ERROR")) {
                    promise.reject("PAYMENT_DECLINED", "Payment declined by terminal: " + response);
                } else {
                    promise.resolve("Payment processed - amount: $" + String.format("%.2f", amount) + " - Response: " + response);
                }
            } else {
                promise.reject("NO_RESPONSE", "No response received from payment terminal");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Payment processing error", e);
            promise.reject("PAYMENT_ERROR", "USB serial communication error: " + e.getMessage());
        }
    }

    private UsbDevice findUsbDevice() {
        try {
            for (UsbDevice device : usbManager.getDeviceList().values()) {
                if (device.getVendorId() == 1027 && device.getProductId() == 24597) {
                    return device;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding USB device", e);
        }
        return null;
    }

    @Override
    public String getName() {
        Log.d(TAG, "getName() called - returning 'MarshallModule'");
        return "MarshallModule";
    }

    @ReactMethod
    public void createEvent(String name, Callback callback) {
        callback.invoke("Event created: " + name);
    }

    @ReactMethod
    public void createEventPromise(String name, Promise promise) {
        promise.resolve("Promise event created: " + name);
    }

    // @ReactMethod
    // public void initSerial(Promise promise) {
    //     try {
    //         if (!isUsbSupported()) {
    //             promise.reject("USB_NOT_SUPPORTED", "USB host mode not supported");
    //             return;
    //         }
    //         if (usbManager == null) {
    //             promise.reject("USB_SERVICE_UNAVAILABLE", "USB service not available");
    //             return;
    //         }
    //         List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
    //         if (drivers.isEmpty()) {
    //             promise.reject("NO_DEVICE", "No USB serial drivers found");
    //             return;
    //         }
    //         UsbSerialDriver driver = drivers.get(0);
    //         // Check for permission to access port before opening the port with openDevice()
    //         UsbDevice device = driver.getDevice();
    //         if (!usbManager.hasPermission(device)) {
    //             promise.reject("PERMISSION_DENIED", "No permission to access USB device");
    //             return;
    //         }
    //         UsbDeviceConnection connection = usbManager.openDevice(device);
    //         if (connection == null) {
    //             promise.reject("CONNECTION_ERROR", "Could not open device connection");
    //             return;
    //         }
    //         // Check for a NULL port list before driver.getPorts() is called
    //         if (driver.getPorts() == null) {
    //             promise.reject("NULL_PORTS", "Port list is null");
    //             return;
    //         }
    //         if (driver.getPorts().isEmpty()) {
    //             promise.reject("NO_PORTS", "No serial ports available on device");
    //             return;
    //         }
    //         UsbSerialPort port = driver.getPorts().get(0);
    //         port.open(connection);
    //         port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
    //         promise.resolve("Serial port opened and configured"); // it works!
    //     } catch (Exception e) {
    //         Log.e(TAG, "Serial init error", e);
    //         promise.reject("SERIAL_ERROR", e.getMessage());
    //     }
    // }
    @ReactMethod
    public void initSerial(Promise promise) {
        try {
            if (!isUsbSupported()) {
                promise.reject("USB_NOT_SUPPORTED", "USB host mode not supported");
                return;
            }

            if (usbManager == null) {
                promise.reject("USB_SERVICE_UNAVAILABLE", "USB service not available");
                return;
            }

            UsbSerialProber prober = CustomProber.getCustomProber();
            List<UsbSerialDriver> drivers = prober.findAllDrivers(usbManager);

            if (drivers.isEmpty()) {
                promise.reject("NO_DEVICE", "No USB serial drivers found");
                return;
            }

            UsbSerialDriver driver = drivers.get(0);

            // Check for permission to access port before opening the port with openDevice()
            UsbDevice device = driver.getDevice();
            if (!usbManager.hasPermission(device)) {
                promise.reject("PERMISSION_DENIED", "No permission to access USB device");
                return;
            }

            Log.d(TAG, "Detected device: VID=" + device.getVendorId() + ", PID=" + device.getProductId());

            UsbDeviceConnection connection = usbManager.openDevice(device);
            if (connection == null) {
                promise.reject("CONNECTION_ERROR", "Could not open device connection");
                return;
            }

            // Check for a NULL port list before driver.getPorts() is called
            if (driver.getPorts() == null) {
                promise.reject("NULL_PORTS", "Port list is null");
                return;
            }

            if (driver.getPorts().isEmpty()) {
                promise.reject("NO_PORTS", "No serial ports available on device");
                return;
            }

            UsbSerialPort port = driver.getPorts().get(0);
            port.open(connection);
            port.setParameters(
                    115200, // baud rate
                    8, // data bits
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
            );

            promise.resolve("Serial port opened and configured");
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

            for (UsbDevice usbDevice : deviceList.values()) {
                int vendorId = usbDevice.getVendorId();
                int productId = usbDevice.getProductId();
                String deviceName = usbDevice.getDeviceName();

                Log.d("USB", "Found device: " + deviceName + " (VID: " + vendorId + ", PID: " + productId + ")");
            }

        } catch (Exception e) {
            Log.e(TAG, "Serial init error", e);
            promise.reject("SERIAL_ERROR", e.getMessage());
        }
    }
}
