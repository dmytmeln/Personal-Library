export interface CollectionNode {
  id: number;
  name: string;
  expandable: boolean;
  level: number;
  parentId: number | null;
}
