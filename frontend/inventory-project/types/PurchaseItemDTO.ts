export interface PurchaseItemDTO {
    itemID: number | null;
    name?: string;
    brand?: string;
    unit: string;
    amount: number;
    price: number;
  }