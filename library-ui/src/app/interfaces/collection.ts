import {BasicCollection} from './basic-collection';

export interface Collection {
  id: number;
  name: string;
  description?: string;
  updatedAt: Date;
  parentId?: number;
  children: BasicCollection[];
  ancestors?: BasicCollection[];
}
