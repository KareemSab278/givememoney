package com.givememoney

import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Toast
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate

class MainActivity : ReactActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkUsbSupport()
        handleUsbIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleUsbIntent(it) }
    }

    private fun checkUsbSupport() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            Toast.makeText(this, "USB host mode not supported", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleUsbIntent(intent: Intent) {
        if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            val device = intent.getParcelableExtra<android.hardware.usb.UsbDevice>(UsbManager.EXTRA_DEVICE)
            if (device != null) {
                Toast.makeText(this, "USB device attached: ${device.deviceName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getMainComponentName(): String = "givememoney"

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return object : ReactActivityDelegate(this, mainComponentName) {}
    }
}