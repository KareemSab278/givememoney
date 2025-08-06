// /**
//  * Sample React Native App
//  * https://github.com/facebook/react-native
//  *
//  * @format
//  */

// import { NewAppScreen } from '@react-native/new-app-screen';
// import { StatusBar, StyleSheet, useColorScheme, View } from 'react-native';

// function App() {
//   const isDarkMode = useColorScheme() === 'dark';

//   return (
//     <View style={styles.container}>
//       <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
//       <NewAppScreen templateFileName="App.tsx" />
//     </View>
//   );
// }

// const styles = StyleSheet.create({
//   container: {
//     flex: 1,
//   },
// });

// export default App;

// version 0.0.1
// import React, {useEffect} from 'react';
// import {NativeModules, Button, View} from 'react-native';

// type MarshallModuleType = {
//   createEvent: (name: string, callback: (msg: string) => void) => void;
//   createEventPromise: (name: string) => Promise<string>;
// };

// const {MarshallModule} = NativeModules as {
//   MarshallModule: MarshallModuleType;
// };

// const App = () => {
//   useEffect(() => {
//     MarshallModule.createEvent("Test", (msg) => {
//       console.log("Callback:", msg);
//     });

//     MarshallModule.createEventPromise("TestPromise")
//       .then(res => console.log("Promise:", res))
//       .catch(err => console.error(err));
//   }, []);

//   return (
//     <View>
//       <Button title="Callback" onPress={() => {
//         MarshallModule.createEvent("From Button", (msg) => {
//           console.log("Button Callback:", msg);
//         });
//       }} />
//       <Button title="Promise" onPress={async () => {
//         try {
//           const res = await MarshallModule.createEventPromise("From Button");
//           console.log("Button Promise:", res);
//         } catch (e) {
//           console.error(e);
//         }
//       }} />
//     </View>
//   );
// };

// export default App;

//version 0.0.2
import React, { useEffect, useState } from 'react';
import { NativeModules, Button, View, Text, StyleSheet } from 'react-native';

type MarshallModuleType = {
  createEvent: (name: string, callback: (msg: string) => void) => void;
  createEventPromise: (name: string) => Promise<string>;
};

const { MarshallModule } = NativeModules as {
  MarshallModule: MarshallModuleType;
};

const App = () => {
  const [message, setMessage] = useState<string>('');

  useEffect(() => {
    MarshallModule.createEvent('Test', msg => {
      setMessage(`Callback: ${msg}`);
    });

    MarshallModule.createEventPromise('TestPromise')
      .then(res => setMessage(`Promise: ${res}`))
      .catch(err => setMessage(`Error: ${err}`));
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
              const res = await MarshallModule.createEventPromise(
                'PAYMENT:0.10',
              );
              setMessage(`Payment Result: ${res}`);
            } catch (e) {
              setMessage(`Error: ${e}`);
            }
          }}
        />
      </View>
    </View>
  );
};

export default App;
