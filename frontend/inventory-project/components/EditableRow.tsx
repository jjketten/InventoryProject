// components/EditableRow.tsx

import React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { TextInput, IconButton, DataTable } from 'react-native-paper';
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
    <DataTable.Row
      style={[
        styles.row,
        isNew && { backgroundColor: '#2a2f3a' },
      ]}
    >
      {columns.map((col) => {
        const key = col.key as keyof T;
        const value = item[key];

        return (
          <DataTable.Cell key={col.key as string} style={[styles.cell, { flex: col.width ?? 40 }]}>
            {/* Custom Render Logic First */}
            {col.render ? (
              col.render(value, item, index)
            ) : col.key === 'categories' ? (
              <ChipEditor
                categories={(value as string[]) || []}
                onChange={(updated) => onEdit(index, { ...item, [key]: updated })}
              />
            ) : typeof value === 'boolean' && col.editable && onEditToggle ? (
              <IconButton
                icon={value ? 'check-circle' : 'checkbox-blank-circle-outline'}
                size={20}
                onPress={() => onEditToggle(index, key, value)}
              />
            ) : col.editable ? (
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
            ) : (
              <Text style={styles.text}>
                {String(value)}
              </Text>
            )}
          </DataTable.Cell>
        );
      })}


      {/* Delete button */}
      <DataTable.Cell key={'del_' + (index.toString())} style={[styles.cell, { flex: 10 }]}>
        {onDelete && (
          <IconButton
            icon="delete"
            size={18}
            onPress={() => onDelete(index)}
            style={styles.deleteButton}
          />
        )}
      </DataTable.Cell>
    </DataTable.Row>
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
    // width: 'auto',
    flex: 1,
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
