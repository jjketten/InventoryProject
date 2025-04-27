import { CalendarDate } from 'react-native-paper-dates/lib/typescript/Date/Calendar';
import { PurchaseItemDTO } from './PurchaseItemDTO';

export interface PurchaseDTO {
    purchaseID: number | null;
    date: string;        //"2025-04-27"
    store: string;
    totalCost: number;
    items: PurchaseItemDTO[];
  }