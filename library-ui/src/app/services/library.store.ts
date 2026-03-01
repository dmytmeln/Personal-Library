import {Injectable, signal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LibraryStore {
  private readonly _refreshVersion = signal(0);
  readonly refreshVersion = this._refreshVersion.asReadonly();

  triggerRefresh() {
    this._refreshVersion.update(v => v + 1);
  }
}
