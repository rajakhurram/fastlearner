import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { TableConfig } from 'src/app/core/models/table.model-config';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss'],
})
export class TableComponent implements OnChanges {
  @ViewChildren('valueInput') valueInputs!: QueryList<ElementRef>;
  @Input() tableConfig: TableConfig = {
    columns: [],
    rowData: [],
    clickableKeys: [],
    headerColor: '#f5f5f5',
    rowColor: '#ffffff',
    rowTextColor: '#212189',
    paginated: false,
    pageNo: 1,
    pageSize: 10,
    totalElements: 0,
    itemsPerPage: false,
  };

  @Output() action = new EventEmitter<{
    event;
    action: string;
    row: any;
    index?: any;
  }>();
  @Output() rowClickEmitter = new EventEmitter<{ event; row: any }>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() routeAction = new EventEmitter<any>();
  @Output() editValueAction = new EventEmitter<{ value?: any; field?: any }>();

  ngOnInit() {
    console.log(this.tableConfig);
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log(this.tableConfig);
  }

  onAction(event, action: string, row: any, index?: any) {
    this.action.emit({ event, action, row, index });
  }
  onRowClickAction(event, row: any) {
    this.rowClickEmitter.emit({ event, row });
  }

  routeToAffiliateDetail(event, row) {
    this.routeAction.emit({ event, row });
  }
  onPageChange(page: number) {
    this.pageChange.emit(page);
  }

  isMoreThanOneButtonVisible(row: any): boolean {
    let visibleButtons = 0;

    if (row.showEditIcon) visibleButtons++;
    if (row.showCopyIcon) visibleButtons++;
    if (row.showDeleteIcon) visibleButtons++;
    if (row.showViewIcon) visibleButtons++;
    return visibleButtons > 1;
  }

  editValue(value?: any, field?: any) {
    this.editValueAction.emit({ value, field });
  }

  // cancelEdit(value?: any, field?: any) {
  //   this.editValueAction.emit({ value, field });
  // }

  public focusFirstEditableInput(rowId?: number): void {
    // console.log(this.tableConfig);
    // setTimeout(() => {
    //   const inputs = this.valueInputs.toArray();
    //   const targetInput = rowId
    //     ? inputs.find(
    //         (inputRef) => inputRef.nativeElement.dataset.rowId == rowId
    //       )
    //     : inputs[0]; // fallback to first
    //   if (targetInput) {
    //     targetInput.nativeElement.focus();
    //     targetInput.nativeElement.scrollIntoView({
    //       block: 'nearest',
    //       behavior: 'smooth',
    //     }); // 👈 optional: avoid scroll jump
    //   }
    // }, 150);
  }

  // onSwitchChange(isChecked: boolean, row: any): void {
  //   console.log(`Switch for row ID ${row.id} is now ${isChecked ? 'ON' : 'OFF'}`);

  //   // Perform specific actions based on the switch state
  //   if (isChecked) {
  //     // Action when switch is ON
  //   } else {
  //     // Action when switch is OFF
  //   }
  // }
}
