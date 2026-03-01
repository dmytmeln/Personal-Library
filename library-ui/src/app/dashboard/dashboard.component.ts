import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {StatisticsService} from '../services/statistics.service';
import {ReadingGoalService} from '../services/reading-goal.service';
import {DashboardStats, MonthlyReadingActivity} from '../interfaces/dashboard-stats';
import {ReadingGoal} from '../interfaces/reading-goal';
import {SetGoalDialogComponent} from './set-goal-dialog/set-goal-dialog.component';
import {filter} from 'rxjs';
import {MatSnackCommon} from '../common/mat-snack-common';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {Router} from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatDividerModule,
    MatDialogModule,
    TranslocoDirective,
    MatSelectModule,
    MatFormFieldModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  readonly stats = signal<DashboardStats | null>(null);
  readonly goal = signal<ReadingGoal | null>(null);
  readonly currentYear = new Date().getFullYear();
  readonly selectedYear = signal<number>(this.currentYear);
  readonly availableYears: number[] = [];

  private snackCommon: MatSnackCommon;

  constructor(
    private statisticsService: StatisticsService,
    private goalService: ReadingGoalService,
    private dialog: MatDialog,
    private translocoService: TranslocoService,
    matSnackBar: MatSnackBar,
    private router: Router,
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
    for (let i = 0; i < 10; i++) {
      this.availableYears.push(this.currentYear - i);
    }
  }

  ngOnInit(): void {
    this.loadData();
  }

  onYearChange(year: number): void {
    this.selectedYear.set(year);
    this.loadData();
  }

  loadData(): void {
    const year = this.selectedYear();
    this.statisticsService.getDashboardStats(year).subscribe(stats => this.stats.set(stats));
    this.goalService.getGoal(year).subscribe({
      next: goal => this.goal.set(goal),
      error: () => this.goal.set(null)
    });
  }

  openSetGoalDialog(): void {
    const dialogRef = this.dialog.open(SetGoalDialogComponent, {
      data: {
        goal: this.goal(),
        year: this.selectedYear()
      },
      width: '400px'
    });

    dialogRef.afterClosed().pipe(filter(Boolean)).subscribe((result) => {
      this.loadData();
      if (result === 'saved') {
        this.snackCommon.showSuccess(this.translocoService.translate('dashboard.success.goalSaved'));
      } else if (result === 'deleted') {
        this.snackCommon.showSuccess(this.translocoService.translate('dashboard.success.goalDeleted'));
      }
    });
  }

  getPercent(current: number, target: number): number {
    if (!target || target <= 0) return 0;
    return Math.min(Math.round((current / target) * 100), 100);
  }

  getMonthlyActivityPercent(activity: MonthlyReadingActivity) {
    return (this.stats()?.summary?.booksReadCount || 0) > 0
      ? (activity.count / this.stats()!.summary.booksReadCount) * 100
      : 0;
  }

  goToAuthorDetails(id: number): void {
    this.router.navigate(['/author-details'], {state: {id}});
  }

  goToCategoryDetails(id: number): void {
    this.router.navigate(['/category-details'], {state: {id}});
  }

}
