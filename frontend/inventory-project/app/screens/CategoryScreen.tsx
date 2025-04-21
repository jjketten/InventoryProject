import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, FlatList, Button, StyleSheet } from 'react-native';
import Autocomplete from 'react-native-autocomplete-input';
import { useTheme, IconButton } from 'react-native-paper';
import { Item } from '../../types/item';
import { useFocusEffect } from '@react-navigation/native';
import { useCallback } from 'react';
import { CategoryUnitTotalDTO } from '@/types/CategoryUnitTotalDTO';

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
  const [query, setQuery] = useState('');
  const [filteredItems, setFilteredItems] = useState<Item[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
  const [totalsPerCategory, setTotalsPerCategory] = useState<CategoryTotal[]>([]);
  

  const headers = {
    'X-Tenant-ID': 'test_schema2',
    'Content-Type': 'application/json'
  };

  useFocusEffect(
    useCallback(() => {
        // refetch on focus
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

  const safeFilteredItems = filteredItems.filter(item => item.itemID !== undefined);

  const fetchCategories = async () => {
    const res = await fetch('http://127.0.0.1:9000/api/categories', { headers });
    const data = await res.json();
    setCategories(data);
  };

  const fetchItems = async () => {
    const res = await fetch('http://127.0.0.1:9000/api/items', { headers });
    const data = await res.json();
    setItems(data);
  };

  // const fetchTotals = async (categoryIds : number[]) => {
  //   // categoryIds.forEach((id) => {
  //   //   const res = await fetch(`http://127.0.0.1:9000/api/categories/${categoryId}/totals`)
  //   // }
  //   // )
  //   var arr = []
  //   for await (const res of categoryIds.map( id => {
  //     categoryID:id;
  //     totals: fetch(`http://127.0.0.1:9000/api/categories/${id}/totals`)
  //   })) {
  //     //???
  //     arr.push(res)
  //   }
  // }

  const fetchTotals = async () => {
    const res = await fetch('http://127.0.0.1:9000/api/categories/totals', { headers });
    const totals: CategoryTotal[] = await res.json();
    setTotalsPerCategory(totals);
  }

  const handleQueryChange = (text: string) => {
    setQuery(text);
    setFilteredItems(items.filter(item => item.name.toLowerCase().includes(text.toLowerCase())));
  };

  const handleAddItem = async (itemId: number, categoryId: number) => {
    const url = `http://127.0.0.1:9000/api/categories/${categoryId}/items/${itemId}`;
    await fetch(url, { method: 'POST', headers });
    fetchCategories(); // refresh
    setQuery('');
    setFilteredItems([]);
    // fetchCategories();
    fetchItems();
  };

  const handleRenameCategory = async (categoryId: number, newName: string) => {
    await fetch(`http://127.0.0.1:9000/api/categories/${categoryId}/rename`, {
      method: 'PUT',
      headers,
      body: JSON.stringify(newName),
    });
    fetchCategories();
  };

  const handleDeleteCategory = async (categoryId: number) => {
    await fetch(`http://127.0.0.1:9000/api/categories/${categoryId}`, {
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
        <View style={[styles.categoryBox, { backgroundColor: colors.onSecondary }]}>
          <View style={styles.categoryHeader}>
            <TextInput
              style={styles.renameInput}
              value={category.name}
              onChangeText={(text) => handleRenameCategory(category.categoryID, text)}
            />
            {totalsPerCategory
              .filter((item) => item.categoryID === category.categoryID)
              .flatMap((item) =>
                item.totals.map((t, i) => (
                  <Text key={`${item.categoryID}-${t.unit}-${i}`}>
                    {t.unit}: {t.totalAmount}
                  </Text>
                )))
            }
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
              data={query.trim() === '' ? [] : safeFilteredItems}
              defaultValue={query}
              onChangeText={handleQueryChange}
              placeholder="Add item by name"
              flatListProps={{
              keyExtractor: (item) => (item?.itemID ?? '').toString(),
              renderItem: ({ item }) => (
              <Text
                  style={styles.suggestion}
                  onPress={() => {
                  if (item?.itemID != null && category.categoryID != null) {
                      handleAddItem(item.itemID, category.categoryID);
                      setQuery('');
                      setFilteredItems([]);
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
