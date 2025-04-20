import React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { TextInput, IconButton } from 'react-native-paper'; // ✅ Make sure IconButton is imported
import { ColumnConfig } from './ColumnConfig';
import ChipEditor from './ChipEditor';

interface EditableRowProps<T> {
  item: T;
  index: number;
  columns: ColumnConfig<T>[];
  onEdit: (index: number, updated: T) => void;
  onDelete?: (index: number) => void; // ✅ Added
  isNew?: boolean;
}

function EditableRow<T extends object>({
  item,
  index,
  columns,
  onEdit,
  onDelete,
  isNew = false,
}: EditableRowProps<T>) {
  return (
    <View
      style={[
        styles.row,
        isNew && { backgroundColor: '#2a2f3a' },
      ]}
    >
      {columns.map((col) => {
        const value = item[col.key];
        return (
          <View key={col.key as string} style={[styles.cell, { flex: col.width ?? 1 }]}>
            {col.key === 'categories' ? (
              <ChipEditor
                categories={(value as string[]) || []}
                onChange={(updated) =>
                  onEdit(index, { ...item, [col.key]: updated })
                }
              />
            ) : col.editable ? (
              <TextInput
                value={String(value ?? '')}
                onChangeText={(text) =>
                  onEdit(index, {
                    ...item,
                    [col.key]: col.inputType === 'number' ? parseFloat(text) || 0 : text,
                  })
                }
                keyboardType={col.inputType === 'number' ? 'numeric' : 'default'}
                mode="outlined"
                dense
                style={styles.input}
              />
            ) : (
              <Text style={{ color: 'white' }}>{String(value)}</Text>
            )}
          </View>
        );
      })}
      {onDelete && (
        <IconButton
          icon="delete"
          size={18}
          onPress={() => onDelete(index)}
          style={styles.deleteButton}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
    row: {
      flexDirection: 'row',
      alignItems: 'flex-start',
      padding: 6,
      borderBottomWidth: 1,
      borderBottomColor: '#444',
    },
    cell: {
      padding: 4,
    },
    input: {
      backgroundColor: 'transparent',
      color: 'white',
    },
    deleteButton: {
        marginTop: 4,
        marginLeft: 4,
        alignSelf: 'center',
    },
  });
  
  export default EditableRow;
