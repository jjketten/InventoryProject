import React, { useCallback, useEffect, useState } from 'react';
import { View, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';
import {
  DataTable,
  ActivityIndicator,
  FAB,
  Text
} from 'react-native-paper';
import { PurchaseDTO } from '@/types/PurchaseDTO';
import { useTheme } from 'react-native-paper';
import { useFocusEffect } from '@react-navigation/native';
import { APIURL } from '../config';
import { PurchaseItemDTO } from '@/types/PurchaseItemDTO';

export default function PurchasesScreen() {
  const router = useRouter();
  const { colors } = useTheme();

  const [purchases, setPurchases] = useState<PurchaseDTO<PurchaseItemDTO>[]>([]);
  const [loading, setLoading] = useState(true);

  const headers = {
    'X-Tenant-ID': 'test_schema3',
    'Content-Type': 'application/json',
  };

  const fetchPurchases = async () => {
    try {
      const res = await fetch(APIURL+'/purchases', {
        method: 'GET',
        headers,
      });
      const data: PurchaseDTO<PurchaseItemDTO>[] = await res.json();
      setPurchases(data);
    } catch (err) {
      console.error('Failed to load purchases', err);
    } finally {
      setLoading(false);
    }
  };

  useFocusEffect(
      useCallback(() => {
        //refetch when tab in focus
        fetchPurchases();
      }, [])
  );

  useEffect(() => {
    fetchPurchases();
  }, []);

  if (loading) {
    return (
      <View style={styles.loaderContainer}>
        <ActivityIndicator animating size="large" />
        <Text style={{ marginTop: 8 }}>Loading purchases…</Text>
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <DataTable>
        <DataTable.Header>
          <DataTable.Title>Date</DataTable.Title>
          <DataTable.Title>Store</DataTable.Title>
          <DataTable.Title numeric>Total</DataTable.Title>
        </DataTable.Header>

        {purchases.map((p) => (
          <DataTable.Row
            key={p.purchaseID}
            onPress={() => router.push(`/purchases/${p.purchaseID}`)}
          >
            <DataTable.Cell>{p.date}</DataTable.Cell>
            <DataTable.Cell>{p.store}</DataTable.Cell>
            <DataTable.Cell numeric>
              {p.totalCost.toFixed(2)}
            </DataTable.Cell>
          </DataTable.Row>
        ))}
      </DataTable>

      <FAB
        icon="plus"
        style={styles.fab}
        onPress={() => router.push('/purchases/new')}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  loaderContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  fab: {
    position: 'absolute',
    right: 16,
    bottom: 16,
  },
});
