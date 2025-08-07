package com.givememoney;

import android.app.Application;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import java.util.Arrays;
import java.util.List;

// Manual imports for react-native packages that need to be linked manually
// These correspond to the packages in your package.json
// import com.bastengao.usbserialport.UsbSerialportForAndroidPackage; // Not available yet

// Custom native modules
import com.givememoney.MarshallPackage;

public class PackageList {
  private Application application;
  private ReactNativeHost reactNativeHost;

  public PackageList(ReactNativeHost reactNativeHost) {
    this.reactNativeHost = reactNativeHost;
  }

  public PackageList(Application application) {
    this.application = application;
  }

  public List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new MarshallPackage()
      // Note: UsbSerialportForAndroidPackage removed due to linking issues
      // Additional packages like AsyncStorage, WebView can be added here
    );
  }
}
