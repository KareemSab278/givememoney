package com.givememoney;

import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.Ch34xSerialDriver;
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProlificSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialProber;

/**
 * add devices here, that are not known to DefaultProber
 *
 * if the App should auto start for these devices, also
 * add IDs to app/src/main/res/xml/device_filter.xml
 */
class CustomProber {

    static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        
        // === FTDI Chips ===
        // Your specific device VID/PID (1027 = 0x0403, 24597 = 0x6015)
        customTable.addProduct(0x0403, 0x6015, FtdiSerialDriver.class); // FT231X/FT230X
        customTable.addProduct(0x0403, 0x6001, FtdiSerialDriver.class); // FT232R/FT232H
        customTable.addProduct(0x0403, 0x6010, FtdiSerialDriver.class); // FT2232C/D/H
        customTable.addProduct(0x0403, 0x6011, FtdiSerialDriver.class); // FT4232H
        customTable.addProduct(0x0403, 0x6014, FtdiSerialDriver.class); // FT232H
        
        // === Prolific Chips (PL2303) ===
        customTable.addProduct(0x067B, 0x2303, ProlificSerialDriver.class); // PL2303 - Very common
        customTable.addProduct(0x067B, 0x04BB, ProlificSerialDriver.class); // PL2303 variant
        
        // === Silicon Labs CP210x Chips ===
        customTable.addProduct(0x10C4, 0xEA60, Cp21xxSerialDriver.class); // CP2102/CP2109
        customTable.addProduct(0x10C4, 0xEA70, Cp21xxSerialDriver.class); // CP2105
        customTable.addProduct(0x10C4, 0xEA71, Cp21xxSerialDriver.class); // CP2108
        
        // === WinChipHead CH340/CH341 Chips ===
        customTable.addProduct(0x1A86, 0x7523, Ch34xSerialDriver.class); // CH340G - Very common in cheap cables
        customTable.addProduct(0x1A86, 0x5523, Ch34xSerialDriver.class); // CH341A
        
        // === CDC-ACM (Generic USB Serial) ===
        customTable.addProduct(0x2341, 0x0043, CdcAcmSerialDriver.class); // Arduino Uno R3
        customTable.addProduct(0x2341, 0x0001, CdcAcmSerialDriver.class); // Arduino Uno
        
        // === Custom/Unknown devices ===
        customTable.addProduct(0x1234, 0x0001, FtdiSerialDriver.class); // Example custom device
        customTable.addProduct(0x1234, 0x0002, FtdiSerialDriver.class); // Example custom device
        
        return new UsbSerialProber(customTable);
    }
}
