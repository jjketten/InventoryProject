import React, { useEffect, useState, useCallback } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  StyleSheet
} from 'react-native';
import Autocomplete from 'react-native-autocomplete-input';
import { useTheme, IconButton } from 'react-native-paper';
import { Item } from '../../types/item';
import { useFocusEffect } from '@react-navigation/native';
import { CategoryUnitTotalDTO } from '@/types/CategoryUnitTotalDTO';
import { APIURL } from '../config';

interface Category {
  categoryID: number;
  name: string;
  items: Item[];
}

interface CategoryTotal {
  categoryID: number;
  totals: CategoryUnitTotalDTO[];
}

const CategoryScreen: React.FC = () => {
  const { colors } = useTheme();
  const [categories, setCategories] = useState<Category[]>([]);
  const [items, setItems] = useState<Item[]>([]);
  const [queries, setQueries] = useState<Record<number, string>>({});
  const [filteredItemsByCategory, setFilteredItemsByCategory] = useState<Record<number, Item[]>>({});
  const [totalsPerCategory, setTotalsPerCategory] = useState<CategoryTotal[]>([]);

  const headers = {
    'X-Tenant-ID': 'test_schema3',
    'Content-Type': 'application/json'
  };

  useFocusEffect(
    useCallback(() => {
      fetchCategories();
      fetchItems();
      fetchTotals();
    }, [])
  );

  useEffect(() => {
    fetchCategories();
    fetchItems();
    fetchTotals();
  }, []);

  const fetchCategories = async () => {
    const res = await fetch(APIURL + '/categories', { headers });
    const data = await res.json();
    setCategories(data);
  };

  const fetchItems = async () => {
    const res = await fetch(APIURL + '/items', { headers });
    const data = await res.json();
    setItems(data);
  };

  const fetchTotals = async () => {
    const res = await fetch(APIURL + '/categories/totals', { headers });
    const totals: CategoryTotal[] = await res.json();
    setTotalsPerCategory(totals);
  };

  const handleQueryChange = (text: string, categoryID: number) => {
    setQueries(prev => ({ ...prev, [categoryID]: text }));

    const filtered = items.filter(item =>
      item.name.toLowerCase().includes(text.toLowerCase())
    );

    setFilteredItemsByCategory(prev => ({
      ...prev,
      [categoryID]: filtered
    }));
  };

  const handleAddItem = async (itemId: number, categoryId: number) => {
    const url = `${APIURL}/categories/${categoryId}/items/${itemId}`;
    await fetch(url, { method: 'POST', headers });
    fetchCategories(); // Refresh list
    fetchItems();

    // Clear query and filteredItems for this category
    setQueries(prev => ({ ...prev, [categoryId]: '' }));
    setFilteredItemsByCategory(prev => ({ ...prev, [categoryId]: [] }));
  };

  const handleRenameCategory = async (categoryId: number, newName: string) => {
    await fetch(`${APIURL}/categories/${categoryId}/rename`, {
      method: 'PUT',
      headers,
      body: (newName).trim(),
    });
    fetchCategories();
    fetchItems();
    fetchTotals();
  };

  const handleDeleteCategory = async (categoryId: number) => {
    await fetch(`${APIURL}/categories/${categoryId}`, {
      method: 'DELETE',
      headers,
    });
    fetchCategories();
  };

  return (
    <FlatList
      style={{ backgroundColor: colors.background }}
      data={categories}
      keyExtractor={(cat) => cat.categoryID.toString()}
      renderItem={({ item: category }) => (
        <View style={[styles.categoryBox, { backgroundColor: colors.onBackground }]}>
          <View style={styles.categoryHeader}>
            <TextInput
              style={styles.renameInput}
              value={category.name}
              onChangeText={(text) => handleRenameCategory(category.categoryID, text)}
            />
            {totalsPerCategory
              .filter(item => item.categoryID === category.categoryID)
              .flatMap(item =>
                item.totals.map((t, i) => (
                  <Text key={`${item.categoryID}-${t.unit}-${i}`}>
                    {t.unit}: {t.totalAmount.toFixed(2)}
                  </Text>
                ))
              )}
            <IconButton icon="delete" onPress={() => handleDeleteCategory(category.categoryID)} />
          </View>

          {items
            .filter((item) => item.categories?.includes(category.name))
            .map((item) => (
              <Text style={styles.itemText} key={item.itemID}>
                {item.name}
              </Text>
            ))}

          <Autocomplete
            data={
              (queries[category.categoryID]?.trim() ?? '') === ''
                ? []
                : (filteredItemsByCategory[category.categoryID] || []).filter(item => item.itemID !== undefined)
            }
            defaultValue={queries[category.categoryID] || ''}
            onChangeText={(text) => handleQueryChange(text, category.categoryID)}
            placeholder="Add item by name"
            flatListProps={{
              keyExtractor: (item) => (item?.itemID ?? '').toString(),
              renderItem: ({ item }) => (
                <Text
                  style={styles.suggestion}
                  onPress={() => {
                    if (item?.itemID != null && category.categoryID != null) {
                      handleAddItem(item.itemID, category.categoryID);
                    }
                  }}
                >
                  {item?.name ?? 'Unnamed'}
                </Text>
              ),
            }}
            inputContainerStyle={styles.autoInput}
          />
        </View>
      )}
    />
  );
};

const styles = StyleSheet.create({
  categoryBox: {
    margin: 10,
    padding: 10,
    borderRadius: 8,
    elevation: 2,
  },
  categoryHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  renameInput: {
    flex: 1,
    borderBottomWidth: 1,
    marginRight: 6,
    padding: 4,
  },
  itemText: {
    marginLeft: 10,
    marginBottom: 2,
  },
  suggestion: {
    padding: 6,
    backgroundColor: '#ccc',
    borderBottomWidth: 1,
  },
  autoInput: {
    borderWidth: 0,
  },
});

export default CategoryScreen;
