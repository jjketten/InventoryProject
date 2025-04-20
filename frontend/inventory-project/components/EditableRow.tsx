// components/EditableRow.tsx

import React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { TextInput, IconButton } from 'react-native-paper';
import { ColumnConfig } from './ColumnConfig';
import ChipEditor from './ChipEditor';

interface EditableRowProps<T> {
  item: T;
  index: number;
  columns: ColumnConfig<T>[];
  onEdit: (index: number, updated: T) => void;
  onEditToggle?: (index: number, field: keyof T, currentValue: boolean) => void;
  onDelete?: (index: number) => void;
  isNew?: boolean;
}

function EditableRow<T extends object>({
  item,
  index,
  columns,
  onEdit,
  onEditToggle,
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
        const key = col.key as keyof T;
        const value = item[key];

        return (
          <View key={col.key as string} style={[styles.cell, { flex: col.width ?? 1 }]}>
            {/* Category Editor */}
            {col.key === 'categories' ? (
              <ChipEditor
                categories={(value as string[]) || []}
                onChange={(updated) => onEdit(index, { ...item, [key]: updated })}
              />
            ) : 
            /* Boolean Toggle */
            typeof value === 'boolean' && col.editable && onEditToggle ? (
              <IconButton
                icon={value ? 'check-circle' : 'checkbox-blank-circle-outline'}
                size={20}
                onPress={() => onEditToggle(index, key, value)}
              />
            ) :
            /* Text or Number Input */
            col.editable ? (
              <TextInput
                value={String(value ?? '')}
                onChangeText={(text) =>
                  onEdit(index, {
                    ...item,
                    [key]: col.inputType === 'number' ? parseFloat(text) || 0 : text,
                  })
                }
                keyboardType={col.inputType === 'number' ? 'numeric' : 'default'}
                mode="outlined"
                dense
                style={styles.input}
              />
            ) :
            /* Non-editable text */
            (
              <Text style={styles.text}>
                {col.render ? col.render(value, item) : String(value)}
              </Text>
            )}
          </View>
        );
      })}

      {/* Delete button */}
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
  text: {
    color: 'white',
  },
  deleteButton: {
    marginTop: 4,
    marginLeft: 4,
    alignSelf: 'center',
  },
});

export default EditableRow;
