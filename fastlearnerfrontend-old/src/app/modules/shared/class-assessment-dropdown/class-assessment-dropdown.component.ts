import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Actions } from 'src/app/core/enums/actions.enum';
import { InstructorAffiliate } from 'src/app/core/models/affiliate.model';
import { AIClass } from 'src/app/core/models/ai-class.model';
import { AIAssessment } from 'src/app/core/models/assessment.model';
import { SearchDropdownConfig } from 'src/app/core/models/search-dropdown-config.model';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';
import { SearchDropdownComponent } from 'src/app/modules/shared/search-dropdown/search-dropdown.component';

@Component({
  selector: 'app-class-assessment-dropdown',
  templateUrl: './class-assessment-dropdown.component.html',
  styleUrls: ['./class-assessment-dropdown.component.scss'],
})
export class ClassAssessmentDropdownComponent {
  @Output() selectedClassCallBackEmit = new EventEmitter<string>();
  @Output() selectedAssessmentCallBackEmit = new EventEmitter<string>();
  @Input() byStudent?: boolean;

  _httpConstants: HttpConstants = new HttpConstants();
  @ViewChild('classDropdown')
  classDropdownComponent?: SearchDropdownComponent;
  @ViewChild('assessmentDropdown')
  assessmentDropdownComponent?: SearchDropdownComponent;

  classPayload?: any = {
    pageNo: 0,
    pageSize: 10,
  };

  assessmentPayload?: any = {
    classId: null,
    search: null,
    pageNo: 0,
    pageSize: 10,
  };

  classes?: AIClass[] = [];
  assessments?: AIAssessment[] = [];
  classesTotalPages?: any;
  assessmentsTotalPages?: any;

  searchClassDropdownConfig: SearchDropdownConfig = {
    placeHolder: 'Select class',
    serverSearch: true,
    showSearch: false,
    values: [],
  };

  searchAssessmentDropdownConfig: SearchDropdownConfig = {
    placeHolder: 'Select assessment',
    values: [],
  };
  actions = Actions;
  selectedId?: any = null;
  selectedClassId?: any = null;
  searchTerm: string = '';
  private searchSubject = new Subject<string>();

  constructor(private _aiGraderService?: AiGraderService) {}

  ngOnInit(): void {
    this.searchSubject
      .pipe(
        debounceTime(300), // Wait 300ms after each keystroke
        distinctUntilChanged() // Ignore if the new term is the same as the last term
      )
      .subscribe((searchTerm: string) => {
        this.assessments = [];
        this.searchTerm = searchTerm;
        this.assessmentPayload.pageNo = 0;
        this.assessmentPayload.search = searchTerm;
        this.getAssessmentsByClass(this.selectedClassId);
      });
    this.getClasses();
  }

  reloadData(data?: any): void {
    if (data?.base == 'class') {
      this.classes = [];
      this.selectedId =
        data.action == this.actions.EDIT &&
        this.searchClassDropdownConfig?.selectedValue != null
          ? data?.id
          : null;
      this.getClasses();
      setTimeout(() => {
        this.classDropdownComponent?.reloadData();
      }, 100);
    } else if (data?.base == 'assessment') {
      this.assessments = [];
      this.selectedId =
        data.action == this.actions.EDIT &&
        this.searchAssessmentDropdownConfig?.selectedValue != null
          ? data?.id
          : null;
      this.getAssessmentsByClass(this.assessmentPayload?.classId);
      setTimeout(() => {
        this.assessmentDropdownComponent?.reloadData();
      }, 100);
    }
  }

  getClasses(append?: boolean): void {

    if(this.byStudent){
      this.getStudentClasses(append)
    }else {
      this.getInstructorClasses(append);
    }
  }

  getStudentClasses(append?: boolean){
        this._aiGraderService.getClassesStudent(this.classPayload)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.classes = append
            ? [...this.classes, ...response?.data?.aiClasses]
            : response?.data?.aiClasses ?? [];
          this.classesTotalPages = response?.data?.pages;
          this.searchClassDropdownConfig = {
            ...this.searchClassDropdownConfig,
            values: this.classes,
            selectedValue:
              this.selectedId == null
                ? null
                : this.classes.find(
                    (aClass?: any) => aClass.id == this.selectedId
                  ),
          };
          this.selectedId = null;
        }
      },
      error: (error: any) => {
        if (!append) {
          this.selectedId = null;
          this.classes = [];
          this.searchClassDropdownConfig = {
            ...this.searchClassDropdownConfig,
            values: this.classes,
            selectedValue: null,
          };
        }
      },
    });
  }

  getInstructorClasses(append?: boolean){
        this._aiGraderService.getClasses(this.classPayload)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.classes = append
            ? [...this.classes, ...response?.data?.aiClasses]
            : response?.data?.aiClasses ?? [];
          this.classesTotalPages = response?.data?.pages;
          this.searchClassDropdownConfig = {
            ...this.searchClassDropdownConfig,
            values: this.classes,
            selectedValue:
              this.selectedId == null
                ? null
                : this.classes.find(
                    (aClass?: any) => aClass.id == this.selectedId
                  ),
          };
          this.selectedId = null;
        }
      },
      error: (error: any) => {
        if (!append) {
          this.selectedId = null;
          this.classes = [];
          this.searchClassDropdownConfig = {
            ...this.searchClassDropdownConfig,
            values: this.classes,
            selectedValue: null,
          };
        }
      },
    });
  }

  getAssessmentsByClass(classId?: any, append?: boolean): void {
    this.assessmentPayload.classId = classId;

    this._aiGraderService.getAssessments(this.assessmentPayload)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.assessments = append
            ? [...this.assessments, ...response?.data?.aiAssessments]
            : response?.data?.aiAssessments ?? [];
          // this.assessments = response?.data?.aiAssessments;
          this.assessmentsTotalPages = response?.data?.pages;
          this.searchAssessmentDropdownConfig = {
            ...this.searchAssessmentDropdownConfig,
            values: this.assessments,
            selectedValue:
              this.selectedId == null
                ? null
                : this.assessments.find(
                    (assessment?: any) => assessment?.id == this.selectedId
                  ),
          };
          this.selectedId = null;
        }
      },
      error: (error: any) => {
        this.selectedId = null;
        this.assessments = [];
        this.searchAssessmentDropdownConfig = {
          ...this.searchAssessmentDropdownConfig,
          values: this.assessments,
          selectedValue: null,
        };
      },
    });
  }

  selectedClassCallBack(event?: any) {
    this.selectedClassId = event?.id;
    if (event != null) {
      this.getAssessmentsByClass(event?.id);
    } else {
      this.assessments = [];
      this.searchAssessmentDropdownConfig = {
        ...this.searchAssessmentDropdownConfig,
        values: this.assessments,
        selectedValue: null,
      };
    }
    this.selectedClassCallBackEmit.emit(event);
  }

  searchClassCallBack(event?: any) {}

  searchAssessmentCallBack(event?: any) {
    if (this.selectedClassId) {
      this.searchSubject.next(event || null);
    }
    // this.selectedAssessmentCallBackEmit.emit(event);
  }

  selectedAssessmentCallBack(event?: any) {
    this.selectedAssessmentCallBackEmit.emit(event);
  }

  loadMoreClassData() {
    if (this.classPayload?.pageNo + 1 < this.classesTotalPages) {
      this.classPayload.pageNo++;
      this.getClasses(true);
    }
  }

  loadMoreAssessmentData() {
    if (this.assessmentPayload?.pageNo + 1 < this.assessmentsTotalPages) {
      this.assessmentPayload.pageNo++;
      this.getAssessmentsByClass(this.selectedClassId, true);
    }
  }
}
