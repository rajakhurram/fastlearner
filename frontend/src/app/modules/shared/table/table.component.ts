import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { TableConfig } from 'src/app/core/models/table.model-config';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss'],
})
export class TableComponent implements OnChanges {
  @Input() tableConfig: TableConfig = {
    columns: [],
    rowData: [],
    headerColor: '#f5f5f5',
    rowColor: '#ffffff',
    rowTextColor: '#212189',
    paginated: false,
    pageNo: 1,
    pageSize: 10,
    totalElements: 0,
    itemsPerPage: false,
  };

  @Output() action = new EventEmitter<{ event; action: string; row: any }>();
  @Output() rowClickEmitter = new EventEmitter<{ event; row: any }>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() routeAction = new EventEmitter<any>();


  ngOnInit(){
  }

  onAction(event, action: string, row: any) {
    this.action.emit({ event, action, row });
  }
  onRowClickAction(event, row: any) {
    this.rowClickEmitter.emit({ event, row });
  }
  ngOnChanges(changes: SimpleChanges): void {}

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
    return visibleButtons > 1;
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
