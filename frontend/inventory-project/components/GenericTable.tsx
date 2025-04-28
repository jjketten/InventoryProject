import React from 'react';
import { View, StyleSheet } from 'react-native';
import { DataTable } from 'react-native-paper';
import EditableRow from './EditableRow';
import { ColumnConfig } from './ColumnConfig';

interface Props<T> {
  data: T[];
  columns: ColumnConfig<T>[];
  onEdit?: (index: number, updatedItem: T) => void;
  onDelete?: (index: number) => void;
  onEditToggle?: (index: number, field: keyof T, currentValue: boolean) => void;
  highlightRows?: number; // number of rows from bottom to highlight
  onAddReminder?: (index: number) => void; 
}

function GenericTable<T extends object>({
  data,
  columns,
  onEdit,
  onDelete,
  onEditToggle,
  highlightRows = 0,
  onAddReminder,
}: Props<T>) {
  const highlightStartIndex = data.length - highlightRows;

  return (
    <DataTable>
      {/* Table Header */}
      <DataTable.Header>
        {columns.map((col) => (
          (!((col.key == "reminderDateTime" || col.key != "reminderDescription") && col.editable == false))  && //oh god
          (
          <DataTable.Title
            key={col.key as string}
            style={{ flex: col.width ?? 40 }}
          >
            {col.label}
          </DataTable.Title>
          )
        ))}
        {onDelete && (
          <DataTable.Title style={{ flex: 10 }}> </DataTable.Title>
        )}
      </DataTable.Header>

      {/* Rows */}
      {data.map((row, index) => (
        <EditableRow
          key={index}
          item={row}
          index={index}
          columns={columns}
          onEdit={onEdit ?? (()=>{})}
          onDelete={onDelete}
          onEditToggle={onEditToggle}
          isNew={index >= highlightStartIndex}
          onAddReminder={onAddReminder}
        />
      ))}
    </DataTable>
  );
}

export default GenericTable;
