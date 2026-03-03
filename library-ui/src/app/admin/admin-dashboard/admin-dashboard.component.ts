import { Component, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { SearchComponent } from '../../search/search.component';
import { AuthorListComponent } from '../../author-list/author-list.component';
import { CategoryListComponent } from '../../category-list/category-list.component';
import { Router } from '@angular/router';
import { TranslocoDirective, TranslocoService } from '@jsverse/transloco';
import { AdminService } from '../../services/admin.service';
import { Author } from '../../interfaces/author';
import { Category } from '../../interfaces/category';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSnackCommon } from '../../common/mat-snack-common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../dialogs/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    SearchComponent,
    AuthorListComponent,
    CategoryListComponent,
    TranslocoDirective,
    MatDialogModule,
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent {

  authorList = viewChild(AuthorListComponent);
  categoryList = viewChild(CategoryListComponent);

  activeTab = 0;
  private snackCommon: MatSnackCommon;

  constructor(
    private router: Router,
    private adminService: AdminService,
    private translocoService: TranslocoService,
    private dialog: MatDialog,
    matSnackBar: MatSnackBar
  ) {
    this.snackCommon = new MatSnackCommon(matSnackBar);
  }

  addEntity(): void {
    if (this.activeTab === 0) this.router.navigate(['/admin/book/new']);
    if (this.activeTab === 1) this.router.navigate(['/admin/author/new']);
    if (this.activeTab === 2) this.router.navigate(['/admin/category/new']);
  }

  onAuthorDeleted(author: Author): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: { message: this.translocoService.translate('common.confirmDelete') }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.adminService.deleteAuthor(author.id).subscribe({
          next: () => {
            this.snackCommon.showSuccess(this.translocoService.translate('common.success.deleted'));
            const list = this.authorList();
            if (list) (list as any).loadAuthors();
          },
          error: (err) => this.snackCommon.showError(err)
        });
      }
    });
  }

  onCategoryDeleted(category: Category): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: { message: this.translocoService.translate('common.confirmDelete') }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.adminService.deleteCategory(category.id).subscribe({
          next: () => {
            this.snackCommon.showSuccess(this.translocoService.translate('common.success.deleted'));
            const list = this.categoryList();
            if (list) (list as any).loadCategories();
          },
          error: (err) => this.snackCommon.showError(err)
        });
      }
    });
  }
}
