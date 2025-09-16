import { Component, EventEmitter, Input, Output } from '@angular/core';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { SearchFilterConfig } from 'src/app/core/models/search-filter-config.model';

@Component({
  selector: 'app-search-filter',
  templateUrl: './search-filter.component.html',
  styleUrls: ['./search-filter.component.scss'],
})
export class SearchFilterComponent {
  @Input() config: SearchFilterConfig = {
    placeHolder: 'Search',
    height: '40px',
    width: '100%',
  };

  input?: string = '';

  @Output() searchCallBack = new EventEmitter<string>();
  @Output() clearCallBack = new EventEmitter<any>();
  private searchSubject = new Subject<string>();

  ngOnInit() {
    this.searchSubject
      .pipe(
        debounceTime(300), // Wait 300ms after each keystroke
        distinctUntilChanged() // Ignore if the new term is the same as the last term
      )
      .subscribe((searchTerm: string) => {
        this.searchCallBack.emit(searchTerm);
      });
  }

  callBackFn() {
    if (this.input) {
      this.searchSubject.next(this.input);
    }
  }

  clearInput() {
    this.input = '';
    this.clearCallBack.emit();
  }
}
