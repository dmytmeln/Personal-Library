export interface AdminBookDto {
  id?: number;
  categoryId?: number;
  publishYear?: number;
  pages?: number;
  coverImageUrl?: string;
  authorIds?: number[];
  translations: { [key: string]: AdminBookTranslationDto };
}

export interface AdminBookTranslationDto {
  title: string;
  bookLanguage: string;
  description: string;
}
