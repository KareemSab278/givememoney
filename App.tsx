//version 1.0.0 - NAYAX-ONLY Payment Module (Marshall removed)
import React, { useEffect, useState } from 'react';
import { NativeModules, Button, View, Text, StyleSheet } from 'react-native';
// import { UsbSerialManager, Parity, Codes } from "react-native-usb-serialport-for-android";

type PaymentModuleType = {
  createEvent: (name: string, callback: (msg: string) => void) => void;
  createEventPromise: (name: string) => Promise<string>;
  startPayment: (amount: number) => Promise<string>;
  startNayaxPayment: (amount: number) => Promise<string>;
  initSerial: () => Promise<string>;
  getDeviceInfo: () => Promise<string>;
};

const { PaymentModule } = NativeModules as {
  PaymentModule: PaymentModuleType;
};

const App = () => {
  const [message, setMessage] = useState<string>('');

  useEffect(() => {
    const timeout = setTimeout(() => {
      if (PaymentModule) {
        PaymentModule.createEvent('Test', msg => {
          setMessage(`Callback: ${msg}`);
        });

        PaymentModule.createEventPromise('TestPromise')
          .then(res => setMessage(`Promise: ${res}`))
          .catch(err => setMessage(`Error: ${err}`));
      } else {
        setMessage('PaymentModule not available');
      }
    }, 2000);

    return () => clearTimeout(timeout);
  }, []);

  // USB functionality commented out until USB Serial package is properly linked
  /*
  useEffect(() => {
    const runUsb = async () => {
      try {
        const devices = await UsbSerialManager.list();
        await UsbSerialManager.tryRequestPermission(2004);
        const usbSerialport = await UsbSerialManager.open(2004, { baudRate: 38400, parity: Parity.None, dataBits: 8, stopBits: 1 });

        const sub = usbSerialport.onReceived((event) => {
          console.log(event.deviceId, event.data);
        });

        await usbSerialport.send('00FF');
        usbSerialport.close();
      } catch (err) {
        console.log(err);
        if (err.code === Codes.DEVICE_NOT_FOND) {
          // handle not found
        }
      }
    };
    runUsb();
  }, []);
  */

  return (
    <View style={{ padding: 20 }}>
      <Text>{message}</Text>

      <View style={{ padding: 10 }}>
        <Button
          title="Callback"
          onPress={() => {
            PaymentModule.createEvent('From Button', msg => {
              setMessage(`Button Callback: ${msg}`);
            });
          }}
        />
      </View>

      <View style={{ padding: 10 }}>
        <Button
          title="Promise"
          onPress={async () => {
            try {
              const res = await PaymentModule.createEventPromise(
                'From Button',
              );
              setMessage(`Button Promise: ${res}`);
            } catch (e) {
              setMessage(`Error: ${e}`);
            }
          }}
        />
      </View>

      <View style={{ padding: 10 }}>
        <Button
          title="🎯 NAYAX Payment (£0.10)"
          onPress={async () => {
            try {
              const res = await PaymentModule.startPayment(0.10);
              setMessage(`Payment result: ${res}`);
            } catch (error: any) {
              setMessage(`Error: ${error.message || error}`);
            }
          }}
        />
      </View>

      <View style={{ padding: 10 }}>
        <Button
          title="Nayax Payment (£0.10)"
          onPress={async () => {
            try {
              const res = await PaymentModule.startNayaxPayment(0.10);
              setMessage(`Nayax Payment: ${res}`);
            } catch (error: any) {
              setMessage(`Nayax Error: ${error.message || error}`);
            }
          }}
        />
      </View>

      <View style={{ padding: 10 }}>
        <Button
          title="Init Serial"
          onPress={async () => {
            try {
              const res = await PaymentModule.initSerial();
              setMessage(`Serial result: ${res}`);
            } catch (error: any) {
              setMessage(`Error: ${error.message || error}`);
            }
          }}
        />
      </View>

      <View style={{ padding: 10 }}>
        <Button
          title="Get Device Info"
          onPress={async () => {
            try {
              const res = await PaymentModule.getDeviceInfo();
              setMessage(`Device Info: ${res}`);
            } catch (error: any) {
              setMessage(`Error: ${error.message || error}`);
            }
          }}
        />
      </View>
    </View>
  );
};

export default App;
