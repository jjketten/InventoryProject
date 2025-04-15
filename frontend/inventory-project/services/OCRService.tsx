// OCRService.tsx
import { useState } from 'react';

// Define the type for the OCR response
interface OcrItem {
  Name: string;
  Cost: number;
  ProductCode: string;
  Quantity: string | number;
  Units: string;
}

interface OcrResponse {
  message: {
    result: {
      output: {
        Items: OcrItem[];
      };
    };
  };
}

const OCRService = () => {
  const [items, setItems] = useState<OcrItem[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Function to process OCR API response
  const processOcrData = (ocrData: OcrResponse) => {
    const items = ocrData.message.result.output.Items.map(item => ({
      Name: item.Name,       // Make sure the key matches the OcrItem type
      Cost: item.Cost,
      ProductCode: item.ProductCode,
      Quantity: item.Quantity,
      Units: item.Units
    }));
    setItems(items);
  };

  // Function to fetch OCR data from Unstract
  const fetchOcrData = async (imageUri: string) => {
    setLoading(true);
    setError(null);

    try {
      // Example API call to Unstract OCR service
      const response = await fetch('YOUR_UNSTRACT_API_ENDPOINT', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer YOUR_API_KEY',
        },
        body: JSON.stringify({ image_uri: imageUri }), // Or any form data required
      });

      const data: OcrResponse = await response.json();

      if (data?.message?.result?.output?.Items) {
        processOcrData(data);
      } else {
        setError('Failed to process OCR data.');
      }
    } catch (err) {
      setError('An error occurred while fetching OCR data.');
    } finally {
      setLoading(false);
    }
  };

  // Return processed OCR items
  return {
    items,
    loading,
    error,
    fetchOcrData
  };
};

export default OCRService;
