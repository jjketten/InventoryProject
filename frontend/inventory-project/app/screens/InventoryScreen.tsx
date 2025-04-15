// screens/InventoryScreen.tsx
import React, { useEffect, useState } from 'react';
import { View, Button, Text, ScrollView } from 'react-native';
import GenericTable from '../../components/GenericTable';
import { Item } from '../../types/item';

const InventoryScreen: React.FC = () => {
  const [localItems, setLocalItems] = useState<Item[]>([]);
  const [serverItems, setServerItems] = useState<Item[]>([]);

  useEffect(() => {
    fetchServerItems();
  }, []);

  const fetchServerItems = async () => {
    try {
      const response = await fetch('http://127.0.0.1:9000/api/items', {
        method: 'GET',
        headers: {
            'X-Tenant-ID' : 'test_schema1',
            'Content-Type' : 'application/json'
        }
      }); //should not hardcode this
      const data: Item[] = await response.json();
      setServerItems(data);
    } catch (error) {
      console.error('Failed to fetch server items:', error);
    }
  };

  const handleEdit = (index: number, updated: Item) => {
    const updatedItems = [...localItems];
    updatedItems[index] = updated;
    setLocalItems(updatedItems);
  };

  const handleAddRow = () => {
    setLocalItems([...localItems, { name: '', brand: '', unit: '', amount: 0 }]);
  };

  const handleSubmit = async () => {
    const serverHash = new Set(serverItems.map(i => JSON.stringify({ name: i.name, brand: i.brand, unit: i.unit, amount: i.amount })));

    const newItems = localItems.filter(
      (item) => !serverHash.has(JSON.stringify({ name: item.name, brand: item.brand, unit: item.unit, amount: item.amount }))
    );

    for (const item of newItems) {
      try {
        const response = await fetch('http://127.0.0.1:9000/api/items', { //again, should not hardcode this
          method: 'POST',
          headers: { 
            'X-Tenant-ID' : 'test_schema1',
            'Content-Type' : 'application/json'
           },
          body: JSON.stringify(item),
        });

        if (response.ok) {
          const saved = await response.json();
          console.log('Item added:', saved);
        } else {
          console.warn('Failed to add item:', item);
        }
      } catch (error) {
        console.error('Error posting item:', error);
      }
    }

    fetchServerItems(); // refresh from server
  };

  return (
    <ScrollView contentContainerStyle={{ padding: 20 }}>
      <Text style={{ fontSize: 20, marginBottom: 10 }}>Inventory</Text>
      <GenericTable data={localItems} onEdit={handleEdit} />
      <Button title="Add Item Row" onPress={handleAddRow} />
      <View style={{ marginTop: 10 }}>
        <Button title="Submit New Items" onPress={handleSubmit} />
      </View>
    </ScrollView>
  );
};

export default InventoryScreen;
