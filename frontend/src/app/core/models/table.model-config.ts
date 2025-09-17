export class ColumnConfig {
  header: string;
  field: string;
  width?: string;
  tooltip?: boolean;
  actions?: boolean;
  backgroundColor?: string;
  color?: string;
}

export class TableConfig {
  columns: ColumnConfig[];
  rowData: any[];
  headerColor?: string;
  rowColor?: string;
  rowTextColor?: string;
  paginated?: boolean;
  pageNo?: number;
  pageSize?: number;
  totalElements?: number;
  itemsPerPage?: boolean;
}
