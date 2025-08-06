public class MarshallModule extends ReactContextBaseJavaModule {

    private final UsbManager usbManager;
    ReactApplicationContext context;

    public MarshallModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        usbManager = (UsbManager) reactContext.getSystemService(Context.USB_SERVICE);
    }

    @ReactMethod
    public void startPayment(double amount, Promise promise) {
        UsbDevice device = findUsbDevice();
        if (device == null) {
            promise.reject("NO_DEVICE", "Payment device not found");
            return;
        }

        if (!usbManager.hasPermission(device)) {
            final String ACTION_USB_PERMISSION = "com.givememoney.USB_PERMISSION";
            PendingIntent permissionIntent = PendingIntent.getBroadcast(
                getReactApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);

            BroadcastReceiver usbReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {
                        synchronized (this) {
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                try {
                                    short cents = (short) (amount * 100);
                                    App.startPayment(cents, getReactApplicationContext());
                                    promise.resolve("Payment started after permission");
                                } catch (Exception e) {
                                    promise.reject("PAYMENT_ERR", e);
                                }
                            } else {
                                promise.reject("PERMISSION_DENIED", "USB permission denied");
                            }
                            getReactApplicationContext().unregisterReceiver(this);
                        }
                    }
                }
            };

            getReactApplicationContext().registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
            usbManager.requestPermission(device, permissionIntent);
        } else {
            try {
                short cents = (short) (amount * 100);
                App.startPayment(cents, getReactApplicationContext());
                promise.resolve("Payment started");
            } catch (Exception e) {
                promise.reject("PAYMENT_ERR", e);
            }
        }
    }

    private UsbDevice findUsbDevice() {
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getVendorId() == 1027 && device.getProductId() == 24533) { // Your vendor/product IDs
                return device;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "MarshallModule";
    }
}
