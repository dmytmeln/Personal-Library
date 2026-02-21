import {Component, input, OnInit, output} from '@angular/core';
import {CollectionNode} from '../../interfaces/collection-node';
import {CollectionService} from '../../services/collection.service';
import {SelectedCollection} from '../../interfaces/selected-collection';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
  selector: 'app-collection-selector',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ],
  templateUrl: './collection-selector.component.html',
  styleUrl: './collection-selector.component.scss'
})
export class CollectionSelectorComponent implements OnInit {
  initialSelectionId = input<number | null>(null);
  disabledIds = input<number[]>([]);
  showRoot = input<boolean>(true);
  onSelect = output<SelectedCollection>();

  leafPaths: CollectionNode[][] = [];
  selectedId: number | null = null;

  constructor(private collectionService: CollectionService) {
  }

  ngOnInit(): void {
    this.selectedId = this.initialSelectionId();
    this.collectionService.getTree().subscribe(tree => {
      this.generateLeafPaths(tree);
      if (this.selectedId !== null) {
        const flat = this.flatten(tree);
        const found = flat.find(c => c.id === this.selectedId);
        if (found) {
          this.onSelect.emit({id: found.id, name: found.name});
        }
      } else {
        this.onSelect.emit({id: null, name: 'Root'});
      }
    });
  }

  private generateLeafPaths(collections: CollectionNode[], currentPath: CollectionNode[] = []) {
    for (const col of collections) {
      const newPath = [...currentPath, col];
      if (!col.children || col.children.length === 0) {
        this.leafPaths.push(newPath);
      } else {
        this.generateLeafPaths(col.children, newPath);
      }
    }
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
