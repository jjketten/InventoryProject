// types/item.ts
export interface Item {
    itemID?: number;         
    name: string;
    brand: string;
    unit: string;
    amount: number;
    categories: Array<string>;
  }