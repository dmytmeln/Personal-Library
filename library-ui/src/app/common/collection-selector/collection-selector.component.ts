import {ChangeDetectorRef, Component, input, OnInit, output, ViewChild} from '@angular/core';
import {CollectionNode} from '../../interfaces/collection-node';
import {CollectionService} from '../../services/collection.service';
import {SelectedCollection} from '../../interfaces/selected-collection';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatTree, MatTreeModule} from '@angular/material/tree';
import {CommonModule} from '@angular/common';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

@Component({
  selector: 'app-collection-selector',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatTreeModule,
    TranslocoDirective,
  ],
  templateUrl: './collection-selector.component.html',
  styleUrl: './collection-selector.component.scss'
})
export class CollectionSelectorComponent implements OnInit {

  readonly SHOW_DELAY = 200;
  initialSelectionId = input<number | null>(null);
  disabledIds = input<number[]>([]);
  showRoot = input<boolean>(true);
  onSelect = output<SelectedCollection>();

  @ViewChild('tree') tree!: MatTree<CollectionNode>;

  dataSource: CollectionNode[] = [];
  childrenAccessor = (node: CollectionNode) => node.children;
  hasChild = (_: number, node: CollectionNode) => !!node.children && node.children.length > 0;

  selectedId: number | null = null;

  constructor(
    private collectionService: CollectionService,
    private cdr: ChangeDetectorRef,
    private translocoService: TranslocoService
  ) {
  }

  ngOnInit(): void {
    this.selectedId = this.initialSelectionId();
    this.collectionService.getTree().subscribe(tree => {
      this.dataSource = tree;
      if (this.selectedId !== null) {
        const flat = this.flatten(tree);
        const found = flat.find(c => c.id === this.selectedId);
        if (found) {
          this.onSelect.emit({id: found.id, name: found.name});
        }
      } else if (this.showRoot()) {
        this.onSelect.emit({id: null, name: this.translocoService.translate('collections.selector.root')});
      }

      this.cdr.detectChanges();
      if (this.tree) {
        this.tree.expandAll();
      }
    });
  }

  private flatten(cols: CollectionNode[]): CollectionNode[] {
    let res: CollectionNode[] = [];
    for (const c of cols) {
      res.push(c);
      if (c.children) res = res.concat(this.flatten(c.children));
    }
    return res;
  }

  isDisabled(id: number | null): boolean {
    if (id === null) return false;
    return this.disabledIds().includes(id);
  }

  select(id: number | null, name: string): void {
    if (this.isDisabled(id)) return;
    this.selectedId = id;
    this.onSelect.emit({id, name});
  }

}
