import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import InventoryScreen from '../screens/InventoryScreen';
import CameraScreen from '../screens/CameraScreen';
import { InventoryTabParamList } from '../types';

import { useTheme } from 'react-native-paper';
import CategoryScreen from '../screens/CategoryScreen';
import ReminderScreen from '../screens/RemindersScreen';

const Tab = createBottomTabNavigator<InventoryTabParamList>();

const InventoryTab: React.FC = () => {
  const { colors } = useTheme(); 

  return (
    <Tab.Navigator
      screenOptions={{
        tabBarStyle: {
          backgroundColor: colors.elevation.level2,
        },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.onSurfaceVariant,
        headerStyle: {
          backgroundColor: colors.background,
        },
        headerTintColor: colors.onBackground,
      }}
    >
        <Tab.Screen name="Reminders" component={ReminderScreen} />
    </Tab.Navigator>
  );
};

export default InventoryTab;
