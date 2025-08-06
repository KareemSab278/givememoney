//import java.util.ArrayList;
// javac -source 8 -target 8 -cp "lib\Marshall\*" -d bin $files // always compile before run
// to run program write java -cp "bin;lib\Marshall\*" App in terminal
package com.givememoney; // forgot this line and lost 5 minutes waiting for it to fail bruh

import com.bitmick.marshall.models.pc_port;
import com.bitmick.marshall.models.*;
import com.bitmick.marshall.vmc.*;
//import com.bitmick.marshall.vmc.vmc_vend_t.vend_item_t;
//import com.bitmick.marshall.vmc.vmc_vend_t.vend_session_data_t;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;
import android.content.Context;
import java.util.HashMap;

public class App {

    static vmc_vend_t.vend_session_t m_session;
    private static int m_runninng_session_id = 0;

    public static int VENDOR_ID = 1027; // decimal for 0x0403
    public static int PRODUCT_ID = 24597; // decimal for 0x6015
    // if these dont work then use hexa instead.

    public static class MyVendCallbacks implements vmc_vend_t.vend_callbacks_t {

        @Override
        public void onReady(vmc_vend_t.vend_session_t session) {
            System.out.println("🔔 Vend Ready callback");

            if (session != null) {
                System.out.println("Session Status = " + session.session_status);
            } else {
                System.out.println("Session is null at this stage.");
            }
        }

        @Override
        public void onSessionBegin(int sessionId) {
            System.out.println("🛎️ Session Begin: ID = " + sessionId);
        }

        @Override
        public void onTransactionInfo(vmc_vend_t.vend_session_data_t data) {
            System.out.println("💳 Transaction Info: Transaction ID = " + data.transaction_id);
        }

        @Override
        public boolean onVendApproved(vmc_vend_t.vend_session_t session) {
            System.out.println("✅ Vend Approved: Funds available = " + session.funds_avail);
            return true;
        }

        @Override
        public void onVendDenied(vmc_vend_t.vend_session_t session) {
            System.out.println("❌ Vend Denied: Session Status = " + session.session_status);
        }

        @Override
        public void onSettlement(boolean success) {
            System.out.println("💰 Settlement: " + (success ? "Success" : "Failure"));
        }

        @Override
        public void onSessionTimeout(int sessionId) {
            System.out.println("⏰ Session Timeout: ID = " + sessionId);
        }

        @Override
        public void onStatus(int statusCode, byte[] data) {
            System.out.println("📊 Status Update: Code = " + statusCode + ", Data length = " + data.length);
        }

        @Override
        public void onOpenedSessions(short[] sessions) {
            System.out.print("📂 Opened Sessions: ");
            for (short s : sessions) {
                System.out.print(s + " ");
            }
            System.out.println();
        }

        @Override
        public void onReaderState(boolean state) {
            System.out.println("📶 Reader State: " + (state ? "ON" : "OFF"));
        }

        @Override
        public void onRemoteVend(short vendId, short productCode, int quantity) {
            System.out.println("🚀 Remote Vend: Vend ID = " + vendId + ", Product Code = " + productCode + ", Quantity = " + quantity);
        }

        @Override
        public void onReceipt(int code, String receipt) {
            System.out.println("🧾 Receipt for code " + code + ": " + receipt);
        }
    }

    // 2. VMC link events
    public static class MyVmcLinkEvents implements vmc_link.vmc_link_events_t {

        @Override
        public void onReady(vmc_link.vpos_config_t config) {
            System.out.println("VMC is ready. VPOS Config: " + config);

            // Start session after registration is done
            vmc_framework.getInstance().vend.session_start(vmc_vend_t.session_type_credit_e);
            //m_session = new vmc_vend_t.vend_session_t(2000, 1, (byte) 0, 10);

            //SET UP PAYMENT
            m_session = new vmc_vend_t.vend_session_t((short) m_runninng_session_id++, 1, (byte) 0, (short) 10);

            //SEND PAYMENT TO MACHINE
            vmc_framework.getInstance().vend.vend_request(m_session);

        }

        @Override
        public void onCommError() {
            System.err.println("Communication Error with VMC!");
        }
    }

    public static void main(String[] args) {

        //UNit Congiguration
        vmc_configuration vmc_config = new vmc_configuration();
        vmc_framework m_vmc = vmc_framework.getInstance();

        vmc_config.model = "SmartFridge-marshall-java-sdk";
        vmc_config.serial = "01234567";
        vmc_config.hw_ver = "1.0";
        vmc_config.manuf_code = "Coinadrink";

        vmc_config.multi_vend_support = false;
        vmc_config.multi_session_support = false;
        vmc_config.price_not_final_support = false;
        vmc_config.reader_always_on = false;
        vmc_config.always_idle = true;
        vmc_config.vend_denied_policy = vmc_configuration.vend_denied_policy_cancel;

        vmc_config.mifare_approved_by_vmc_support = false;
        vmc_config.mag_card_approved_by_vmc_support = false;

        vmc_config.dump_packets_level = vmc_configuration.debug_level_dump_moderate;
        vmc_config.debug = true;

        //Define USB PORT to SERIAL
        // m_vmc.link.set_serial_port(new pc_port("/dev/tty.usbserial-FT1RAJGY", 115200)); // MAC OS port
        m_vmc.link.set_serial_port(new pc_port("COM3", 115200)); // WINDOWS OS PORT (change depending on where device is plugged)

        m_vmc.link.configure(vmc_config);
        m_vmc.link.set_events(new MyVmcLinkEvents());

        // 🔁 Register your vend callbacks BEFORE calling session_start
        m_vmc.vend.register_callbacks(new MyVendCallbacks());

        //Start Session
        m_vmc.start();

        System.out.println("Nayax Script Reached End!");
    }

    public static void startPayment(short amount) {
        try {
            vmc_configuration vmc_config = new vmc_configuration();
            vmc_framework m_vmc = vmc_framework.getInstance();

            vmc_config.model = "SmartFridge-marshall-java-sdk";
            vmc_config.serial = "01234567";
            vmc_config.hw_ver = "1.0";
            vmc_config.manuf_code = "Coinadrink";
            vmc_config.multi_vend_support = false;
            vmc_config.multi_session_support = false;
            vmc_config.price_not_final_support = false;
            vmc_config.reader_always_on = false;
            vmc_config.always_idle = true;
            vmc_config.vend_denied_policy = vmc_configuration.vend_denied_policy_cancel;
            vmc_config.mifare_approved_by_vmc_support = false;
            vmc_config.mag_card_approved_by_vmc_support = false;
            vmc_config.dump_packets_level = vmc_configuration.debug_level_dump_moderate;
            vmc_config.debug = true;

            m_vmc.link.set_serial_port(new pc_port("COM3", 115200));
            m_vmc.link.configure(vmc_config);
            m_vmc.link.set_events(new MyVmcLinkEvents());
            m_vmc.vend.register_callbacks(new MyVendCallbacks());

            // Start session for payment
            m_session = new vmc_vend_t.vend_session_t((short) 1, 1, (byte) 0, amount);
            m_vmc.vend.vend_request(m_session);
            m_vmc.start();
        } catch (Exception e) {
            System.err.println("Payment error: " + e.getMessage());
        }
    }

    public static void startPayment(short amount, Context context) {
        try {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

            UsbDevice paymentDevice = null;
            for (UsbDevice device : deviceList.values()) {
                if (device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID) {
                    paymentDevice = device;
                    break;
                }
            }

            for (UsbDevice device : deviceList.values()) {
                System.out.println("Vendor ID: " + device.getVendorId() + ", Product ID: " + device.getProductId());
            }

            if (paymentDevice == null) {
                System.err.println("Payment device not found!");
                return;
            }

            String devicePath = paymentDevice.getDeviceName(); // i might need device file path or name idk

            vmc_configuration vmc_config = new vmc_configuration();
            vmc_framework m_vmc = vmc_framework.getInstance();

            // Set up config fields (copy from main)
            vmc_config.model = "SmartFridge-marshall-java-sdk";
            vmc_config.serial = "01234567";
            vmc_config.hw_ver = "1.0";
            vmc_config.manuf_code = "Coinadrink";
            vmc_config.multi_vend_support = false;
            vmc_config.multi_session_support = false;
            vmc_config.price_not_final_support = false;
            vmc_config.reader_always_on = false;
            vmc_config.always_idle = true;
            vmc_config.vend_denied_policy = vmc_configuration.vend_denied_policy_cancel;
            vmc_config.mifare_approved_by_vmc_support = false;
            vmc_config.mag_card_approved_by_vmc_support = false;
            vmc_config.dump_packets_level = vmc_configuration.debug_level_dump_moderate;
            vmc_config.debug = true;

            m_vmc.link.set_serial_port(new pc_port(devicePath, 115200));
            m_vmc.link.configure(vmc_config);
            m_vmc.link.set_events(new MyVmcLinkEvents());
            m_vmc.vend.register_callbacks(new MyVendCallbacks());

            m_session = new vmc_vend_t.vend_session_t((short) 1, 1, (byte) 0, amount);
            m_vmc.vend.vend_request(m_session);
            m_vmc.start();
        } catch (Exception e) {
            System.err.println("Payment error: " + e.getMessage());
        }
    }
}
