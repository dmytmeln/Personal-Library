export interface Author {
  id: number;
  fullName: string;
  country: string;
  birthYear: number;
  deathYear?: number;
  biography?: string;
  booksCount?: number;
}
