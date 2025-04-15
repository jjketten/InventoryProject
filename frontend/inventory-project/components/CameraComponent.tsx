import React, { useState, useRef } from 'react';
import { View, Button, Text, ActivityIndicator, StyleSheet } from 'react-native';
import { CameraView, CameraType, useCameraPermissions } from 'expo-camera';  // Correct imports from expo-camera

interface CameraComponentProps {
  onCapture: (uri: string) => void; // onCapture takes a string (image URI) and returns nothing
}

const CameraComponent: React.FC<CameraComponentProps> = ({ onCapture }) => {
  const [facing, setFacing] = useState<CameraType>('back');  // Track the camera type (front or back)
  const [permission, requestPermission] = useCameraPermissions();  // Get camera permissions using hook
  const cameraRef = useRef<any>(null);  // Reference to the camera

  if (!permission) {
    return <ActivityIndicator size="large" />;  // Camera permissions are still loading
  }

  if (!permission.granted) {
    return (
      <View style={styles.container}>
        <Text style={styles.message}>We need your permission to show the camera</Text>
        <Button onPress={requestPermission} title="Grant Permission" />  {/* Request permission */}
      </View>
    );
  }

  const toggleCameraFacing = () => {
    setFacing(current => (current === 'back' ? 'front' : 'back'));  // Toggle between front and back cameras
  };

  const captureImage = async () => {
    if (cameraRef.current) {
      // Take the picture using the camera ref
      const photo = await cameraRef.current.takePictureAsync();  // Capture the image
      onCapture(photo.uri);  // Pass the captured image URI to the parent component
    }
  };

  return (
    <View style={styles.container}>
      <CameraView style={styles.camera} facing={facing} ref={cameraRef}>
        <View style={styles.buttonContainer}>
          <Button title="Flip Camera" onPress={toggleCameraFacing} />  {/* Flip between front and back camera */}
        </View>
      </CameraView>
      <Button title="Capture" onPress={captureImage} /> {/* Button to capture image */}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  message: {
    textAlign: 'center',
    paddingBottom: 10,
  },
  camera: {
    flex: 1,
  },
  buttonContainer: {
    position: 'absolute',
    bottom: 30,
    left: 30,
    right: 30,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default CameraComponent;
