export interface DashboardSummary {
  totalLibraryBooks: number;
  booksReadCount: number;
  pagesReadCount: number;
  averageRating: number;
  currentlyReadingCount: number;
  booksAddedThisYear: number;
  totalRatedBooks: number;
}

export interface CategoryDistribution {
  categoryId: number;
  categoryName: string;
  count: number;
}

export interface StatusDistribution {
  status: string;
  count: number;
}

export interface LanguageDistribution {
  language: string;
  count: number;
}

export interface AuthorCountryDistribution {
  country: string;
  count: number;
}

export interface MonthlyReadingActivity {
  month: number;
  count: number;
}

export interface TopAuthor {
  authorId: number;
  authorName: string;
  count: number;
}

export interface DashboardStats {
  summary: DashboardSummary;
  categoryDistribution: CategoryDistribution[];
  statusDistribution: StatusDistribution[];
  languageDistribution: LanguageDistribution[];
  authorCountryDistribution: AuthorCountryDistribution[];
  monthlyReadingActivity: MonthlyReadingActivity[];
  topAuthors: TopAuthor[];
}
