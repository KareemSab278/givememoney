//version 0.0.2
import React, { useEffect, useState } from 'react';
import { NativeModules, Button, View, Text, StyleSheet } from 'react-native';

type MarshallModuleType = {
  createEvent: (name: string, callback: (msg: string) => void) => void;
  createEventPromise: (name: string) => Promise<string>;
  startPayment: (amount: number) => Promise<string>;
  initSerial: () => Promise<string>;
};

const { MarshallModule } = NativeModules as {
  MarshallModule: MarshallModuleType;
};

const App = () => {
  const [message, setMessage] = useState<string>('');

  useEffect(() => {
    const timeout = setTimeout(() => {
      MarshallModule.createEvent('Test', msg => {
        setMessage(`Callback: ${msg}`);
      });

      MarshallModule.createEventPromise('TestPromise')
        .then(res => setMessage(`Promise: ${res}`))
        .catch(err => setMessage(`Error: ${err}`));
    }, 2000);

    return () => clearTimeout(timeout);
  }, []);

  return (
    <View style={{ padding: 20 }}>
      <Text>{message}</Text>

      <View style={{ padding: 10 }}>
        <Button
          title="Callback"
          onPress={() => {
            MarshallModule.createEvent('From Button', msg => {
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
              const res = await MarshallModule.createEventPromise(
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
          title="Make a Payment (0.10)"
          onPress={async () => {
            try {
              const res = await MarshallModule.startPayment(1000);
              setMessage(`Payment result: ${res}`);
            } catch (error: any) {
              setMessage(`Error: ${error.message || error}`);
            }
          }}
        />
      </View>

      <View style={{ padding: 10 }}>
        <Button
          title="Init Serial"
          onPress={async () => {
            try {
              const res = await MarshallModule.initSerial();
              setMessage(`Serial result: ${res}`);
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
