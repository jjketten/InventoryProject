import { Tabs } from 'expo-router';
import React from 'react';
import { Platform } from 'react-native';

import { HapticTab } from '@/components/HapticTab';
import { IconSymbol } from '@/components/ui/IconSymbol';
import TabBarBackground from '@/components/ui/TabBarBackground';
import { Colors } from '@/constants/Colors';
import { useColorScheme } from '@/hooks/useColorScheme';

import {
  Provider as PaperProvider,
  MD3DarkTheme as PaperDarkTheme,
  MD3LightTheme as PaperLightTheme,
} from 'react-native-paper';

import { useTheme } from 'react-native-paper';

export default function TabLayout() {
  // const colorScheme = useColorScheme();
  const paperTheme = PaperDarkTheme; 
  const { colors } = useTheme(); // from react-native-paper

  return (
    <PaperProvider theme={paperTheme}>
      <Tabs
        screenOptions={{
            tabBarStyle: {
              backgroundColor: colors.surface,        // Tab bar background
            },
            tabBarActiveTintColor: colors.primary,    // Active tab icon color
            tabBarInactiveTintColor: colors.outline,  // Inactive tab icon color
            headerStyle: {
              backgroundColor: colors.background,     // Header background
            },
            headerTintColor: colors.onBackground,     // Header text/icons
            tabBarLabelStyle: {
              color: colors.onSurface,                // Optional: label text color
            },
          }}
        >
        <Tabs.Screen
          name="index"
          options={{
            title: 'Home',
            tabBarIcon: ({ color }) => <IconSymbol size={28} name="house.fill" color={color} />,
          }}
        />
        <Tabs.Screen
          name="explore"
          options={{
            title: 'Explore',
            tabBarIcon: ({ color }) => <IconSymbol size={28} name="paperplane.fill" color={color} />,
          }}
        />
        <Tabs.Screen
          name="InventoryTab"
          options={{
            title: 'Manage Inventory',
            tabBarIcon: ({ color }) => <IconSymbol size={28} name="checklist.checked" color={color} />,
          }}
        />
        <Tabs.Screen
          name="recipes"
          options={{
            title: 'Recipes',
            tabBarIcon: ({ color }) => <IconSymbol size={28} name="book.fill" color={color} />,
          }}
        />
      </Tabs>
    </PaperProvider>
  );
}
