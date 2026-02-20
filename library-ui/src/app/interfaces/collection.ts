export interface Collection {
  id: number;
  name: string;
  description?: string;
  color?: string;
  updatedAt: Date;
  parentId?: number;
  children: Collection[];
}
