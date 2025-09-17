import { Component, EventEmitter, Input, Output } from '@angular/core';
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

  ngOnInit() {}

  callBackFn() {
    this.searchCallBack.emit(this.input);
  }

  clearInput(){
    this.input = '';
    this.clearCallBack.emit();
  }
}
