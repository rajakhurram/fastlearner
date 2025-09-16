import { Pagination } from "./pagination.model";

export class Filter extends Pagination {
    categoriesId: any[] = [];
    courseType: string = '';
    contentType: string = '';
    feature: string = '';
    rating: number = 0;
    search: string= '';
  
    constructor() {
      super();
    }
  }