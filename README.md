# **GiveMeMoney - Payment Terminal Integration**

## **Project Overview**
React Native Android app that integrates with USB payment terminals (Nayax and generic devices) for processing payments.

---

## **📋 Requirements**

### **Development Environment:**
- **Node.js**: 16.x or 18.x
- **Java**: JDK 17.0.2 (REQUIRED - exact version)
- **Android Studio**: Latest version (for SDK and emulator)
- **React Native CLI**: `npm install -g react-native-cli`
- **ADB (Android Debug Bridge)**: For device communication and debugging

### **System Requirements:**
- **Windows 10/11** (current setup)
- **Android SDK API 33** (minimum API 21)
- **USB Host Support** on target Android device

### **Hardware:**
- **Android device** with USB OTG/Host capability
- **Payment terminals**: Marshall, Nayax, or generic USB serial devices
- **USB cables** (obviously...)

---

## **🚀 Setup Instructions**

### **1. Clone Repository**
```bash
git clone https://github.com/KareemSab278/givememoney.git
cd givememoney
```

### **2. Install Dependencies**
```bash
npm install
```

### **3. Environment Setup**

#### **Java Setup (CRITICAL):**
```powershell
# Verify Java 17.0.2 is installed (EXACT VERSION REQUIRED)
java --version

# Set JAVA_HOME (different on linux and bigmac)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

**⚠️ IMPORTANT**: Only Java 17.0.2 is supported. Other versions may cause build failures.

#### **Android SDK + ADB Setup:**
1. Install **Android Studio** (includes ADB automatically)
2. Open Android Studio → SDK Manager
3. Install:
   - **Android SDK Platform 33**
   - **Android SDK Build-Tools 33.0.0+**
   - **Android SDK Platform-Tools** (includes ADB)

4. Set environment variables:
```powershell
$env:ANDROID_HOME = "C:\Users\[username]\AppData\Local\Android\Sdk"
$env:PATH = "$env:ANDROID_HOME\platform-tools;$env:PATH"
```

5. **Verify ADB Installation:**
```powershell
adb version
# Should show ADB version and confirm installation
```

### **4. Device Setup & Testing**

#### **Android Device Requirements:**
- **USB OTG/Host support** enabled
- **Developer options** enabled
- **USB debugging** enabled
- **ADB connection** working

#### **Verify Device Connection:**
```bash
# Connect Android device via USB
adb devices
# Should list your device with "device" status
```

### **5. Build Project**
```bash
# Build Android APK
cd android
./gradlew assembleDebug

# Or build and run on device (recommended)
cd ..
npx react-native run-android
```

---

## **📱 Project Structure**

### **Key Files (5 total):**
```
android/app/src/main/java/com/givememoney/
├── PaymentModule.java          # Main payment logic & USB communication
├── PaymentPackage.java         # React Native bridge registration  
├── MainActivity.java           # USB intents + React Native lifecycle
├── MainApplication.java        # App initialization
└── PackageList.java           # Package registration

App.tsx                        # React Native UI and payment buttons
```

### **Consolidated Architecture:**
- **PaymentModule.java**: All payment logic, USB handling, multi-device support
- **App.tsx**: Simple UI with payment testing buttons
- **Clean bridge**: Single module, no conflicts

---

## **🔧 Building & Testing**

### **Prerequisites Check:**
```bash
# Verify all requirements before building
java --version          # Must be 17.0.2
adb version            # Must show ADB installed
adb devices            # Must show connected Android device
```

### **Debug Build:**
```bash
cd android
./gradlew assembleDebug
```

### **Release Build:**
```bash
cd android  
./gradlew assembleRelease
```

### **Run on Device:**
```bash
# Connect Android device via USB and enable USB debugging
adb devices              # Verify device connected
npx react-native run-android
```

### **View Logs:**
```bash
npx react-native log-android
# OR
adb logcat -s ReactNativeJS (recommended)
```

---

## **💳 Payment Device Support**

### **Supported Devices:**
- **Marshall Payment Terminals** (VID: 1027, PID: 24597)
- **Nayax Payment Devices** (Various VID/PIDs) 
- **Generic USB Serial** payment devices

### **Protocols:** (i have no clue what these mean)
- **Marshall**: VMC framework protocol (115200 baud)
- **Nayax**: Generic serial protocol (38400 baud) 
- **Generic**: Configurable baud rates

---

## **🎯 Testing the App**

### **Available Functions:**
1. **"Callback"** - Test React Native bridge callbacks
2. **"Promise"** - Test Promise-based communication
3. **"Make a Payment (£0.10)"** - Process test payment
4. **"Init Serial"** - Initialize USB serial connection
5. **"Get Device Info"** - List connected USB devices

### **Testing Steps:**
1. **Verify device connection**: `adb devices`
2. **Connect payment device** via USB to Android device
3. **Launch app**: `npx react-native run-android`
4. **Test device detection**: Tap "Get Device Info"
5. **Initialize connection**: Tap "Init Serial"
6. **Test payment**: Tap "Make a Payment (£0.10)"

### **Debugging:**
```bash
# Monitor logs during testing
npx react-native log-android

# Or filter for PaymentModule logs
adb logcat -s PaymentModule
```

---

## **⚠️ Troubleshooting**

### **Common Issues:**

#### **"JAVA_HOME not set" or "Wrong Java version"**
```powershell
# Install Java 17.0.2 EXACTLY - no other version
java --version  # Must show 17.0.2
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

#### **"Android SDK not found"**
```powershell
$env:ANDROID_HOME = "C:\Users\[username]\AppData\Local\Android\Sdk"
```

#### **"ADB not found" or "Device not detected"**
```bash
# Install Android Studio (includes ADB)
# OR manually add to PATH:
$env:PATH = "$env:ANDROID_HOME\platform-tools;$env:PATH"

# Test ADB
adb version
adb devices
```

#### **"PaymentModule not available"**
- Rebuild project: `./gradlew clean assembleDebug`
- Check React Native logs: `npx react-native log-android`
- Verify device connected: `adb devices`

#### **USB Permission Denied**
- Ensure USB OTG/Host enabled on Android device
- Check USB cable supports data transfer
- Grant USB permissions when prompted in app
- Enable Developer Options and USB Debugging

#### **Build Failures**
```bash
# Clean and rebuild
cd android
./gradlew clean
./gradlew assembleDebug

# Check Java version
java --version  # Must be exactly 17.0.2
```

---

## **🔄 Development Workflow**

### **Before Starting Development:**
1. **Verify environment**:
   ```bash
   java --version     # 17.0.2
   adb version       # Working
   adb devices       # Android device connected
   ```

2. **Test base build**:
   ```bash
   ./gradlew assembleDebug
   npx react-native run-android
   ```

### **Making Changes:**
1. Edit code in Android Studio or VS Code
2. Rebuild: `./gradlew assembleDebug`
3. Test on device: `npx react-native run-android`
4. Monitor logs: `npx react-native log-android`

### **Adding New Payment Devices:**
1. Update VID/PID detection in `PaymentModule.java`
2. Add device-specific protocol in `processXXXPayment()` method
3. Test with actual hardware using ADB debugging

---

## **📝 Version Info**
- **Current Version**: 0.0.9
- **React Native**: 0.71.x
- **Target Android**: API 33
- **Min Android**: API 21
- **Java**: 17.0.2 (REQUIRED)

---

## **👥 Team Development**

### **Environment Verification Checklist:**
- [ ] Java 17.0.2 installed (`java --version`)
- [ ] Android Studio installed
- [ ] ADB working (`adb version`)
- [ ] Android device connected (`adb devices`)
- [ ] USB debugging enabled on device
- [ ] Project builds successfully (`./gradlew assembleDebug`)
- [ ] App launches on device (`npx react-native run-android`)

### **Code Changes:**
- **Payment logic**: Edit `PaymentModule.java`
- **UI changes**: Edit `App.tsx`
- **New devices**: Update `detectDeviceType()` in PaymentModule

### **Testing Protocol:**
1. Always verify device connection with `adb devices`
2. Test with actual USB payment devices
3. Monitor Android logs during testing
4. Verify all 5 app buttons work correctly
5. Test USB permission handling

### **Required Tools for Development:**
- **ADB**: Essential for device communication and debugging
- **Java 17.0.2**: Exact version required for builds
- **Android device**: With USB Host support for payment terminals

---

**Ready to build payments! 🚀💳**

**Note**: This project requires physical Android devices with USB Host capability for payment terminal testing. Emulators cannot test USB payment device functionality.
