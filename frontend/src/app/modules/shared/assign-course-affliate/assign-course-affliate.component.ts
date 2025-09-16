import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { buttonConfig, HoverConfig } from 'src/app/core/models/button.model-config';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { InstructorAffiliate } from 'src/app/core/models/affiliate.model';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { TableConfig } from 'src/app/core/models/table.model-config';

@Component({
  selector: 'app-assign-course-affliate',
  templateUrl: './assign-course-affliate.component.html',
  styleUrls: ['./assign-course-affliate.component.scss'],
})
export class AssignCourseAffliateComponent implements OnInit, OnChanges {
  @Input() selectLabel = 'Select Course';
  @Input() rewardsWidth = 100;
  @Input() coursesList: any;
  @Input() affiliateList: any;
  @Input() courseId: any;
  @Input() buttonText = 'Add Course';
  @Input() showBorder = true;
  @Input() selection = false;
  @Input() selectPlaceholder = 'Select';
  @Input() affiliateSearch?: boolean = false;
  @Input() courseSearch?: boolean = false;
  @Output() addCourseAffiliateEmitter: EventEmitter<any> = new EventEmitter();
  selectedCourse?: any;
  selectedCourseReward?: any;
  selectedCourseId;
  selectedAffiliateId;
  selectedAffiliateName;
  selectedAffiliateUuid;
  rewardInvalid?: boolean = false;
  rewardValidationMsg?: string;
  private errorMessageKey: string | null = null;
  searchTerm: string = '';
  private searchSubject = new Subject<string>();
  showLoader = true;
  _httpConstants: HttpConstants = new HttpConstants();
  buttonConfig: buttonConfig = {
    iconType: 'plus',
    backgroundColor: '#ffffff',
    color: '#212189',
    borderColor: '#212189',
    border: '1px solid',
    borderRadius: '5px',
    height: '40px',
    hoverConfig: {
      color: '#fff',
      backgroundColor: '#212189'
    },
    fontWeight: 'bold',
  };
  payload = {
    pageSize: 10,
    pageNo: 0,
    search: '',
  };
  affiliateTotalPages: number;
  isDropdownOpened: boolean = false;
  isSearching: boolean = false;

  constructor(
    private cdr: ChangeDetectorRef,
    private _affiliateService: AffiliateService,
    private _messageService: NzMessageService
  ) {}

    ngOnInit(): void {
      this.searchSubject
        .pipe(
          debounceTime(300), // Wait 300ms after each keystroke
          distinctUntilChanged() // Ignore if the new term is the same as the last term
        )
        .subscribe((searchTerm: string) => {
          this.affiliateList = [];
          this.searchTerm = searchTerm;
          this.payload.pageNo = 0;
          this.payload.search = searchTerm;
          this.getAffiliateData(this.payload);
        });
    }
    
  selectCurrentCourse(event) {
    this.selection = true;
    this.selectedCourseId = event?.id;
    this.selectedCourseReward = event?.defaultReward ?? event?.reward;
    this.selectedAffiliateId = event?.affiliateId;
    this.selectedAffiliateName = event?.affiliateName;
    this.selectedAffiliateUuid = event?.uuid;
    this.rewardMinMaxValidation(Number(this.selectedCourseReward));
  }

  setReward(value) {
    this.selection = true;
    this.selectedCourseReward = value;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.coursesList = changes['coursesList']?.currentValue;
  }

  onAddCourseEmit() {
    if (this.affiliateList?.length > 0) {
      this.addCourseAffiliateEmitter.emit({
        courseId: this.courseId,
        reward: this.selectedCourseReward,
        affiliateId: this.selectedAffiliateId,
        affiliateName: this.selectedAffiliateName,
        url: this.selectedAffiliateUuid,
      });
    } else {
      this.addCourseAffiliateEmitter.emit({
        courseId: this.selectedCourseId,
        reward: this.selectedCourseReward,
      });
    }

    this.selection = false;
  }

  rewardValidation(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    let value = inputElement.value;
  
    const cursorPosition = inputElement.selectionStart;

    const validDecimalRegex = /^\d*\.?\d{0,2}$/;
  
    if (!validDecimalRegex.test(value)) {
      const match = value.match(/^\d*\.?\d{0,2}/);
      value = match ? match[0] : '';
    }
  
    if (value === '.') {
      value = '';
    }
  
    inputElement.value = value;
  
    const numericValue = value === '' ? null : parseFloat(value);
  
    this.selectedCourseReward = numericValue;

    inputElement.value = value;
  
    if (inputElement === document.activeElement) {
      inputElement.setSelectionRange(cursorPosition, cursorPosition);
    }
  
    if (numericValue !== null) {
      this.rewardMinMaxValidation(numericValue);
    } else {
      // Reset error state if input is invalid (e.g., only a ".")
      this.rewardInvalid = false;
      this.rewardValidationMsg = null;
      this._messageService.remove(this.errorMessageKey);
    }
  
    this.cdr.detectChanges();
  }
  
  rewardMinMaxValidation(value: number): void {
    if (value > 90) {
      this.rewardInvalid = true;
      this.rewardValidationMsg = 'Reward cannot be greater than 90%.';
      this.showErrorMessage(this.rewardValidationMsg);
    } else if (value < 1) {
      this.rewardInvalid = true;
      this.rewardValidationMsg = 'Reward must be at least 1%.';
      this.showErrorMessage(this.rewardValidationMsg);
    } else {
      this.rewardInvalid = false;
      this.rewardValidationMsg = null;
      this._messageService.remove(this.errorMessageKey);
    }
  
    this.cdr.detectChanges();
  }

  showErrorMessage(message?: string){
    if (this.errorMessageKey) {
      this._messageService.remove(this.errorMessageKey);
    }
    this.errorMessageKey = this._messageService.error(message, {
      nzDuration: 3000,
    }).messageId;
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
          pages: number
        };
      }) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.showLoader = false;    
          response?.data?.affiliates?.forEach(el => {
            this.affiliateList.push(el);
          });
          this.affiliateTotalPages = response?.data?.pages;

          if(this.payload.pageNo == 0){
            this.loadMoreAffiliate()
          }
        }
      },
      error: () => {
        this.showLoader = false;
        this.affiliateList = []; 
      },
    });
  }
  
  // onSearch(value: string): void {
  //   // Trigger search only when dropdown is open and user is typing
  //   if (value) {
  //     this.isSearching = true; // Mark as searching
  //     this.searchCallBack(value);
  //   }
  // }
  
  searchCallBack(value?: string): void {   
    this.searchSubject.next(value || '');
  }

  loadMoreAffiliate(){
    if(this.affiliateTotalPages!=this.payload.pageNo+1){
      this.payload.pageNo+=1;
      this.getAffiliateData(this.payload);
    }
  }

  affiliateFocus(){
    this.loadMoreAffiliate();
  }

  selectOpen(event?: any){
    if(event){
      this.selectPlaceholder = 'Start typing'
    }else{
      this.selectPlaceholder = 'Search a Affiliate'
    }
  }

}
