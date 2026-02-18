export interface Book {
  id: number,
  title: string,
  categoryId: number,
  categoryName: string,
  publishYear: number,
  language: string,
  pages: number,
  description?: string,
  coverImageUrl?: string,
  authors: Record<number, string>
}
