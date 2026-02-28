import {BasicCollection} from './basic-collection';
import {CollectionBook} from './collection-book';

export interface CollectionDetails {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  createdAt: Date;
  ancestors: BasicCollection[];
  children: BasicCollection[];
  books: CollectionBook[];
}
