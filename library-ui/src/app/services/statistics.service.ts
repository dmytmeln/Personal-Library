import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { DashboardStats } from '../interfaces/dashboard-stats';

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  constructor(private api: ApiService) { }

  getDashboardStats(year: number): Observable<DashboardStats> {
    return this.api.get<DashboardStats>('/statistics/dashboard', { params: { year } });
  }

}
