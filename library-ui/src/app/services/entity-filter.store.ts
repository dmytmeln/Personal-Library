import {signal} from '@angular/core';
import {toObservable} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs';

export class EntityFilterStore<T extends object> {

  readonly state = signal<T>({} as T);

  readonly filtersChanged$ = toObservable(this.state).pipe(
    skip(1),
    debounceTime(350),
    distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr))
  );

  constructor(initialState: T) {
    this.state.set(initialState);
  }

  update<K extends keyof T>(key: K, value: T[K]): void {
    this.state.update((s) => ({...s, [key]: value}));
  }

  reset(initialState: T): void {
    this.state.set(initialState);
  }

  hasActiveFilters(predicate: (values: T) => boolean): boolean {
    return predicate(this.state());
  }

}
