export type ColumnConfig<T> = {
  key: keyof T | string;
  label: string;
  editable?: boolean;
  inputType?: 'text' | 'number' | 'datetime' | 'decimal';
  width?: number;
  isAction?: boolean;
  render?: (value: any, row: T, index?: number) => React.ReactNode;
};
