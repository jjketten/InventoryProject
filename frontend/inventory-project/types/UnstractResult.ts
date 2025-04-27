
export interface UnstractItem {
    brand: string;
    cost: string;
    itemName: string;
    productCode: string;
    quantity: string;
    units: string;
  }
  
  export interface UnstractResult {
    date: string;       // "04/27/19"
    storeName: string; 
    tax: string;
    totalCost: string;
    items: UnstractItem[];
  }