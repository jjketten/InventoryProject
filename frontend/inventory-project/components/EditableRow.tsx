import React, { useState, useEffect } from 'react';
import { DataTable, TextInput, IconButton } from 'react-native-paper';
import { StyleSheet, Text, View } from 'react-native';
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

export default function EditableRow<T extends object>({
  item,
  index,
  columns,
  onEdit,
  onEditToggle,
  onDelete,
  isNew = false,
}: EditableRowProps<T>) {
  return (
    <DataTable.Row style={[styles.row, isNew && styles.newRow]}>
      {columns.map((col) => {
        const key = col.key as keyof T;
        const rawVal = item[key] as any;

        // 1) read-only columns
        if (!col.editable) {
          return (
            <DataTable.Cell
              key={col.key as string}
              style={[styles.cell, { flex: col.width ?? 40 }]}
            >
              <Text style={styles.text}>{String(rawVal)}</Text>
            </DataTable.Cell>
          );
        }

        // 2) chip editor
        if (col.key === 'categories') {
          return (
            <DataTable.Cell
              key={col.key as string}
              style={[styles.cell, { flex: col.width ?? 40 }]}
            >
              <ChipEditor
                categories={(rawVal as string[]) || []}
                onChange={(updated) =>
                  onEdit(index, { ...item, [key]: updated } as T)
                }
              />
            </DataTable.Cell>
          );
        }

        // 3) boolean toggle
        if (typeof rawVal === 'boolean' && onEditToggle) {
          return (
            <DataTable.Cell
              key={col.key as string}
              style={[styles.cell, { flex: col.width ?? 40 }]}
            >
              <IconButton
                icon={rawVal ? 'check-circle' : 'checkbox-blank-circle-outline'}
                size={20}
                onPress={() => onEditToggle(index, key, rawVal)}
              />
            </DataTable.Cell>
          );
        }

        // 4) numeric / decimal
        if (col.inputType === 'number' || col.inputType === 'decimal') {
          return (
            <NumericCell
              key={col.key as string}
              style={[styles.cell, { flex: col.width ?? 40 }]}
              index={index}
              item={item}
              field={key}
              rawValue={rawVal}
              inputType={col.inputType}
              onEdit={onEdit}
            />
          );
        }

        // 5) all other editable fields
        return (
          <DataTable.Cell
            key={col.key as string}
            style={[styles.cell, { flex: col.width ?? 40 }]}
          >
            <TextInput
              value={String(rawVal ?? '')}
              onChangeText={(text) =>
                onEdit(index, { ...item, [key]: text } as T)
              }
              mode="outlined"
              dense
              style={styles.input}
            />
          </DataTable.Cell>
        );
      })}

      {onDelete && (
        <DataTable.Cell style={[styles.cell, { flex: 10 }]}>
          <IconButton
            icon="delete"
            size={18}
            onPress={() => onDelete(index)}
            style={styles.deleteButton}
          />
        </DataTable.Cell>
      )}
    </DataTable.Row>
  );
}

// const InventoryScreen: React.FC = () => {
// interface cellProps<T> {
//   style : any,
//   index : number,
//   item : T,
//   field : keyof T,
//   rawValue : any,
//   inputType?: 'number' | 'decimal';
//   onEdit: (i: number, updated: T) => void;
// }

// const NumericCell2: React.FC<cellProps<any>> = ({ style, index, item, field, rawValue, inputType, onEdit }) => {
function NumericCell<T extends object>({
  style,
  index,
  item,
  field,
  rawValue,
  inputType,
  onEdit,
}: {
  style: any,
  index: number;
  item: T;
  field: keyof T;
  rawValue: any;
  inputType?: 'number' | 'decimal';
  onEdit: (i: number, updated: T) => void;
}) {
  const [text, setText] = useState(() => String(rawValue ?? ''));

  // keep local text in sync if item[field] changes from outside
  useEffect(() => {
    setText(String(item[field] ?? ''));
  }, [item, field]);

  function commitIfValid(value: string) {
    if (inputType === 'number') {
      const n = parseInt(value, 10);
      if (!isNaN(n)) {
        onEdit(index, { ...item, [field]: n } as T);
      }
    } else {
      // decimal
      const n = Number(value);
      if (!isNaN(n)) {
        onEdit(index, { ...item, [field]: n } as T);
      }
    }
  }

  function handleBlur() {
    // format on blur
    if (inputType === 'decimal') {
      const n = Number(text);
      if (!isNaN(n)) {
        const formatted = n.toFixed(2);
        setText(formatted);
        onEdit(index, { ...item, [field]: n } as T);
      }
    }
  }

  return (
    <DataTable.Cell style={style}>
      <TextInput
        value={text}
        onChangeText={(t) => {
          setText(t);
          // only commit if the text is already a valid number (or partial decimal like '12.' is okay to hold locally)
          if (/^\d+$/.test(t) && inputType === 'number') {
            commitIfValid(t);
          } else if (/^\d*\.?\d*$/.test(t) && inputType === 'decimal') {
            commitIfValid(t);
          }
        }}
        onBlur={handleBlur}
        keyboardType={inputType === 'decimal' ? 'decimal-pad' : 'numeric'}
        mode="outlined"
        dense
        style={styles.input}
      />
    </DataTable.Cell>
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
  newRow: {
    backgroundColor: '#2a2f3a',
  },
  cell: {
    padding: 4,
    flex: 40,
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
