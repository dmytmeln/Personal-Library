export interface CollectionNode {
  id: number;
  name: string;
  parentId?: number;
  children: CollectionNode[];
}
