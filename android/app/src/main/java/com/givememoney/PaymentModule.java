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
import android.hardware.usb.UsbDeviceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;

// USB Serial imports for NAYAX ONLY
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProlificSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;

import java.util.List;
import java.util.HashMap;

/**
 * NAYAX-ONLY Payment Module
 * Supports NAYAX payment terminals using MDB (Multi-Drop Bus) protocol
 */
public class PaymentModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext context;
    private UsbManager usbManager;
    private static final String TAG = "PaymentModule";

    // Device type constants - NAYAX ONLY!
    private static final String DEVICE_TYPE_NAYAX = "NAYAX";
    private static final String DEVICE_TYPE_GENERIC = "GENERIC";

    // REAL Nayax device IDs (verified from actual Nayax devices)
    private static final int NAYAX_VID_1 = 0x0403; // FTDI - many Nayax use FTDI chips
    private static final int NAYAX_PID_1 = 0x6001; // FTDI FT232R USB UART
    private static final int NAYAX_VID_2 = 0x10C4; // Silicon Labs - some Nayax models  
    private static final int NAYAX_PID_2 = 0xEA60; // CP2102/CP2109 USB to UART Bridge
    private static final int NAYAX_VID_3 = 0x067B; // Prolific - older Nayax models
    private static final int NAYAX_PID_3 = 0x2303; // PL2303 Serial Port
    private static final int NAYAX_VID_4 = 0x0403; // current nayax reader VID
    private static final int NAYAX_PID_4 = 0x6015; // current nayax reader PID
    

    public PaymentModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        Log.d(TAG, "PaymentModule constructor - NAYAX ONLY payment system");

        // Initialize USB manager with delay for Android 8+ compatibility
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                usbManager = (UsbManager) reactContext.getSystemService(Context.USB_SERVICE);
                if (usbManager == null) {
                    Log.e(TAG, "USB Service not available");
                } else {
                    Log.d(TAG, "USB Manager initialized successfully for NAYAX devices");
                }
            } catch (Exception e) {
                Log.e(TAG, "USB initialization error", e);
            }
        }, 1000);
    }

    @Override
    public String getName() {
        return "PaymentModule";
    }

    // ===== REACT NATIVE BRIDGE METHODS - NAYAX ONLY =====

    @ReactMethod
    public void startPayment(double amount, Promise promise) {
        startNayaxPayment(amount, promise);
    }

    @ReactMethod
    public void startNayaxPayment(double amount, Promise promise) {
        Log.d(TAG, "Starting NAYAX payment for amount: £" + amount);
        
        try {
            if (!isUsbSupported()) {
                promise.reject("USB_NOT_SUPPORTED", "USB host mode not supported on this device");
                return;
            }

            UsbDevice device = findNayaxDevice();
            if (device == null) {
                promise.reject("NO_NAYAX_DEVICE", "No NAYAX payment device found");
                return;
            }

            Log.d(TAG, "Found NAYAX device: " + device.getDeviceName());

            if (!usbManager.hasPermission(device)) {
                requestUsbPermission(device, amount, promise);
            } else {
                processNayaxPayment(device, amount, promise);
            }

        } catch (Exception e) {
            Log.e(TAG, "NAYAX payment error", e);
            promise.reject("NAYAX_PAYMENT_ERROR", e.getMessage());
        }
    }

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

            UsbSerialProber prober = getCustomProber();
            List<UsbSerialDriver> drivers = prober.findAllDrivers(usbManager);

            if (drivers.isEmpty()) {
                promise.reject("NO_DEVICE", "No USB serial drivers found");
                return;
            }

            // List all found devices for debugging
            StringBuilder deviceInfo = new StringBuilder("NAYAX Device Scan: ");
            for (UsbSerialDriver driver : drivers) {
                UsbDevice device = driver.getDevice();
                deviceInfo.append(String.format("VID=0x%04X PID=0x%04X ", 
                    device.getVendorId(), device.getProductId()));
            }
            
            Log.d(TAG, deviceInfo.toString());
            promise.resolve("NAYAX serial initialization successful. " + deviceInfo.toString());

        } catch (Exception e) {
            Log.e(TAG, "Serial init error", e);
            promise.reject("SERIAL_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void createEvent(String name, Callback callback) {
        callback.invoke("NAYAX Event created: " + name);
    }

    @ReactMethod
    public void createEventPromise(String name, Promise promise) {
        promise.resolve("NAYAX Promise event created: " + name);
    }

    @ReactMethod
    public void getDeviceInfo(Promise promise) {
        try {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            StringBuilder info = new StringBuilder();
            
            info.append("=== NAYAX DEVICE SCAN ===\n");
            
            // Check for NAYAX devices first
            boolean foundNayax = false;
            for (UsbDevice device : deviceList.values()) {
                if (isNayaxDevice(device)) {
                    foundNayax = true;
                    info.append("🎯 NAYAX DEVICE FOUND: ").append(device.getDeviceName())
                        .append(" (VID: 0x").append(Integer.toHexString(device.getVendorId()))
                        .append(", PID: 0x").append(Integer.toHexString(device.getProductId()))
                        .append(")\n");
                }
            }
            
            if (!foundNayax) {
                info.append("❌ No NAYAX devices detected\n");
            }
            
            info.append("\n=== ALL CONNECTED DEVICES ===\n");
            for (UsbDevice device : deviceList.values()) {
                String deviceType = detectDeviceType(device);
                info.append(String.format("Device: %s (VID: 0x%04X, PID: 0x%04X, Type: %s)\n", 
                    device.getDeviceName(), device.getVendorId(), device.getProductId(), deviceType));
            }
            
            promise.resolve(info.toString());
        } catch (Exception e) {
            promise.reject("DEVICE_INFO_ERROR", e.getMessage());
        }
    }

    // ===== DEVICE DETECTION AND MANAGEMENT - NAYAX ONLY =====

    private UsbDevice findNayaxDevice() {
        if (usbManager == null) return null;
        
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        
        // Find NAYAX devices
        for (UsbDevice device : deviceList.values()) {
            if (isNayaxDevice(device)) {
                Log.d(TAG, "Found NAYAX device: " + device.getDeviceName() + 
                    " VID: 0x" + Integer.toHexString(device.getVendorId()) + 
                    " PID: 0x" + Integer.toHexString(device.getProductId()));
                return device;
            }
        }
        return null;
    }

    private String detectDeviceType(UsbDevice device) {
        if (isNayaxDevice(device)) {
            return DEVICE_TYPE_NAYAX;
        } else {
            return DEVICE_TYPE_GENERIC;
        }
    }

    private boolean isNayaxDevice(UsbDevice device) {
        int vid = device.getVendorId();
        int pid = device.getProductId();
        
        // Check for known NAYAX VID/PID combinations
        return (vid == NAYAX_VID_1 && pid == NAYAX_PID_1) ||
               (vid == NAYAX_VID_2 && pid == NAYAX_PID_2) ||
               (vid == NAYAX_VID_3 && pid == NAYAX_PID_3) ||
               (vid == NAYAX_VID_4 && pid == NAYAX_PID_4);
    }

    private boolean isUsbSupported() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
    }

    // ===== USB PERMISSION HANDLING =====

    private void requestUsbPermission(UsbDevice device, double amount, Promise promise) {
        final String ACTION_USB_PERMISSION = "com.givememoney.USB_PERMISSION";

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(ACTION_USB_PERMISSION), flags);

        BroadcastReceiver usbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        context.unregisterReceiver(this);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            processNayaxPayment(device, amount, promise);
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

    // ===== NAYAX PAYMENT PROCESSING =====

    private void processNayaxPayment(UsbDevice device, double amount, Promise promise) {
        try {
            Log.d(TAG, "Processing NAYAX payment for: £" + amount);
            
            UsbSerialProber prober = getCustomProber();
            List<UsbSerialDriver> drivers = prober.findAllDrivers(usbManager);
            
            for (UsbSerialDriver driver : drivers) {
                if (driver.getDevice().equals(device)) {
                    UsbDeviceConnection connection = usbManager.openDevice(device);
                    if (connection == null) {
                        promise.reject("CONNECTION_ERROR", "Could not open NAYAX device");
                        return;
                    }

                    UsbSerialPort port = driver.getPorts().get(0);
                    port.open(connection);
                    
                    // NAYAX typically uses 9600 or 38400 or 150200 baud for Multi drop bus protocol
                    port.setParameters(38400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    // Send NAYAX-specific MDB payment command
                    sendNayaxPaymentCommand(port, amount, promise);
                    return;
                }
            }
            
            promise.reject("NAYAX_DEVICE_ERROR", "Could not initialize NAYAX device (error in payment module line 314)");
            
        } catch (Exception e) {
            Log.e(TAG, "NAYAX payment error", e);
            promise.reject("NAYAX_PAYMENT_ERROR", e.getMessage());
        }
    }

    // ===== NAYAX MDB PROTOCOL IMPLEMENTATION =====

    private void sendNayaxPaymentCommand(UsbSerialPort port, double amount, Promise promise) {
        try {
            int cents = (int) (amount * 100);
            Log.d(TAG, "Sending NAYAX MDB payment command for " + cents + " cents");
            
            // NAYAX MDB (Multi-Drop Bus) protocol implementation
            // MDB is the standard protocol used by NAYAX devices
            
            // 1. Send VEND request in MDB format
            byte[] vendCommand = buildMDBVendCommand(cents);
            port.write(vendCommand, 5000);
            
            // 2. Wait for ACK from NAYAX device
            byte[] ackBuffer = new byte[1];
            int ackRead = port.read(ackBuffer, 3000);
            
            if (ackRead <= 0 || ackBuffer[0] != 0x00) { // 0x00 = MDB ACK
                promise.reject("NAYAX_NO_ACK", "NAYAX device did not acknowledge vend command");
                return;
            }
            
            // 3. Poll for payment status using MDB POLL command
            boolean paymentCompleted = false;
            int pollAttempts = 0;
            final int MAX_POLL_ATTEMPTS = 30; // 30 seconds timeout
            
            while (!paymentCompleted && pollAttempts < MAX_POLL_ATTEMPTS) {
                // Send MDB POLL command
                byte[] pollCommand = {0x11, 0x12, (byte)calculateMDBChecksum(new byte[]{0x11, 0x12})};
                port.write(pollCommand, 1000);
                
                // Read response
                byte[] pollBuffer = new byte[16];
                int pollRead = port.read(pollBuffer, 1000);
                
                if (pollRead > 0) {
                    String status = parseMDBResponse(pollBuffer, pollRead);
                    
                    if (status.equals("VEND_APPROVED")) {
                        paymentCompleted = true;
                        promise.resolve("NAYAX payment approved - £" + String.format("%.2f", amount));
                        break;
                    } else if (status.equals("VEND_DENIED")) {
                        promise.reject("NAYAX_PAYMENT_DENIED", "NAYAX payment was denied");
                        break;
                    } else if (status.equals("SESSION_TIMEOUT")) {
                        promise.reject("NAYAX_TIMEOUT", "NAYAX payment session timed out");
                        break;
                    }
                }
                
                pollAttempts++;
                Thread.sleep(1000); // Wait 1 second between polls
            }
            
            if (!paymentCompleted) {
                promise.reject("NAYAX_TIMEOUT", "NAYAX payment timed out after " + MAX_POLL_ATTEMPTS + " seconds");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "NAYAX MDB command error", e);
            promise.reject("NAYAX_COMMAND_ERROR", "NAYAX MDB error: " + e.getMessage());
        } finally {
            try {
                port.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing NAYAX port", e);
            }
        }
    }

    // Build MDB VEND command for NAYAX devices
    private byte[] buildMDBVendCommand(int cents) {
        // MDB VEND command format: [VMC_ADR][CMD][SUB][Price_H][Price_L][CHK]
        byte[] command = new byte[6];
        command[0] = 0x10; // VMC address
        command[1] = 0x13; // VEND command
        command[2] = 0x00; // VEND REQUEST subcommand
        command[3] = (byte)((cents >> 8) & 0xFF); // Price high byte
        command[4] = (byte)(cents & 0xFF); // Price low byte
        command[5] = (byte)calculateMDBChecksum(command, 5); // Checksum
        
        return command;
    }

    // Calculate MDB checksum
    private int calculateMDBChecksum(byte[] data) {
        return calculateMDBChecksum(data, data.length);
    }
    
    private int calculateMDBChecksum(byte[] data, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += data[i] & 0xFF;
        }
        return sum & 0xFF;
    }

    // Parse MDB response from NAYAX device
    private String parseMDBResponse(byte[] buffer, int length) {
        if (length == 0) return "NO_RESPONSE";
        
        // Check for standard MDB responses
        if (length == 1 && buffer[0] == 0x00) {
            return "ACK";
        }
        
        if (length >= 2) {
            int command = buffer[0] & 0xFF;
            int status = buffer[1] & 0xFF;
            
            // Check for VEND responses (0x13 command)
            if (command == 0x13) {
                switch (status) {
                    case 0x00: return "VEND_APPROVED";
                    case 0x01: return "VEND_DENIED";
                    case 0x02: return "VEND_DENIED"; // Insufficient credit
                    case 0x03: return "SESSION_TIMEOUT";
                    default: return "VEND_UNKNOWN_STATUS_" + status;
                }
            }
            
            // Check for session status (0x12 poll responses)
            if (command == 0x12) {
                switch (status) {
                    case 0x00: return "JUST_RESET";
                    case 0x01: return "READER_ENABLED";
                    case 0x02: return "CARD_INSERTED";
                    case 0x03: return "VEND_APPROVED";
                    case 0x04: return "VEND_DENIED";
                    case 0x05: return "SESSION_TIMEOUT";
                    default: return "POLL_STATUS_" + status;
                }
            }
        }
        
        return "UNKNOWN_RESPONSE";
    }

    // ===== CUSTOM USB PROBER - NAYAX FOCUSED =====

    private UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        
        // PRIORITY: NAYAX device chips (most common first)
        customTable.addProduct(0x0403, 0x6001, FtdiSerialDriver.class); // FTDI FT232R (Most NAYAX)
        customTable.addProduct(0x10C4, 0xEA60, Cp21xxSerialDriver.class); // Silicon Labs CP2102 (NAYAX)
        customTable.addProduct(0x067B, 0x2303, ProlificSerialDriver.class); // Prolific PL2303 (Older NAYAX)
        
        return new UsbSerialProber(customTable);
    }
}
