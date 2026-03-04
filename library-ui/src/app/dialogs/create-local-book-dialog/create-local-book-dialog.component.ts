import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatChipsModule} from '@angular/material/chips';
import {TranslocoDirective, TranslocoPipe} from '@jsverse/transloco';
import {LibraryBookStatus, LIBRARY_BOOK_STATUSES} from '../../interfaces/library-book';
import {CategoryService} from '../../services/category.service';
import {AuthorService} from '../../services/author.service';
import {Category} from '../../interfaces/category';
import {Author} from '../../interfaces/author';
import {CreateLocalBook} from '../../interfaces/create-local-book';
import {AutocompleteSearchStore} from '../../services/autocomplete-search.store';
import {AutocompleteFilterComponent} from '../../common/filters/autocomplete-filter/autocomplete-filter.component';

@Component({
  selector: 'app-create-local-book-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    TranslocoDirective,
    TranslocoPipe,
    AutocompleteFilterComponent
  ],
  templateUrl: './create-local-book-dialog.component.html',
  styleUrl: './create-local-book-dialog.component.scss'
})
export class CreateLocalBookDialogComponent implements OnInit {
  form: FormGroup;
  readonly statuses = LIBRARY_BOOK_STATUSES;

  readonly categorySearch: AutocompleteSearchStore<Category>;
  readonly authorSearch: AutocompleteSearchStore<Author>;

  selectedCategory = signal<Category | null>(null);
  selectedAuthors = signal<Author[]>([]);

  useCustomCategory = signal<boolean>(false);
  useCustomAuthor = signal<boolean>(false);

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<CreateLocalBookDialogComponent>,
    private readonly categoryService: CategoryService,
    private readonly authorService: AuthorService,
  ) {
    this.categorySearch = new AutocompleteSearchStore<Category>(
      (query, page, size) => this.categoryService.search({name: query, page, size})
    );
    this.authorSearch = new AutocompleteSearchStore<Author>(
      (query, page, size) => this.authorService.search({name: query, page, size})
    );

    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(2000)]],
      bookLanguage: ['', [Validators.maxLength(50)]],
      status: [LibraryBookStatus.NO_TAG, [Validators.required]],
      publishYear: [null, [Validators.min(0)]],
      pages: [null, [Validators.min(1)]],
      categoryId: [null],
      customCategoryName: ['', [Validators.maxLength(255)]],
      customAuthorName: ['', [Validators.maxLength(255)]],
      authorIds: [[]]
    });
  }

  ngOnInit(): void {
  }

  onCategorySearch(query: string): void {
    this.categorySearch.search(query);
  }

  onCategorySelected(cat: Category): void {
    this.selectedCategory.set(cat);
    this.form.patchValue({categoryId: cat.id});
  }

  clearCategory(): void {
    this.selectedCategory.set(null);
    this.form.patchValue({categoryId: null});
    this.categorySearch.clear();
  }

  onAuthorSearch(query: string): void {
    this.authorSearch.search(query);
  }

  onAuthorSelected(author: Author): void {
    if (this.selectedAuthors().some(a => a.id === author.id)) return;
    this.selectedAuthors.update(list => [...list, author]);
    this.form.patchValue({authorIds: this.selectedAuthors().map(a => a.id)});
    this.authorSearch.clear();
  }

  removeAuthor(authorId: number): void {
    this.selectedAuthors.update(list => list.filter(a => a.id !== authorId));
    this.form.patchValue({authorIds: this.selectedAuthors().map(a => a.id)});
  }

  toggleCustomCategory(): void {
    const newVal = !this.useCustomCategory();
    this.useCustomCategory.set(newVal);
    if (newVal) {
      this.clearCategory();
    } else {
      this.form.patchValue({customCategoryName: ''});
    }
  }

  toggleCustomAuthor(): void {
    const newVal = !this.useCustomAuthor();
    this.useCustomAuthor.set(newVal);
    if (newVal) {
      this.selectedAuthors.set([]);
      this.form.patchValue({authorIds: []});
      this.authorSearch.clear();
    } else {
      this.form.patchValue({customAuthorName: ''});
    }
  }

  isCategorySpecified(): boolean {
    const val = this.form.value;
    return !!(val.categoryId || (val.customCategoryName && val.customCategoryName.trim()));
  }

  isAuthorSpecified(): boolean {
    const val = this.form.value;
    return !!((val.authorIds && val.authorIds.length > 0) || (val.customAuthorName && val.customAuthorName.trim()));
  }

  onSubmit(): void {
    if (this.form.invalid || !this.isCategorySpecified() || !this.isAuthorSpecified()) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.value;
    const result: CreateLocalBook = {
      title: val.title,
      description: val.description,
      bookLanguage: val.bookLanguage,
      status: val.status,
      publishYear: val.publishYear,
      pages: val.pages,
      categoryId: val.categoryId,
      authorIds: val.authorIds,
      customCategoryName: val.customCategoryName,
      customAuthorName: val.customAuthorName
    };

    this.dialogRef.close(result);
  }
}
