export interface AdminAuthorDto {
  id?: number;
  birthYear?: number;
  deathYear?: number;
  translations: { [key: string]: AdminAuthorTranslationDto };
}

export interface AdminAuthorTranslationDto {
  fullName: string;
  country: string;
  biography: string;
}
