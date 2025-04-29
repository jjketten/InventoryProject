import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, Button } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { RecipeDTO } from '@/types/RecipeDTO';
import { useRouter } from 'expo-router';
import { ScrollView } from 'react-native-gesture-handler';
import { useTheme } from 'react-native-paper';
import { APIURL, TENANTID } from '../config';


const RecipeScreen = () => {
  const [recipes, setRecipes] = useState<RecipeDTO[]>([]);
  const navigation = useNavigation<any>();
  const router = useRouter();
  const { colors } = useTheme();


  const headers = {
    'X-Tenant-ID': TENANTID,
    'Content-Type': 'application/json',
  };

  const fetchRecipes = async () => {
    const res = await fetch(APIURL+'/recipes', {
      method: 'GET',
      headers,
    });
    const data = await res.json();
    setRecipes(data);
  };

  useEffect(() => {
    const unsubscribe = navigation.addListener('focus', fetchRecipes);
    return unsubscribe;
  }, [navigation]);

  return (
    <ScrollView style={{ flex: 1, backgroundColor: colors.background }}>
      <View style={{ flex: 1, padding: 16}}>
        <FlatList
          style={{backgroundColor: colors.secondary}}
          data={recipes}
          keyExtractor={(item, index) => item.recipeID?.toString() ?? `recipe-${index}`}
          renderItem={({ item }) => (
            <TouchableOpacity
              onPress={() => router.push(`/recipes/${item.recipeID}`)}
            >
              <View style={{ padding: 12, borderBottomWidth: 1 }}>
                <Text style={{ fontSize: 16, fontWeight: 'bold' }}>{item.name}</Text>
                {item.reference && <Text>{item.reference}</Text>}
              </View>
            </TouchableOpacity>
          )}
        />
        <Button
          title="Add New Recipe"
          onPress={() => router.push('/recipes/new')}
        />
      </View>
    </ScrollView>
  );
};

export default RecipeScreen;
