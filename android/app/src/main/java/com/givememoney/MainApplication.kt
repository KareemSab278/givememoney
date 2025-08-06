package com.givememoney

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost

class MainApplication : Application(), ReactApplication {

  override val reactNativeHost: ReactNativeHost = object : DefaultReactNativeHost(this) {
    override fun getPackages(): List<ReactPackage> {
      val packages = PackageList(this).packages.toMutableList()
      packages.add(MarshallPackage()) // Add your package here
      return packages
    }

    override fun getJSMainModuleName() = "index"
    override fun getUseDeveloperSupport() = BuildConfig.DEBUG
    override val isNewArchEnabled = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
    override val isHermesEnabled = BuildConfig.IS_HERMES_ENABLED
  }

  override val reactHost: ReactHost
    get() = getDefaultReactHost(applicationContext, reactNativeHost)

  override fun onCreate() {
    super.onCreate()
    loadReactNative(this)
  }
}
