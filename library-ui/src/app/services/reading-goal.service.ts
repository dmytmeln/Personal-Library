import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { ReadingGoal } from '../interfaces/reading-goal';

@Injectable({
  providedIn: 'root'
})
export class ReadingGoalService {

  constructor(private api: ApiService) { }

  getGoal(year: number): Observable<ReadingGoal> {
    return this.api.get<ReadingGoal>('/reading-goals', { params: { year } });
  }

  saveOrUpdateGoal(goal: ReadingGoal): Observable<ReadingGoal> {
    return this.api.put<ReadingGoal>('/reading-goals', { body: goal });
  }

  deleteGoal(year: number): Observable<void> {
    return this.api.delete<void>('/reading-goals', { params: { year } });
  }

}
