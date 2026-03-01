import {signal} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, switchMap} from 'rxjs/operators';

export class AutocompleteSearchStore<T> {
  readonly query = signal('');
  readonly results = signal<T[]>([]);

  private readonly searchSubject = new Subject<string>();

  constructor(
    private searchFn: (query: string, page: number, size: number) => Observable<{content: T[]; page: {totalElements: number}}>,
    private readonly debounceMs: number = 450,
    private readonly pageSize: number = 10
  ) {
    this.setupSubscription();
  }

  private setupSubscription(): void {
    this.searchSubject.pipe(
      debounceTime(this.debounceMs),
      distinctUntilChanged(),
      switchMap(query => this.searchFn(query, 0, this.pageSize))
    ).subscribe(response => this.results.set(response.content));
  }

  search(query: string): void {
    this.query.set(query);
    this.searchSubject.next(query);
  }

  clear(): void {
    this.query.set('');
    this.results.set([]);
  }
}
