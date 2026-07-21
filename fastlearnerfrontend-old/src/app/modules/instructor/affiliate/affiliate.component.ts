import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { SearchFilterConfig } from 'src/app/core/models/search-filter-config.model';
import { TableConfig } from 'src/app/core/models/table.model-config';
import { AffiliateModalComponent } from '../../dynamic-modals/affiliate-modal/affiliate-modal.component';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import {
  debounceTime,
  distinctUntilChanged,
  elementAt,
  Observable,
  Subject,
  switchMap,
} from 'rxjs';
import { error } from '@ant-design/icons-angular';
import { AffiliateMode } from 'src/app/core/enums/affiliate.enum';
import { DeletionModalComponent } from '../../dynamic-modals/deletion-modal/deletion-modal.component';
import { MessageService } from 'src/app/core/services/message.service';
import { InstructorAffiliate } from 'src/app/core/models/affiliate.model';
import { CacheService } from 'src/app/core/services/cache.service';

@Component({
  selector: 'app-affiliate',
  templateUrl: './affiliate.component.html',
  styleUrls: ['./affiliate.component.scss'],
})
export class AffiliateComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  constructor(
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _router: Router,
    private _affiliateService: AffiliateService,
    private _messageService: MessageService,
    private _cacheService: CacheService
  ) {}
  affiliates = [];
  private searchSubject = new Subject<string>();
  searchTerm: string;
  showLoader = true;
  affiliateMode = AffiliateMode;
  payload = {
    pageSize: 10,
    pageNo: 0,
    search: '',
  };
  rowCount?: any = 1;
  tableConfig: TableConfig = {
    columns: [
      {
        header: 'S.NO',
        field: 'id',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Name',
        field: 'name',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Nickname',
        field: 'nickName',
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Email',
        field: 'email',
        tooltip: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Rewards',
        field: 'defaultReward',
        tooltip: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
      {
        header: 'Actions',
        field: 'actions',
        actions: true,
        backgroundColor: '#edf6ff',
        color: '#212189',
      },
    ],
    rowData: [] as InstructorAffiliate[],
    headerColor: '#f5f5f5',
    rowColor: '#ffffff',
    paginated: true,
    pageNo: 0,
    pageSize: 10,
    totalElements: 10,
    itemsPerPage: false,
  };

  searchFilter: SearchFilterConfig = {
    placeHolder: 'Search by Affiliate',
    height: '45px',
  };

  buttonConfig: buttonConfig = {
    iconType: 'plus-circle',
  };

  ngOnInit(): void {
    this.getAffiliateData(this.payload);
    this.initializeSearch();
  }

  getAffiliateData(payload: any): void {
    this._affiliateService.getAffiliates(payload)?.subscribe({
      next: (response: {
        status: number;
        data: {
          affiliates: InstructorAffiliate[];
          pageNo: number;
          pageSize: number;
          totalElements: number;
        };
      }) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          const targetDiv = document.getElementById(`top`);
          if (targetDiv) {
            targetDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }

          this.showLoader = false;
          const affiliates: InstructorAffiliate[] = response?.data?.affiliates;
          payload.pageNo == 0 ? this.rowCount = 0 : this.rowCount = payload.pageNo * payload.pageSize
          affiliates?.forEach((element) => {
            element['showEditIcon'] = true;
            element['showDeleteIcon'] = true;
            element['serialNo'] = ++this.rowCount;
          });

          this.tableConfig = {
            ...this.tableConfig,
            rowData: affiliates, // Assigning typed data
            pageNo: response?.data?.pageNo,
            pageSize: 10,
            totalElements: response?.data?.totalElements,
          };
        }
      },
      error: (error: any) => {
        this.tableConfig = {
          ...this.tableConfig,
          rowData: [],
          pageNo: 0,
          pageSize: 10,
          totalElements: 0,
        };
        this.showLoader = false;
        this.rowCount = 1
      },
    });
  }

  handleTableAction(event: { event; action: string; row: any }) {
    if (event.action === 'edit') {
      this.addAffiliate(AffiliateMode.EDIT, event.row);
      event.event.stopPropagation();
      return;
    } else if (event.action === 'delete') {
      this.openDeleteModal(event?.row);
      event.event.stopPropagation();
      return;
    }
  }

  deleteAffiliate(affiliate) {
    this._affiliateService.deleteAffiliate(affiliate)?.subscribe({
      next: (response) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          // this.getAffiliateData(this.payload);
          let filteredData = this.tableConfig.rowData.filter(
            (element, index) => {
              return (
                affiliate.instructorAffiliateId != element.instructorAffiliateId
              );
            }
          );

          this.tableConfig.rowData = filteredData;
          this._messageService.success('Affiliate deleted successfully');
        }
      },
      error: (error: any) => {},
    });
  }

  handlePageChange(page: number) {
    this.payload = {
      pageNo: page == 0 ? page : page - 1,
      pageSize: 10,
      search: '',
    };

    this.getAffiliateData(this.payload);

    // Load new page data if necessary
  }

  onRowClick(event) {
    event.event.stopPropagation();
    this._cacheService.saveInCache('affiliatePreviousRoute', 'instructor/affiliate/profiles')
    this._router.navigate([
      'instructor/affiliate/affiliate-details',
      event.row.instructorAffiliateId,
    ]);
  }

  initializeSearch() {
    this.searchSubject
      .pipe(
        debounceTime(300), // Wait 300ms after each keystroke
        distinctUntilChanged(), // Ignore if the next search term is the same as the previous
        switchMap((searchTerm) => {
          const updatedPayload = {
            ...this.payload,
            search: searchTerm, // Add search term to the payload
          };
          return new Observable((observer) => {
            this.getAffiliateData(updatedPayload);
            observer.complete(); // Complete the observable after the function call
          });
        })
      )
      .subscribe();
  }

  searchCallBack(value?: any) {
      this.rowCount = 1;
      this.searchTerm = value;
      if (value && value.length >= 1) {
        this.payload.search = this.searchTerm;
        this.payload.pageNo = 0;
        this.getAffiliateData(this.payload)
      } 
      // else if (value.length === 0) {
      //   const updatedPayload = {
      //     ...this.payload,
      //     search: '', // Add search term to the payload
      //   };
      //   this.getAffiliateData(updatedPayload);
      // }
  }

  addAffiliate(mode, data?) {
    const modal = this._modal.create({
      nzContent: AffiliateModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false, // Disables closing with Enter or Escape
      nzWidth: '40%',
      nzComponentParams: {
        data: data,
        mode: mode,
      },
      nzMask: true, // Ensures the modal has a backdrop
      nzClosable: true, // ✅ Ensure cross icon can close the modal
      nzMaskClosable: true,
    });

    modal?.afterClose?.subscribe((result) => {
      this.getAffiliateData({ pageNo: 0, pageSize: 10 });
    });
  }
  openDeleteModal(data?) {
    const modal = this._modal.create({
      nzContent: DeletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: false,
      nzWidth: '40%',
      nzComponentParams: {
        msg: 'Are you sure you want to delete the selected Affiliate',
        secondBtnText: 'Delete',
      },
    });

    modal?.afterClose?.subscribe((result) => {
      if (result){
        const affiliates = this.tableConfig.rowData;
        let filteredData = affiliates.filter(
          (element, index) => {
            return (
              data.instructorAffiliateId != element.instructorAffiliateId
            );
          }
        );
        this.deleteAffiliate(data);
          setTimeout(() => {
            this.handlePageChange(filteredData.length == 0 ? this.payload.pageNo : this.payload.pageNo + 1);
          }, 300)
      }
    });
  }

  onSearchClear(){
    if(this.payload.search){
      this.payload.pageNo = 0;
      this.payload.search = '';
      this.getAffiliateData(this.payload);
    }
  }

}
