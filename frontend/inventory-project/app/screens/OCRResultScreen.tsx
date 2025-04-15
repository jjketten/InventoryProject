// OcrResultScreen.tsx
import React, { useEffect } from 'react';
import { View, Text, Button, ActivityIndicator } from 'react-native';
import { OcrResultScreenNavigationProp, OcrResultScreenRouteProp } from '../types';  // Import types
import OCRService from '../../services/OCRService';

// Define the Props type for this screen using the imported types
type Props = {
  navigation: OcrResultScreenNavigationProp;
  route: OcrResultScreenRouteProp;
};

const OcrResultScreen: React.FC<Props> = ({ route, navigation }) => {
  const { imageUri } = route.params;

  const { items, loading, error, fetchOcrData } = OCRService();

  useEffect(() => {
    if (imageUri) {
      fetchOcrData(imageUri);
    }
  }, [imageUri]);

  return (
    <View>
      {loading && <ActivityIndicator size="large" />}
      {error && <Text>{error}</Text>}

      {items.length > 0 && (
        <View>
          <Text>Items Scanned:</Text>
          {items.map((item, index) => (
            <View key={index}>
              <Text>{item.Name} - {item.Cost}</Text>
            </View>
          ))}
        </View>
      )}

      <Button
        title="Add Items to Inventory"
        onPress={() => {
          // Logic to pass items to backend or move to inventory screen
          console.log(items);
        }}
      />
    </View>
  );
};

export default OcrResultScreen;
