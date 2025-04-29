import React, { useState, useEffect } from 'react';
import { DataTable, TextInput, IconButton } from 'react-native-paper';
import { StyleSheet, Text, View } from 'react-native';
import { ColumnConfig } from './ColumnConfig';
import ChipEditor from './ChipEditor';
import { format, isValid, parse, parseISO } from 'date-fns';

interface EditableRowProps<T> {
  item: T;
  index: number;
  columns: ColumnConfig<T>[];
  onEdit: (index: number, updated: T) => void;
  onEditToggle?: (index: number, field: keyof T, currentValue: boolean) => void;
  onDelete?: (index: number) => void;
  // isNew?: boolean;
  highlightStartIndex?: number,
  onAddReminder?: (index: number) => void;
}

export default function EditableRow<T extends object>({
  item,
  index,
  columns,
  onEdit,
  onEditToggle,
  onDelete,
  // isNew = false,
  highlightStartIndex = 999,
  onAddReminder,
}: EditableRowProps<T>) {
  // const realIndex = columns.indexOf()
  const isNew = (index >= highlightStartIndex)
  return (
    <DataTable.Row style={[styles.row, isNew && styles.newRow]}>
      {columns.map((col) => {
        const key = col.key as keyof T;
        const rawVal = item[key] as any;
        const isReminderButton = (col.key == "reminderDateTime");
        var reminderDesc = "?" as any;
        if(isReminderButton) {
          const reminderDescPos = columns.map(c => c.key).indexOf("reminderDescription");
          reminderDesc = item["reminderDescription" as keyof T];
        }
        const reminderButtonDisabled = (reminderDesc == "?" || reminderDesc == "" || reminderDesc == null || reminderDesc == undefined)
        //   if(reminderDesc == "") {
        //     reminderDesc = "?"
        //   }
        // } else {
        //   reminderDesc = "?";
        // }

        // custom
        if(col.render) return (
          <DataTable.Cell
            key={col.key as string}
            style={[styles.cell, { flex: col.width ?? 40 }]}
          >
            {col.render(rawVal, item, index)}
          </DataTable.Cell>
        )

        // 2) chip editor
        if (col.key === 'categories' && col.editable) {
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
        if ((col.inputType === 'number' || col.inputType === 'decimal') && col.editable) {
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

        if((col.key == "reminderDateTime" || col.key == "reminderDescription") && col.editable == false) {
          return (<></>);
        }

        // Reminder Button
        if (col.key === 'reminderDateTime' && onAddReminder) {
          return (
            <DataTable.Cell
              key={col.key as string}
              style={[styles.cell, { flex: col.width ?? 40, flexWrap: 'wrap', flexDirection: 'column'}]}
            >
              <div style={{textAlign: 'center'}}>
                <IconButton
                  disabled = {reminderButtonDisabled}
                  icon="bell-plus"
                  size={18}
                  onPress={() => onAddReminder(index)}
                  // style={[flex: 1]}
                />
              </div>
              <Text>{(rawVal == "" || rawVal == null || rawVal == undefined) ? "" : (parseISO(rawVal).toLocaleDateString('en-US') +" " + parseISO(rawVal).toLocaleTimeString('en-US'))}</Text>
            </DataTable.Cell>
          );
        }

        if (!col.editable && (key.toString()).includes("dateTime")) {
          const newdate = (new Date(Date.parse(rawVal)))
          return (
            <DataTable.Cell
              key={col.key as string}
              style={[styles.cell, { flex: col.width ?? 40 }]}
            >
              <Text style={styles.text}>{newdate.toLocaleDateString() + " " + newdate.toLocaleTimeString()}</Text>
            </DataTable.Cell>
          );
        }

        // other read-only columns
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

        //default
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

function NumericCell<T extends object>({
  style,
  index,
  item,
  field,
  rawValue,
  inputType,
  onEdit,
}: {
  style: any;
  index: number;
  item: T;
  field: keyof T;
  rawValue: any;
  inputType?: 'number' | 'decimal';
  onEdit: (index: number, updated: T) => void;
}) {
  const [text, setText] = useState(() => String(rawValue ?? ''));

  useEffect(() => {
    setText(String(item[field] ?? ''));
  }, [item, field]);

  function handleBlur() {
    if (inputType === 'number') {
      const n = parseInt(text, 10);
      if (!isNaN(n)) {
        onEdit(index, { ...item, [field]: n } as T);
      }
    } else if (inputType === 'decimal') {
      const n = parseFloat(text);
      if (!isNaN(n)) {
        onEdit(index, { ...item, [field]: n } as T);
        setText(n.toFixed(2)); // format after committing
      }
    }
  }

  return (
    <DataTable.Cell style={style}>
      <TextInput
        value={text}
        onChangeText={(t) => {
          // Just update local text immediately, even if partial
          if (inputType === 'number') {
            if (/^\d*$/.test(t)) {
              setText(t);
            }
          } else if (inputType === 'decimal') {
            if (/^\d*\.?\d*$/.test(t)) {
              setText(t);
            }
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
