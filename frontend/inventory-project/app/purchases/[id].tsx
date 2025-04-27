// app/(tabs)/purchases/[id].tsx

import React, { useEffect, useState } from 'react';
import { ScrollView, View, StyleSheet, Alert, TouchableOpacity } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import * as DocumentPicker from 'expo-document-picker';
import * as ImagePicker    from 'expo-image-picker';
import PDFLib, { PDFDocument, PDFPage } from 'react-native-pdf-lib';
import * as FileSystem     from 'expo-file-system';
import { Text, Button, ActivityIndicator, useTheme, TextInput } from 'react-native-paper';
import GenericTable        from '@/components/GenericTable';
import { ColumnConfig }    from '@/components/ColumnConfig';
import { PurchaseDTO }     from '@/types/PurchaseDTO';
import { PurchaseItemDTO } from '@/types/PurchaseItemDTO';
import { UnstractResult }  from '@/types/UnstractResult';
import { DatePickerModal } from 'react-native-paper-dates';
import { format, isValid, parse } from 'date-fns'


export default function PurchaseDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const isNew    = id === 'new';
  const router   = useRouter();
  const { colors } = useTheme();

  const [purchase, setPurchase] = useState<PurchaseDTO>({
    purchaseID: null,
    date:       '',
    store:      '',
    totalCost:  0,
    items:      []
  });

  const [loading, setLoading] = useState<boolean>(true);
  const [datePickerOpen, setDatePickerOpen] = useState(false)

  const headers = {
    'X-Tenant-ID': 'test_schema2',
    // fetch will auto-set multipart/form-data
  };

  useEffect(() => {
    if (!isNew) {
      fetch(`http://localhost:9000/api/purchases/${id}`, { headers })
        .then(res => res.json())
        .then((dto: PurchaseDTO) => setPurchase(dto))
        .catch(console.error)
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [id]);

  // 1) Pick a file from the device and return its URI
  async function pickDocument(): Promise<string|undefined> {
    try{
      const res = await DocumentPicker.getDocumentAsync({ type: ['image/*','application/pdf'] });
      if(res.canceled) {console.log("[pickDocument] Document selection cancelled."); return;}
      const successResult = res as DocumentPicker.DocumentPickerSuccessResult;
      return successResult.assets[0].uri;

    } catch (error) {
      console.log("Error picking documents:", error);
    }
  }

  // 2) Take a photo
  async function snapPhoto(): Promise<string|undefined> {
    const cam = await ImagePicker.requestCameraPermissionsAsync();
    if (!cam.granted) {
      Alert.alert('Camera permission required');
      return;
    }
    const photo = await ImagePicker.launchCameraAsync({ quality: 0.8 });
    // if (!photo.cancelled) return photo.uri;
    if (photo.assets) {
      return photo.assets[0].uri
    } else {
      console.log("[snapPhoto] null assets array result from image picker")
    };
  }

  // 3) (optional) wrap an image URI into a one-page PDF
  async function imageToPdf(imageUri: string): Promise<string> {
    const pdfPath = `${FileSystem.cacheDirectory}receipt_${Date.now()}.pdf`;
    const page = PDFPage
      .create()
      .setMediaBox(612, 792)
      // .drawImage(imageUri, 'jpg', {
      .drawImage(imageUri, {
        x: 0, y: 0,
        width: 612,
        height: 792,
      });
    await PDFDocument
      .create(pdfPath)
      .addPages(Array.from([page]))
      .write(); // returns path
    return pdfPath;
  }

  // Used for step 4)
  function hashStringToId(str: string): number {
    let hash = 5381
    for (let i = 0; i < str.length; i++) {
      // classic djb2: hash * 33 + charCode
      /* tslint:disable:no-bitwise */
      hash = ((hash << 5) + hash) + str.charCodeAt(i)
      /* tslint:enable:no-bitwise */
    }
    return hash >>> 0   // force to unsigned
  }

  // 4) Upload the PDF to Unstract, parse response, populate state
  async function handleUploadFlow(getUri: () => Promise<string|undefined>) {
    try {
      setLoading(true);
      const picked = await getUri();
      if (!picked) return;

      // if PDF already, skip conversion
      // const isPdf = picked.toLowerCase().endsWith('.pdf');
      const isPdf = picked.toLowerCase().includes("application/pdf")
      console.log("[handleUploadFlow] picked:" + picked.toLowerCase())
      if(isPdf) {console.log("[handleUploadFlow] pdf found, skipping conversion");}
      const pdfUri = isPdf ? picked : await imageToPdf(picked);

      //blobify pdf
      const blob = await fetch(pdfUri).then(r => r.blob());

      // build multipart/form-data
      // const form = new FormData();
      // form.append('file', {
      //   uri:  pdfUri,
      //   name: 'receipt.pdf',
      //   type: 'application/pdf',
      // } as any);

      const form = new FormData();
      form.append('file', blob, 'receipt.pdf');

      const resp = await fetch('http://localhost:9000/api/unstract/upload', {
        method: 'POST',
        headers: {
          'X-Tenant-ID': 'test_schema2',
        },
        body: form,
      });

      if (!resp.ok) {
        Alert.alert('Upload failed', await resp.text());
        return;
      }

      const result: UnstractResult = await resp.json();
      // now map UnstractResult -> PurchaseDTO
      setPurchase({
        purchaseID: null,
        date:       result.date,
        store:      result.storeName,
        totalCost:  parseFloat(result.totalCost),
        items:      result.items.map(i => {
          const code = i.productCode
          const itemID = /^\d+$/.test(code)
            ? parseInt(code, 10)
            : hashStringToId(code)
          return {
          itemID:   itemID,                   
          name:     i.itemName,
          unit:     i.units,
          brand:    i.brand,
          amount:   parseInt(i.quantity, 10),
          price:    parseFloat(i.cost),
        }})
      });

    } catch (err) {
      console.error(err);
      Alert.alert('Error', 'Couldn’t process receipt');
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator />
      </View>
    );
  }

  const columns: ColumnConfig<PurchaseItemDTO>[] = [
    { key: 'itemID',    label: 'ID',       editable: isNew },
    { key: 'name',      label: 'Name',     editable: isNew },
    { key: 'brand',     label: 'Brand',    editable: isNew },
    { key: 'unit',      label: 'Unit',     editable: isNew },
    { key: 'amount',    label: 'Quantity', editable: isNew, inputType: 'number' },
    { key: 'price',     label: 'Price',    editable: isNew, inputType: 'decimal' },
  ];

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={styles.content}
    >
      <Text variant="headlineLarge">Purchase Details</Text>

      {isNew && (
        <View style={styles.buttons}>
          <Button
            mode="outlined"
            onPress={() => handleUploadFlow(pickDocument)}
            style={styles.button}
          >
            Upload Receipt
          </Button>
          <Button
            mode="outlined"
            onPress={() => handleUploadFlow(snapPhoto)}
            style={styles.button}
          >
            Use Camera
          </Button>
        </View>
      )}

      {/* DATE PICKER */}
      <TextInput
        label="Date"
        value={purchase.date}
        mode="outlined"
        editable={false}
        right={
          <TextInput.Icon
            icon="calendar"
            onPress={() => setDatePickerOpen(true)}
          />
        }
       />
      <DatePickerModal
        locale="en"
        mode="single"
        visible={datePickerOpen}
        date={
              (() => {
                const d = parse(purchase.date, 'yyyy-MM-dd', new Date());
                return isValid(d) ? d : new Date();
              })()
            }
        onDismiss={() => setDatePickerOpen(false)}
        onConfirm={({ date }) => {
          if (!date) return
          setDatePickerOpen(false)
          setPurchase((p) => ({
            ...p,
            date: format(date, 'yyyy-MM-dd'),
          }))
        }}
      />
      
      <TextInput
        label="Store"
        value={purchase.store}
        onChangeText={s => setPurchase(p => ({ ...p, store: s }))}
        mode="outlined"
        editable={isNew}
        style={styles.input}
      />
      <TextInput
        label="Total Cost"
        value={String(purchase.totalCost)}
        keyboardType="decimal-pad"
        onChangeText={c => setPurchase(p => ({ ...p, totalCost: parseFloat(c) || 0 }))}
        mode="outlined"
        editable={isNew}
        style={styles.input}
      />

      <View style={styles.tableSection}>
        <Text variant="titleMedium">Items</Text>
        <GenericTable<PurchaseItemDTO>
          data={purchase.items}
          columns={columns}
          onEdit={isNew
            ? (idx, upd) => {
                const items = [...purchase.items];
                items[idx] = upd;
                setPurchase(p => ({ ...p, items }));
              }
            : undefined
          }
        />
      </View>

      {isNew && (
        <Button
          mode="contained"
          onPress={async () => {
            // save purchase
            const res = await fetch('http://localhost:9000/api/purchases', {
              method: 'POST',
              headers: {
                ...headers,
                'Content-Type': 'application/json',
              },
              body: JSON.stringify(purchase),
            });
            if (res.ok) {
              const created: PurchaseDTO = await res.json();
              router.replace(`/purchases/${created.purchaseID}`);
            } else {
              Alert.alert('Error', await res.text());
            }
          }}
          style={{ marginTop: 24 }}
        >
          Submit Purchase
        </Button>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content:   { padding: 16, gap: 16 },
  loading:   { flex: 1, justifyContent: 'center', alignItems: 'center' },
  buttons:   { flexDirection: 'row', justifyContent: 'space-between', gap: 8 },
  button:    { flex: 1 },
  input:     { backgroundColor: 'transparent' },
  tableSection: { marginTop: 24 },
});
