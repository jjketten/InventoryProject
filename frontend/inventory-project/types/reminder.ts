export interface Reminder {
    itemID: number | null;
    itemName?: string;
    itemBrand: string | undefined;
    itemUnit: string;
    itemAmount: number;
  
    purchaseID: number | null;
    purchaseDate: string; // ISO format string, purchase date (from java.time.LocalDate)
    purchaseStore: string;
    purchaseTotalCost: number;
  
    dateTime: string; // ISO format string, expiration date (from java.time.LocalDateTime)
    completed: boolean;
  
    description: string;
  }
  