import React from 'react';
import { View, Text } from 'react-native';
import { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import { InventoryTabParamList } from '../types'; 
import CameraComponent from '../../components/CameraComponent'; 
import { useTheme } from 'react-native-paper';

type CameraScreenProps = BottomTabScreenProps<InventoryTabParamList, 'Camera'>;

const CameraScreen: React.FC<CameraScreenProps> = ({ navigation }) => {
  const { colors } = useTheme();
  const handleCapture = (uri: string) => {
    // Navigate wherever needed — replace with actual route if in parent navigator
    console.log('Captured image URI:', uri);
    // navigation.navigate('OcrResult', { imageUri: uri }); // Uncomment if OcrResult is reachable from here
  };

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <Text style={{ textAlign: 'center', marginVertical: 10, color: colors.onBackground }}>Capture Reciept</Text>
      <CameraComponent onCapture={handleCapture} />
    </View>
  );
};

export default CameraScreen;
