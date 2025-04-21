export interface RecipeItemDTO {
    itemID?: number;           // Only needed when sending to backend
    itemName: string;          // Used for autocomplete & display
    unit: string;
    amount: number;
    categoryID?: number | null;  // Used when saving, not required for frontend display
    categoryName: string;
  }
  