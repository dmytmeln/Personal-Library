import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {Note, NoteRequest} from '../interfaces/note';

@Injectable({
  providedIn: 'root'
})
export class NoteService {

  private readonly API_URL = '/notes';

  constructor(private api: ApiService) { }

  getByLibraryBookId(libraryBookId: number): Observable<Note> {
    return this.api.get(this.API_URL, { params: { libraryBookId } });
  }

  upsert(request: NoteRequest): Observable<Note> {
    return this.api.put(this.API_URL, {body: request});
  }

  delete(libraryBookId: number): Observable<void> {
    return this.api.delete(this.API_URL, { params: { libraryBookId } });
  }

}
