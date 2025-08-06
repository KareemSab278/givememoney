package com.givememoney

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader

class MainApplication : Application(), ReactApplication {

  override val reactNativeHost: ReactNativeHost = object : DefaultReactNativeHost(this) {
    override fun getPackages(): List<ReactPackage> {
      val packages = PackageList(this).packages.toMutableList()
      packages.add(MyAppPackage())
      return packages
    }

    override fun getJSMainModuleName() = "index"
    override fun getUseDeveloperSupport() = BuildConfig.DEBUG
    override val isNewArchEnabled = false
    override val isHermesEnabled = BuildConfig.IS_HERMES_ENABLED
  }

  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, false)
  }
}
