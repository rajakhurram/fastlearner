import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  Renderer2,
  ViewContainerRef,
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { buttonConfig } from 'src/app/core/models/button.model-config';
import { TableConfig } from 'src/app/core/models/table.model-config';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionPlanComponent } from '../../auth/subscription-plan/subscription-plan.component';

@Component({
  selector: 'app-assign-course-modal',
  templateUrl: './assign-course-modal.component.html',
  styleUrls: ['./assign-course-modal.component.scss'],
})
export class AssignCourseModalComponent {
  addedAffliate = false;
  _httpConstants: HttpConstants = new HttpConstants();
  @Input() affiliates = [];
  @Input() courseId: any;
  @Input() courseUrl!: string;
  affiliateData = [];
  selection = false;
  affiliateLimitExceed: boolean = false;
  affiliateLimitExceedMessage: SafeHtml;
  private isUpgradeClickEventAttached = false;

  constructor(
    private _modal: NzModalService,
    private modalRef: NzModalRef,
    private _messageService: MessageService,
    private _affiliateService: AffiliateService,
    private sanitizer: DomSanitizer,
    private renderer: Renderer2,
    private el: ElementRef,
    private _viewContainerRef: ViewContainerRef
  ) {}
  buttonCancel: buttonConfig = {
    backgroundColor: '#fff',
    color: '#fe4a55',
    text: 'Cancel',
    borderColor: '#fe4a55',
    paddingRight: '50px',
    paddingLeft: '50px',
    border: '1px solid',
  };

  buttonAdd: buttonConfig = {
    backgroundColor: '#fe4a55',
    color: '#fff',
    text: 'Save',
    borderColor: '#fe4a55',
    paddingRight: '50px',
    paddingLeft: '50px',
    border: '1px solid',
  };

  tableConfig: TableConfig = {
    columns: [
      {
        header: 'Affiliate Name',
        field: 'affiliateName',
        backgroundColor: '#F9FAFB',
        color: '#292929',
      },
      {
        header: 'Generated Link',
        field: `url`,
        backgroundColor: '#F9FAFB',
        color: '#292929',
      },
      {
        header: 'Rewards(%)',
        field: 'reward',
        backgroundColor: '#F9FAFB',
        color: '#292929',
      },
      {
        header: 'Actions',
        field: 'actions',
        actions: true,
        backgroundColor: '#F9FAFB',
        color: '#292929',
      },
    ],
    rowData: [],
    headerColor: '#F9FAFB',
    rowColor: '#F9FAFB',
    rowTextColor: '#212189',
    paginated: false,
    pageNo: 1,
    pageSize: 10,
    totalElements: 2,
    itemsPerPage: true,
  };

  ngAfterViewChecked(): void {
    if (!this.isUpgradeClickEventAttached) {
      this.attachUpgradeClickEvent();
    }
  }

  attachUpgradeClickEvent(): void {
    const upgradeLink = this.el.nativeElement.querySelector('.upgrade-link');
    if (upgradeLink) {
      this.renderer.listen(upgradeLink, 'click', () => {
        this.openSubscriptionPlan();
      });
      this.isUpgradeClickEventAttached = true; // Avoid duplicate listeners
    }
  }

  cancel() {
    this.closeModal();
  }

  onAffiliateAdd(addedAffiliates) {
    if (
      !addedAffiliates?.affiliateName ||
      this.affiliateData.find(
        (affiliate?: any) =>
          affiliate?.affiliateId == addedAffiliates?.affiliateId
      )
    ) {
      return;
    }
    addedAffiliates = {
      ...addedAffiliates,
      showDeleteIcon: true,
      url: `${this.courseUrl + addedAffiliates?.url}`,
    };
    this.affiliateData.push(addedAffiliates);
    this.tableConfig = {
      ...this.tableConfig,
      rowData: this.affiliateData,
    };
  }

  assignAffiliates() {
    this.addedAffliate = true;
    let payload = [];
    this.tableConfig.rowData.map((element) => {
      payload.push({
        courseId: element?.courseId,
        affiliateId: element?.affiliateId,
        reward: element?.reward,
      });
    });
    this._affiliateService.assignAffiliateCourse(payload).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.affiliateLimitExceed = false;
          this.affiliateLimitExceedMessage = '';
          this._messageService.success('Affiliate assigned successfully');
          this.closeModal();
        }
      },
      error: (error) => {
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.CONFLICT_409.CODE
        ) {
          this.affiliateLimitExceed = true;
          this.affiliateLimitExceedMessage =
            this.sanitizer.bypassSecurityTrustHtml(error?.error?.message);
        } else {
          this._messageService.error(error?.error?.message);
        }
      },
    });
  }

  handleTableAction(event: { event; action: string; row: any }) {
    if (event.action === 'edit') {
      return;
    } else if (event.action === 'delete') {
      this.deleteElement(event?.row);
      event.event.stopPropagation();
      return;
    }
  }

  deleteElement(row) {
    let filteredData = this.tableConfig.rowData.filter((element, index) => {
      return row.affiliateId != element.affiliateId;
    });
    this.tableConfig.rowData = filteredData;
    this.affiliateData = filteredData;
    this.selection = true;
  }

  closeModal(params?) {
    this.modalRef.close(params);
  }

  openSubscriptionPlan(): void {
    const modal = this._modal.create({
      nzContent: SubscriptionPlanComponent,
      nzComponentParams: {
        fromSubscriptionPlan: true,
        showFreePlan: false,
      },
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '80%' : '100%',
      nzWidth: '80%',
    });
    modal.afterClose?.subscribe((result) => {
      // this.subscriptionModalOpened = false;
    });
  }
}
