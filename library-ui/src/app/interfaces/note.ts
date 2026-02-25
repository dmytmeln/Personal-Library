export interface Note {
  id: number;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface NoteRequest {
  libraryBookId: number;
  content: string;
}
