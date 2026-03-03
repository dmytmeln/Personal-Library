export interface AdminCategoryDto {
  id?: number;
  translations: { [key: string]: AdminCategoryTranslationDto };
}

export interface AdminCategoryTranslationDto {
  name: string;
  description: string;
}
