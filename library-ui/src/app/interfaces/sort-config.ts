export interface SortOption {
  field: string;
  label: string;
}

export interface ActiveSort {
  field: string;
  direction: 'asc' | 'desc';
}
