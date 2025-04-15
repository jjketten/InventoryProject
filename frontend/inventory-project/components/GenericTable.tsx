// components/GenericTable.tsx
import React from 'react';
import { View, Text, TextInput, StyleSheet } from 'react-native';
import { Item } from '../types/item';

interface Props {
  data: Item[];
  onEdit: (index: number, updatedItem: Item) => void;
}

const GenericTable: React.FC<Props> = ({ data, onEdit }) => {
  return (
    <View>
      <View style={{ flexDirection: 'row', marginBottom: 6, backgroundColor: '#fff' }}>
        {['Name', 'Brand', 'Unit', 'Amount'].map((label) => (
            <Text key={label} style={{ flex: 1, fontWeight: 'bold', color: '#000', textAlign: 'center' }}>
              {label}
            </Text>
        ))}
    </View>

      {data.map((item, index) => (
        <View key={index} style={styles.row}>
          <TextInput
            style={styles.input}
            value={item.name}
            placeholder="Name"
            onChangeText={(text) =>
              onEdit(index, { ...item, name: text })
            }
          />
          <TextInput
            style={styles.input}
            value={item.brand}
            placeholder="Brand"
            onChangeText={(text) =>
              onEdit(index, { ...item, brand: text })
            }
          />
          <TextInput
            style={styles.input}
            value={item.unit}
            placeholder="Unit"
            onChangeText={(text) =>
              onEdit(index, { ...item, unit: text })
            }
          />
          <TextInput
            style={styles.input}
            value={item.amount.toString()}
            placeholder="Amount"
            keyboardType="numeric"
            onChangeText={(text) =>
              onEdit(index, { ...item, amount: parseFloat(text) || 0 })
            }
          />
        </View>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
    row: {
      flexDirection: 'row',
      marginBottom: 10,
      alignItems: 'center',
      backgroundColor: '#fff',         // light background for visibility
      padding: 6,
      borderRadius: 6,
      elevation: 1,                    // adds subtle shadow on Android
      shadowColor: '#000',
      shadowOpacity: 0.1,
      shadowOffset: { width: 0, height: 1 },
      shadowRadius: 2,
    },
    input: {
      flex: 1,
      borderWidth: 1,
      borderColor: '#ccc',
      padding: 6,
      marginHorizontal: 4,
      borderRadius: 4,
      backgroundColor: '#f9f9f9',      // visible input background
      color: '#000',                   // black text for light bg
    },
  });

export default GenericTable;
