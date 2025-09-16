import { Component, EventEmitter, Input, Output } from '@angular/core';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import {
  SearchDropdownConfig,
  StyleConfig,
} from 'src/app/core/models/search-dropdown-config.model';

@Component({
  selector: 'app-search-dropdown',
  templateUrl: './search-dropdown.component.html',
  styleUrls: ['./search-dropdown.component.scss'],
})
export class SearchDropdownComponent {
  @Output() searchCallBack = new EventEmitter<string>();
  @Output() selectedCallBack = new EventEmitter<string>();
  @Output() loadMore = new EventEmitter<void>();
  @Input() config: SearchDropdownConfig = {};
  private searchSubject = new Subject<string>();

  reloadData(): void {
    // this.config.selectedValue = null;
    this.ngOnInit();
  }

  ngOnInit(): void {
    this.config.placeHolder = this.config.placeHolder ?? 'Search';
    this.config.selectedValue = this.config.selectedValue ?? null;
    this.config.showSearch = this.config.showSearch ?? true;
    this.config.allowClear = this.config.allowClear ?? true;
    this.config.serverSearch = this.config.serverSearch ?? true;
    this.config.values = this.config.values ?? [];

    this.config.style = {
      height: this.config.style?.height ?? '40px',
      width: this.config.style?.width ?? '100%',
    };

    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe((searchTerm: string) => {
        this.searchCallBack.emit(searchTerm);
      });
  }

  searchCallBackFn(event?: any) {
    this.searchSubject.next(event);
  }

  loadMoreData() {}

  selectedCallBackFn(event?: any) {
    this.selectedCallBack.emit(event);
  }

  onScrollToBottom(): void {
    this.loadMore.emit();
  }
}
