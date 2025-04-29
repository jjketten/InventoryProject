import React, { useEffect, useState } from 'react';
import { ScrollView, View, StyleSheet, Alert, Platform } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import * as DocumentPicker from 'expo-document-picker';
import * as ImagePicker from 'expo-image-picker';
import PDFLib, { PDFDocument, PDFPage } from 'react-native-pdf-lib';
import * as FileSystem from 'expo-file-system';
import { Text, Button, ActivityIndicator, useTheme, TextInput } from 'react-native-paper';
import GenericTable from '@/components/GenericTable';
import { ColumnConfig } from '@/components/ColumnConfig';
import { DatePickerModal, TimePickerModal } from 'react-native-paper-dates';
import { format, isValid, parse, parseISO } from 'date-fns';
import { APIURL, TENANTID } from '../config';
import CameraModal from '@/components/CameraModal';
import { PurchaseDTO } from '@/types/PurchaseDTO';
import { PurchaseItemDTO } from '@/types/PurchaseItemDTO';
import { Reminder as ReminderDTO } from '@/types/reminder'; // Make sure you have ReminderDTO imported
import { UnstractItem } from '@/types/UnstractResult';
import { PDFDocument as PDFDocumentWeb } from 'pdf-lib';
import { Dialog, Portal, Checkbox } from 'react-native-paper';

interface ExtendedPurchaseItemDTO extends PurchaseItemDTO {
  reminderDateTime?: string;
  reminderDescription?: string;
}

export default function PurchaseDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const isNew = id === 'new';
  const router = useRouter();
  const { colors } = useTheme();

  const [purchase, setPurchase] = useState<PurchaseDTO<ExtendedPurchaseItemDTO>>({
    purchaseID: null,
    date: '',
    store: '',
    totalCost: 0,
    items: [],
  });
  const [reminders, setReminders] = useState<ReminderDTO[]>([]);

  const [cameraOpen, setCameraOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [datePickerOpen, setDatePickerOpen] = useState(false);
  const [reminderModalOpen, setReminderModalOpen] = useState(false);
  const [timeModalOpen, setTimeModalOpen] = useState(false);
  const [tempReminderDate, setTempReminderDate] = useState<Date | null>(null);
  const [reminderTargetIndex, setReminderTargetIndex] = useState<number | null>(null);
  const [totalCostText, setTotalCostText] = useState("");
  const [deleteItemsToo, setDeleteItemsToo] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);


  const headers = { 'X-Tenant-ID': TENANTID };

  useEffect(() => {
    if (!isNew) {
      Promise.all([
        fetch(`${APIURL}/purchases/${id}`, { headers }).then(res => res.json()),
        fetch(`${APIURL}/reminders`, { headers }).then(res => res.json())
      ])
      .then(([purchaseData, remindersData]) => {
        setPurchase(purchaseData);
        setReminders(remindersData.filter((r: ReminderDTO) => r.purchaseID === purchaseData.purchaseID));
      })
      .catch(console.error)
      .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [id]);

  function handleAddRow() {
    setPurchase(prev => ({
      ...prev,
      items: [
        ...prev.items,
        {
          itemID: 0,
          name: '',
          brand: '',
          categories: [],
          unit: '',
          amount: 0,
          price: 0,
        },
      ],
    }));
    setReminders(prev => (
      [...prev,{
        itemID: null,
        itemName: undefined,
        itemBrand: undefined,
        itemUnit: "?",
        itemAmount: 0,
        purchaseID: null,
        // date: (parse(purchase.date, 'yyyy-MM-dd', new Date())).toISOString(),
        purchaseDate: "?",
        purchaseStore: "?",
        purchaseTotalCost: 0,
        dateTime : "?",
        description: "",
        completed: false,
      }]
    ));
  }

  function handleDeleteRow(index: number) {
    const updatedItems = [...purchase.items];
    updatedItems.splice(index, 1);
    setPurchase(prev => ({ ...prev, items: updatedItems }));
  }

  function handleEditRow(index: number, updatedItem: ExtendedPurchaseItemDTO) {
    const updated = [...purchase.items];
    updated[index] = updatedItem;
    setPurchase(prev => ({ ...prev, items: updated }));
  }

  function handleRequestAddReminder(index: number) {
    setReminderTargetIndex(index);
    setReminderModalOpen(true);
  }

  function handleConfirmReminder(index: number,dateTime: Date) {
    const item = purchase.items[index];
    const newReminder: ReminderDTO = {
      itemID: item.itemID!,
      itemName: item.name!,
      itemBrand: item.brand!,
      itemUnit: item.unit,
      itemAmount: item.amount,
      purchaseID: purchase.purchaseID!,
      // date: (parse(purchase.date, 'yyyy-MM-dd', new Date())).toISOString(),
      purchaseDate: purchase.date,
      purchaseStore: purchase.store,
      purchaseTotalCost: purchase.totalCost,
      dateTime : (dateTime.toISOString()),
      description: (item.reminderDescription ? item.reminderDescription : ""),
      completed: false,
    };
    setPurchase(prev => {
      const items = [...prev.items];
      items[index] = {
        ...items[index],
        reminderDateTime:dateTime.toISOString(),
      };
      return { ...prev, items };
    });
    // setReminders(prev => [...prev, newReminder]);
    setReminders(prev => {
      // const rmdrs = [...prev]
      // // const cur = rmdrs[index];
      // rmdrs[index] = newReminder;
      // return(rmdrs)
      // // const updated = [...purchase.items];
      // // updated[index] = updatedItem;
      // // setPurchase(prev => ({ ...prev, items: updated }));
      return(prev.with(index, newReminder))
    });
    setReminderModalOpen(false);
    setTimeModalOpen(false);
    setReminderTargetIndex(null);
  }

  async function handleDeleteConfirmed() {
    try {
      if (!purchase.purchaseID) {
        throw new Error('Invalid purchase ID');
      }
  
      //delete the purchase
      await fetch(`${APIURL}/purchases/${purchase.purchaseID}`, {
        method: 'DELETE',
        headers,
      });
  
      if (deleteItemsToo) {
        //delete associated items
        for (const item of purchase.items) {
          if (item.itemID) {
            await fetch(`${APIURL}/items/${item.itemID}`, {
              method: 'DELETE',
              headers,
            });
          }
        }
      }
  
      setDeleteDialogOpen(false);
      router.back(); //navigate back to the purchase list
    } catch (error) {
      console.error('[handleDeleteConfirmed] Failed to delete:', error);
      if (Platform.OS !== 'web') {
        Alert.alert('Error', 'Failed to delete purchase.');
      } else {
        window.alert('Failed to delete purchase.');
      }
      setDeleteDialogOpen(false);
    }
  }
  

  async function pickDocument(): Promise<string | undefined> {
    try {
      const res = await DocumentPicker.getDocumentAsync({ type: ['image/*', 'application/pdf'] });
      if (res.canceled) return;
      return res.assets[0].uri;
    } catch (error) {
      console.error(error);
    }
  }

  async function snapPhoto(): Promise<string | undefined> {
    if (Platform.OS === 'web') {
      setCameraOpen(true);
      return undefined;
    } else {
      const cam = await ImagePicker.requestCameraPermissionsAsync();
      if (!cam.granted) {
        Alert.alert('Camera permission required');
        return;
      }
      const photo = await ImagePicker.launchCameraAsync({ quality: 0.8 });
      return photo.assets?.[0]?.uri;
    }
  }

  async function handlePhotoCaptured(uri: string) {
    if (!uri) return;
    console.log('[handlePhotoCaptured] URI:', uri);
    await handleUploadFlow(() => Promise.resolve(uri));
  }

  async function saveBase64ToFile(base64Uri: string): Promise<string> {
    const base64Data = base64Uri.split(',')[1]; // remove data:image/...;base64, prefix

    if (Platform.OS === 'web') {
      //web: manually create a Blob
      const mimeType = base64Uri.split(';')[0].split(':')[1] || 'image/jpeg';
      const byteCharacters = atob(base64Data);
      const byteNumbers = new Array(byteCharacters.length).fill(0).map((_, i) => byteCharacters.charCodeAt(i));
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: mimeType });

      //create a "fake file path" using ObjectURL
      const objectUrl = URL.createObjectURL(blob);
      return objectUrl;
    } else {
      //android or ios: use real filesystem i guess
      const filePath = `${FileSystem.cacheDirectory}photo_${Date.now()}.jpg`;
      await FileSystem.writeAsStringAsync(filePath, base64Data, { encoding: FileSystem.EncodingType.Base64 });
      return filePath;
    }
  }

  async function createPdfFromImageWeb(base64Image: string): Promise<Uint8Array> {
    const pdfDoc = await PDFDocumentWeb.create();
    const page = pdfDoc.addPage([612, 792]); // 8.5x11 in points
  
    const mimeType = base64Image.split(';')[0].split(':')[1];
    const imageBytes = await fetch(base64Image).then(res => res.arrayBuffer());
  
    let embeddedImage;
    if (mimeType === 'image/jpeg') {
      embeddedImage = await pdfDoc.embedJpg(imageBytes);
    } else if (mimeType === 'image/png') {
      embeddedImage = await pdfDoc.embedPng(imageBytes);
    } else {
      throw new Error('Unsupported image type for PDF generation');
    }
  
    const { width, height } = embeddedImage.scale(1);
    page.drawImage(embeddedImage, {
      x: 0,
      y: 0,
      width: 612,
      height: 792,
    });
  
    const pdfBytes = await pdfDoc.save();
    return pdfBytes;
  }

  async function imageToPdf(imageUri: string): Promise<string> {
    if (Platform.OS === 'web') {
      if (!imageUri.startsWith('data:image/')) {
        throw new Error('Web only supports base64 image data for now');
      }
  
      const pdfBytes = await createPdfFromImageWeb(imageUri);
  
      //turn PDF bytes into a Blob URL
      const blob = new Blob([pdfBytes], { type: 'application/pdf' });
      const pdfUrl = URL.createObjectURL(blob);
      return pdfUrl;
    } else {
      //native (iOS, Android)
      let finalUri = imageUri;
      if (imageUri.startsWith('data:image/')) {
        finalUri = await saveBase64ToFile(imageUri);
      }
  
      const pdfPath = `${FileSystem.cacheDirectory}receipt_${Date.now()}.pdf`;
      const page = PDFPage.create()
        .setMediaBox(612, 792)
        .drawImage(finalUri, {
          x: 0, y: 0,
          width: 612,
          height: 792,
        });
      await PDFDocument.create(pdfPath).addPages([page]).write();
      return pdfPath;
    }
  }

  function hashStringToId(str: string): number {
    let hash = 5381;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) + hash) + str.charCodeAt(i);
    }
    return hash >>> 0;
  }

  async function handleUploadFlow(getUri: () => Promise<string | undefined>) {
    try {
      setLoading(true);
      const picked = await getUri();
      if (!picked) return;

      // if PDF already, skip conversion
      // const isPdf = picked.toLowerCase().endsWith('.pdf');
      const isPdf = picked.toLowerCase().includes("application/pdf") || picked.toLowerCase().endsWith('.pdf')
      // console.log("[handleUploadFlow] picked:" + picked.toLowerCase())
      if(isPdf) {console.log("[handleUploadFlow] pdf found, skipping conversion");}
      const pdfUri = isPdf ? picked : await imageToPdf(picked);

      const blob = await fetch(pdfUri).then(r => r.blob());

      const form = new FormData();
      form.append('file', blob, 'receipt.pdf');

      const resp = await fetch(`${APIURL}/unstract/upload`, {
        method: 'POST',
        headers,
        body: form,
      });

      if (!resp.ok) {
        if(Platform.OS != "web") {Alert.alert('Upload failed', await resp.text());}
        else {window.alert('Upload failed')}
        console.log("[handleUploadFlow] failed upload");
        return;
      }

      const result = await resp.json();
      setPurchase({
        purchaseID: null,
        date: result.date,
        store: result.storeName,
        totalCost: parseFloat(result.totalCost),
        items: result.items.map((i: UnstractItem) => ({
          itemID: /^\d+$/.test(i.productCode) ? parseInt(i.productCode, 10) : hashStringToId(i.productCode),
          name: i.itemName,
          brand: i.brand,
          unit: i.units,
          amount: parseInt(i.quantity, 10),
          price: parseFloat(i.cost),
          categories: i.categories,
        })),
      });
    } catch (err) {
      console.error(err);
      if(Platform.OS != "web") {Alert.alert('Error', 'Could not process receipt')}
      else {window.alert('Error: Could not process receipt')};
    } finally {
      setLoading(false);
    }
  }

  const columns: ColumnConfig<ExtendedPurchaseItemDTO>[] = [
    { key: 'itemID', label: 'ID', editable: isNew },
    { key: 'name', label: 'Name', editable: isNew },
    { key: 'brand', label: 'Brand', editable: isNew },
    { key: 'unit', label: 'Unit', editable: isNew },
    { key: 'amount', label: 'Quantity', editable: isNew, inputType: 'decimal' },
    { key: 'categories', label: 'Categories', editable: isNew },
    { key: 'price', label: 'Price', editable: isNew, inputType: 'decimal' },
    { key: 'reminderDateTime', label: 'Reminder Date', editable: isNew },
    { key: 'reminderDescription', label: 'Reminder Notes', editable: isNew },
  ];

  if (loading) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator />
      </View>
    );
  }

  function createCleanPurchase(purchase: PurchaseDTO<any>): PurchaseDTO<PurchaseItemDTO> {
    const itemMap = new Map<number, PurchaseItemDTO>();
  
    for (const item of purchase.items) {
      const existing = itemMap.get(item.itemID)!
      console.log("[creatCleanPurchase] itemid:" + JSON.stringify(item.itemID))
      

      
      if (itemMap.has(item.itemID) && !((item.itemID == null || item.itemID == 0 || item.itemID == undefined ))) {
        // if (item.itemID == null || item.itemID == 0 || item.itemID == undefined ) continue; // skip if no ID
        
        //check if unit matches
        if (existing.unit !== item.unit) {
          const message = `Unit mismatch for itemID ${item.itemID} (${existing.unit} vs ${item.unit}). Cannot combine.`;
          
          if (Platform.OS === 'web') {
            window.alert(message);
          } else {
            Alert.alert('Error', message);
          }
          throw new Error(message); // Also throw to break further execution
        }
  
        // If unit matches, combine the amounts
        existing.amount += item.amount;
      } else {
        // Add a clean copy
        itemMap.set(item.itemID, {
          itemID: ((item.itemID && item.itemID > 0) ? item.itemID : null),
          name: item.name,
          brand: item.brand,
          categories: item.categories,
          unit: item.unit,
          amount: item.amount,
          price: item.price,
        });
      }
    }
  
    return {
      purchaseID: purchase.purchaseID,
      date: purchase.date,
      store: purchase.store,
      totalCost: purchase.totalCost,
      items: Array.from(itemMap.values()),
    };
  }

  return (
    <ScrollView style={[styles.container, { backgroundColor: colors.background }]} contentContainerStyle={styles.content}>
      <Text variant="headlineLarge">Purchase Details</Text>

      {isNew && (
        <View style={styles.buttons}>
          <Button mode="outlined" onPress={() => handleUploadFlow(pickDocument)} style={styles.button}>
            Upload Receipt
          </Button>
          <Button mode="outlined" onPress={() => handleUploadFlow(snapPhoto)} style={styles.button}>
            Use Camera
          </Button>
        </View>
      )}

      <TextInput
        label="Date"
        value={purchase.date}
        mode="outlined"
        editable={false}
        right={<TextInput.Icon icon="calendar" onPress={() => setDatePickerOpen(true)} />}
      />

      {/* PURCHASE DATE PICKER */}
      <DatePickerModal
        locale="en"
        mode="single"
        visible={datePickerOpen}
        date={
          isValid(parse(purchase.date, 'yyyy-MM-dd', new Date()))
            ? parse(purchase.date, 'yyyy-MM-dd', new Date())
            : new Date()
        }
        onDismiss={() => setDatePickerOpen(false)}
        onConfirm={({ date }) => {
          if (date) {
            setPurchase(p => ({ ...p, date: format(date, 'yyyy-MM-dd') }));
            setDatePickerOpen(false);
          }
        }}
      />
      {/* REMINDER DATE & TIME PICKERS */}
      <DatePickerModal
        locale="en"
        mode="single"
        visible={reminderModalOpen}
        date={new Date()}
        onDismiss={() => setReminderModalOpen(false)}
        onConfirm={({ date }) => {
          if (date) {
            setTempReminderDate(date);
            setReminderModalOpen(false);
            setTimeModalOpen(true);
          }
        }}
      />

      <TimePickerModal
        visible={timeModalOpen}
        onDismiss={() => setTimeModalOpen(false)}
        onConfirm={({ hours, minutes }) => {
          if (tempReminderDate && reminderTargetIndex !== null) {
            const dt = new Date(tempReminderDate);
            dt.setHours(hours);
            dt.setMinutes(minutes);
            handleConfirmReminder(reminderTargetIndex, dt);
          }
        }}
        hours={12}
        minutes={0}
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
        // value={String(purchase.totalCost)}
        value={totalCostText}
        keyboardType="decimal-pad"
        // onChangeText={c => setPurchase(p => ({ ...p, totalCost: parseFloat(c) || 0 }))}
        onChangeText= {(t) => {
          // Just update local text immediately, even if partial
            if (/^\d*\.?\d*$/.test(t)) {
              setTotalCostText(t);
            }
          }
        }
        onBlur={() => setPurchase((p) => {
          const n = parseFloat(totalCostText);
          if (isNaN(n)){return p} 
          else{
            setTotalCostText(n.toFixed(2))
            return ( { ...p, totalCost: n || 0 })
          }
        })}
        mode="outlined"
        editable={isNew}
        style={styles.input}
      />

      <View style={styles.tableSection}>
        <Text variant="titleMedium">Items</Text>
        <GenericTable
          data={purchase.items}
          columns={columns}
          onEdit={handleEditRow}
          onDelete={handleDeleteRow}
          onAddReminder={handleRequestAddReminder}
        />
        {isNew && (
          <Button mode="outlined" onPress={handleAddRow} style={{ marginTop: 12 }}>
            Add Item Row
          </Button>
        )}
      </View>

      {isNew && (
        <Button
          mode="contained"
          style={{ marginTop: 24 }}
          onPress={async () => {
            const cleanPurchase = createCleanPurchase(purchase);
          
            const purchaseResp = await fetch(`${APIURL}/purchases`, {
              method: 'POST',
              headers: { ...headers, 'Content-Type': 'application/json' },
              body: JSON.stringify(cleanPurchase),
            });
          
            if (purchaseResp.ok) {
              const createdPurchase = await purchaseResp.json();
              const { purchaseID, items } = createdPurchase;
          
              //lookup table
              const itemLookup = new Map<string, number>();
              for (const item of items) {
                const key = `${item.name}::${item.brand}::${item.unit}`;
                itemLookup.set(key, item.itemID);
              }
          
              //fix reminder
              for (const reminder of reminders) {
                const lookupKey = `${reminder.itemName}::${reminder.itemBrand}::${reminder.itemUnit}`;
                const newItemID = itemLookup.get(lookupKey);
          
                if (!newItemID) {
                  console.error('No matching itemID found for reminder:', reminder);
                  continue; // skip or alert?
                }
          
                const fixedReminder = {
                  ...reminder,
                  itemID: newItemID,
                  purchaseID: purchaseID,
                };
          
                await fetch(`${APIURL}/reminders`, {
                  method: 'POST',
                  headers: { ...headers, 'Content-Type': 'application/json' },
                  body: JSON.stringify(fixedReminder),
                });
              }
          
              router.replace(`/purchases/${purchaseID}`);
            }
          }}          
        >
          Submit Purchase
        </Button>
      )}

    {!isNew && (
      <Button
        mode="contained-tonal"
        style={{ marginTop: 24, backgroundColor: colors.error }}
        onPress={() => setDeleteDialogOpen(true)}
      >
        Delete Purchase
      </Button>
    )}

    <Portal>
      <Dialog visible={deleteDialogOpen} onDismiss={() => setDeleteDialogOpen(false)}>
        <Dialog.Title>Delete Purchase</Dialog.Title>
        <Dialog.Content>
          <Text>Are you sure you want to delete this purchase?</Text>
          <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 16 }}>
            <Checkbox
              status={deleteItemsToo ? 'checked' : 'unchecked'}
              onPress={() => setDeleteItemsToo(!deleteItemsToo)}
            />
            <Text>Also delete associated items from inventory</Text>
          </View>
        </Dialog.Content>
        <Dialog.Actions>
          <Button onPress={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onPress={handleDeleteConfirmed}>Confirm</Button>
        </Dialog.Actions>
      </Dialog>
    </Portal>




      <CameraModal
        visible={cameraOpen}
        onClose={() => setCameraOpen(false)}
        onCapture={handlePhotoCaptured}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 16 },
  loading: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  buttons: { flexDirection: 'row', justifyContent: 'space-between', gap: 8 },
  button: { flex: 1 },
  input: { backgroundColor: 'transparent' },
  tableSection: { marginTop: 24 },
});
