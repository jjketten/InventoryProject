// InventoryTab.tsx

import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import InventoryScreen from '../screens/InventoryScreen'; // replace with your actual screen
import CameraScreen from '../screens/CameraScreen';
import { InventoryTabParamList } from '../types'; // adjust path if needed

const Tab = createBottomTabNavigator<InventoryTabParamList>();

const InventoryTab: React.FC = () => {
  return (
    <Tab.Navigator>
      <Tab.Screen name="Inventory" component={InventoryScreen} />
      <Tab.Screen name="Camera" component={CameraScreen} />
    </Tab.Navigator>
  );
};

export default InventoryTab;
