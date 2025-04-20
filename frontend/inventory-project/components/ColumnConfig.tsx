export type ColumnConfig<T> = {
    key: keyof T;
    label: string;
    editable?: boolean;
    inputType?: 'text' | 'number';
    width?: number;
  };
  