
export interface PurchaseItemDTO {
    itemID: number | null;
    name?: string;
    brand?: string;
    categories: string[];
    unit: string;
    amount: number;
    price: number;
    // reminder?: Reminder;
  }