import React, { useEffect, useState } from 'react';
import { View, ScrollView, StyleSheet, Alert } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { Text, TextInput, Button, ActivityIndicator } from 'react-native-paper';
import GenericTable from '@/components/GenericTable';
import { ColumnConfig } from '@/components/ColumnConfig';
import { RecipeDTO } from '@/types/RecipeDTO';
import { RecipeItemDTO } from '@/types/RecipeItemDTO';
import { RecipeStepDTO } from '@/types/RecipeStepDTO';
import Autocomplete from 'react-native-autocomplete-input';
import { useTheme } from 'react-native-paper';

// type TrackedItem = Item & {
//   itemID?: number;
//   isNew?: boolean;
//   isModified?: boolean;
// };

type TrackedRecipeItem = RecipeItemDTO & { 
  itemName: string; 
  isNew:boolean 
};

export default function RecipeDetailScreen() {
  const { id } = useLocalSearchParams();
  const isNew = id === 'new';  
  const router = useRouter();
  const { colors } = useTheme();

  const [recipe, setRecipe] = useState<RecipeDTO>({
    recipeID: null,
    name: '',
    reference: '',
    items: [],
    steps: [],
  });

  const headers = {
    'X-Tenant-ID': 'test_schema2',
    'Content-Type': 'application/json',
  };

  const [loading, setLoading] = useState(true);
  const [itemsRef, setItemsRef] = useState<{ itemID: number; name: string }[]>([]);
  const [categoriesRef, setCategoriesRef] = useState<{ categoryID: number, name: string}[]>([]);

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const itemRes = await fetch('http://localhost:9000/api/items', { method: 'GET', headers });
        const itemData = await itemRes.json();
        const itemsList = itemData.map((i: any) => ({ itemID: i.itemID, name: i.name }));
        setItemsRef(itemsList);
  
        const categoryRes = await fetch('http://localhost:9000/api/categories', { method: 'GET', headers });
        const categoryData = await categoryRes.json();
        const categoriesList = categoryData.map((c: any) => ({ categoryID: c.categoryID, name: c.name }));
        setCategoriesRef(categoriesList);
  
        if (!isNew) {
          const recipeRes = await fetch(`http://localhost:9000/api/recipes/${id}`, { method: 'GET', headers });
          const recipeData = await recipeRes.json();
          console.log('original recipe data:', JSON.stringify(recipeData))
  
          const enrichedRecipe: RecipeDTO = {
            ...recipeData,
            items: recipeData.items.map((item: RecipeItemDTO) => ({
              ...item,
              itemName: itemsList.find((i: any) => i.itemID === item.itemID)?.name || '',
              isNew: false,
            })),
          };
  
          setRecipe(enrichedRecipe);
          console.log('recipe data:', JSON.stringify(enrichedRecipe));
        }
      } catch (err) {
        console.error('Error fetching data:', err);
      } finally {
        setLoading(false);
      }
    };
  
    fetchAll();
  }, [id]);
  
  

  const updateItem = (index: number, updated: RecipeItemDTO) => {
    setRecipe((prev) => {
      const newItems = [...prev.items];
      newItems[index] = updated;
      return { ...prev, items: newItems };
    });
  };

  const updateStep = (index: number, updated: RecipeStepDTO) => {
    setRecipe((prev) => {
      const newSteps = [...prev.steps];
      newSteps[index] = updated;
      return { ...prev, steps: newSteps };
    });
  };

  const handleDelete = async () => {
    Alert.alert('Delete Recipe', 'Are you sure?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Delete',
        style: 'destructive',
        onPress: async () => {
          await fetch(`http://localhost:9000/api/recipes/${recipe.recipeID}`, {
            method: 'DELETE',
            headers
          });
          router.back();
        },
      },
    ]);
  };

  const handleSave = async () => {
    const itemNameToID = (name: string) => itemsRef.find((i) => i.name === name)?.itemID ?? null;
    const itemIDToName = (id: number) => itemsRef.find((i) => i.itemID === id)?.name ?? '';
    console.log("categories:" + JSON.stringify(categoriesRef))

    const cleanedItems = recipe.items.map((item) => {
      const itemID = itemNameToID(item.itemName);
      const matchedCategory = categoriesRef.find((c) => c.name === item.categoryName);
      const categoryID = matchedCategory ? matchedCategory.categoryID : -1;
    
      // let categoryID: number | null = null;
      // if (categoriesRef.includes(item.categoryName)) {
      //   categoryID = item.categoryID ?? -1;
      // }
    
      return {
        ...item,
        itemID,
        categoryID,
      };
    });
    

    const body = {
      ...recipe,
      items: cleanedItems,
    };

    if (!isNew) {
      await fetch(`http://localhost:9000/api/recipes/${recipe.recipeID}`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify(body),
      });
    } else {
      const res = await fetch('http://localhost:9000/api/recipes', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(body),
      });
      // const newID = await res.json();
      const newID = parseInt(await res.text(), 10);
      // router.replace({
      //   pathname: '/recipes/[id]',
      //   params: { id: String(newID) },
      // } as any);      
      if (!isNaN(newID)) {
        router.replace(`/recipes/${newID}`);
      } else {
        console.error('Failed to get new recipe ID from server:', newID);
      }      
    }
  };

  const itemColumns: ColumnConfig<TrackedRecipeItem>[]= [
    {
      key: 'itemName',
      label: 'Item',
      editable: true,
      inputType: 'text',
      width: 40,
      render: (val, row, index) =>
        row.isNew ? (
          <Autocomplete
            style={{ backgroundColor: colors.onBackground }}
            data={itemsRef.filter((i) => i.name.toLowerCase().includes(val?.toLowerCase() || ''))}
            defaultValue={val}
            onChangeText={(text) => updateItem(index as number, { ...row, itemName: text })}
            flatListProps={{
              keyExtractor: (item) => item.itemID.toString(),
              renderItem: ({ item }) => (
                <Text
                  style={{ color: colors.background }}
                  onPress={() => updateItem(index as number, { ...row, itemName: item.name })}
                >
                  {item.name}
                </Text>
              ),
            }}
            inputContainerStyle={{ borderWidth: 0 }}
          />
        ) : (
          <Text style={{ color: colors.onSurface }}>{val}</Text>
        ),
    },
    { key: 'unit', label: 'Unit', editable: true, inputType: 'text' },
    { key: 'amount', label: 'Amount', editable: true, inputType: 'number' },
    {
      key: 'categoryName',
      label: 'Category (for substitutes)',
      editable: true,
      inputType: 'text',
      render: (val, row, index) => (
        row.isNew ? (
          <Autocomplete style={{ backgroundColor: colors.onBackground }}
            data={categoriesRef.filter((c) => c.name.toLowerCase().includes(val?.toLowerCase() || ''))}
            defaultValue={val}
            onChangeText={(text) => updateItem(index as number, { ...row, categoryName: text })}
            flatListProps={{
              keyExtractor: (_, i) => i.toString(),
              renderItem: ({ item }) => (
                <Text style={{ color: colors.background }} onPress={() => updateItem(index as number, { ...row, categoryName: item.name })}>
                  {item.name}
                </Text>
              ),
            }}
            inputContainerStyle={{ borderWidth: 0 }}
          />
        ) : (
          <Text style={{ color: colors.onSurface }}>{val}</Text>
        )
      ),
    },
  ];

  const stepColumns: ColumnConfig<RecipeStepDTO>[] = [
    { key: 'stepNumber', label: '#', editable: true, inputType: 'number', width: 10 },
    { key: 'content', label: 'Content', editable: true, inputType: 'text', width: 40 },
  ];

  if (loading) return <ActivityIndicator />;

  return (
    <ScrollView contentContainerStyle={styles.container} style={{ flex: 1, backgroundColor: colors.background }}>
      <Text variant="headlineLarge">Recipe Details</Text>
      <TextInput
        label="Name"
        value={recipe.name}
        onChangeText={(text) => setRecipe((r) => ({ ...r, name: text }))}
        mode="outlined"
      />
      <TextInput
        label="Reference"
        value={recipe.reference ?? ''}
        onChangeText={(text) => setRecipe((r) => ({ ...r, reference: text }))}
        mode="outlined"
      />

      <Text variant="titleMedium" style={{ marginTop: 20 }}>
        Items
      </Text>
      <GenericTable<any>
        data={recipe.items}
        columns={itemColumns}
        onEdit={updateItem}
        onDelete={(i) => {
          setRecipe((r) => ({
            ...r,
            items: r.items.filter((_, idx) => idx !== i),
          }));
        }}
      />
      <Button
        mode="outlined"
        onPress={() =>
          setRecipe((r) => ({
            ...r,
            items: [...r.items, { itemName: '', unit: '', amount: 0, categoryName: '', isNew: true }],
          }))
        }
      >
        Add Item
      </Button>

      <Text variant="titleMedium" style={{ marginTop: 20 }}>
        Steps
      </Text>
      <GenericTable
        data={recipe.steps}
        columns={stepColumns}
        onEdit={updateStep}
        onDelete={(i) => {
          setRecipe((r) => ({
            ...r,
            steps: r.steps.filter((_, idx) => idx !== i),
          }));
        }}
      />
      <Button
        mode="outlined"
        onPress={() =>
          setRecipe((r) => ({
            ...r,
            steps: [...r.steps, { stepNumber: r.steps.length + 1, content: '' }],
          }))
        }
      >
        Add Step
      </Button>

      <View style={{ marginTop: 30, flexDirection: 'row', justifyContent: 'space-between' }}>
        <Button mode="contained" onPress={handleSave}>
          Save
        </Button>
        {!isNew && (
          <Button mode="contained" buttonColor="red" onPress={handleDelete}>
            Delete
          </Button>
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 16,
  },
});
