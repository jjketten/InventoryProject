import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';

// Define all your screen parameters here
export type RootStackParamList = {
  Home: undefined;
  Camera: undefined;
  OcrResult: { imageUri: string };
  Inventory: undefined;
  Recipes: undefined;
  Reminders: undefined;
  // Add any other screen names and their params here
};

// Define navigation and route prop types
export type OcrResultScreenNavigationProp = StackNavigationProp<RootStackParamList, 'OcrResult'>;
export type OcrResultScreenRouteProp = RouteProp<RootStackParamList, 'OcrResult'>;

export type InventoryTabParamList = {
  Inventory: undefined;
  Camera: undefined;
};
