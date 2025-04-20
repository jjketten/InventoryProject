export interface Reminder {
    itemID: number;
    itemName: string;
    brand: string;
    unit: string;
    amount: number;
  
    purchaseID: number;
    date: string; // ISO format string, purchase date (from java.time.LocalDate)
    store: string;
    totalCost: number;
  
    datetime: string; // ISO format string, expiration date (from java.time.LocalDateTime)
    completed: boolean;
  
    description: string;
  }
  