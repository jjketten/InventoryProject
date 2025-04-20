import { useColorScheme } from '@/hooks/useColorScheme';
import { useFonts } from 'expo-font';
import * as SplashScreen from 'expo-splash-screen';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import 'react-native-reanimated';

import {
  Provider as PaperProvider,
  MD3DarkTheme as PaperDarkTheme,
  MD3LightTheme as PaperLightTheme,
} from 'react-native-paper';

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const paperTheme = PaperDarkTheme;


  const [loaded] = useFonts({
    SpaceMono: require('../assets/fonts/SpaceMono-Regular.ttf'),
  });

  // const paperTheme = colorScheme === 'dark' ? PaperDarkTheme : PaperLightTheme;

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync();
    }
  }, [loaded]);

  if (!loaded) return null;

  return (
    <PaperProvider theme={paperTheme}>
      <Stack>
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="+not-found" />
      </Stack>
      <StatusBar style="auto" />
    </PaperProvider>
  );
}
