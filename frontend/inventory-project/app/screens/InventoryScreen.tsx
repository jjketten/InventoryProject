import React, { useEffect, useState } from 'react';
import { View, Button, ScrollView } from 'react-native';
import { useTheme } from 'react-native-paper';
import GenericTable from '../../components/GenericTable';
import { ColumnConfig } from '../../components/ColumnConfig';
import { Item } from '../../types/item';
import { useFocusEffect } from '@react-navigation/native';
import { useCallback } from 'react';
import { APIURL } from '../config';

type TrackedItem = Item & {
  itemID?: number;
  isNew?: boolean;
  isModified?: boolean;
};

const InventoryScreen: React.FC = () => {
  const { colors } = useTheme();
  const [localItems, setLocalItems] = useState<TrackedItem[]>([]);
  const [serverItems, setServerItems] = useState<Item[]>([]);
  const [deletedItemIds, setDeletedItemIds] = useState<number[]>([]);

  const headers = {
    'X-Tenant-ID': 'test_schema3',
    'Content-Type': 'application/json',
  };

  const columns: ColumnConfig<TrackedItem>[] = [
    { key: 'name', label: 'Name', editable: true },
    { key: 'brand', label: 'Brand', editable: true },
    { key: 'unit', label: 'Unit', editable: true },
    { key: 'amount', label: 'Amount', editable: true, inputType: 'decimal' },
    { key: 'categories', label: 'Categories', editable: true },
  ];

  useFocusEffect(
    useCallback(() => {
      //refetch when tab in focus
      fetchServerItems();
    }, [])
  );

  useEffect(() => {
    fetchServerItems();
  }, []);

  const fetchServerItems = async () => {
    try {
      const response = await fetch(APIURL+'/items', {
        method: 'GET',
        headers,
      });
      const data: Item[] = await response.json();
      setServerItems(data);
      const enriched = data.map((i) => ({ ...i, isNew: false, isModified: false }));
      setLocalItems(enriched);
    } catch (error) {
      console.error('Failed to fetch server items:', error);
    }
  };

  const handleEdit = (index: number, updated: TrackedItem) => {
    const updatedItems = [...localItems];
    updatedItems[index] = {
      ...updated,
      isNew: updatedItems[index].isNew,
      isModified: true,
    };
    setLocalItems(updatedItems);
  };

  const handleAddRow = () => {
    const newItem: TrackedItem = {
      name: '',
      brand: '',
      unit: '',
      amount: 0,
      categories: [],
      isNew: true,
      isModified: false,
    };
    setLocalItems([...localItems, newItem]);
  };

  const handleDeleteRow = (index: number) => {
    const item = localItems[index];
    if (item.itemID !== undefined) {
      setDeletedItemIds((prev) => [...prev, item.itemID!]);
    }    
    const updated = [...localItems];
    updated.splice(index, 1);
    setLocalItems(updated);
  };

  const handleSubmit = async () => {
    try {
      // Delete items
      for (const id of deletedItemIds) {
        await fetch(`${APIURL}/items/${id}`, {
          method: 'DELETE',
          headers,
        });
      }

      // Create or update items
      for (const item of localItems) {
        const { isNew, isModified, ...payload } = item;
        const body = JSON.stringify(payload);

        if (isNew) {
          await fetch(APIURL+'/items', {
            method: 'POST',
            headers,
            body,
          });
        } else if (isModified && item.itemID) {
          await fetch(`${APIURL}/items/${item.itemID}`, {
            method: 'PUT',
            headers,
            body,
          });
        }
      }

      // Refresh from backend
      setDeletedItemIds([]);
      fetchServerItems();
    } catch (err) {
      console.error('Error submitting changes:', err);
    }
  };

  return (
    <ScrollView style={{ backgroundColor: colors.background, flex: 1 }} contentContainerStyle={{ padding: 20 }}>
      <GenericTable<TrackedItem>
        data={localItems}
        columns={columns}
        onEdit={handleEdit}
        onDelete={handleDeleteRow}
      />
      <Button title="Add Item Row" onPress={handleAddRow} />
      <View style={{ marginTop: 10 }}>
        <Button title="Submit Changes" onPress={handleSubmit} />
      </View>
    </ScrollView>
  );
};

export default InventoryScreen;
