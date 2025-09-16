import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';
import { InstructorTabs } from 'src/app/core/enums/instructor_tabs';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { CourseService } from 'src/app/core/services/course.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { MessageService } from 'src/app/core/services/message.service';
import { environment } from 'src/environments/environment.development';

@Component({
  selector: 'app-course',
  templateUrl: './course.component.html',
  styleUrls: ['./course.component.scss'],
})
export class CourseComponent implements OnInit {
  stepOne?: boolean = true;
  stepTwo?: boolean = false;
  stepThree?: boolean = false;
  currentStep?: any = 0;  // this needs to be 0
  sectionsData?: any = [];
  courseInformationData: FormGroup;
  courseId?: any;
  courseContentType = CourseContentType
  selectedContentType?: CourseContentType;

  constructor(
    private _route: ActivatedRoute,
    private _communicationService: CommunicationService,
    private _router: Router
  ) {
    this._communicationService.instructorTabChange(InstructorTabs.COURSE);
  }

  ngOnInit(): void {
    this._route.queryParams.subscribe((params) => {
      this.courseId = params['id'];
    });

    const currentRoute = this._router.url.split('/')[2];
    if(currentRoute.includes(this.courseContentType.COURSE)){
      this.selectedContentType = this.courseContentType.COURSE;
    }else if(currentRoute.includes(this.courseContentType.TEST)){
      this.selectedContentType = this.courseContentType.TEST;
    }

  }

  step(event?: any) {
    this.currentStep = event;
    window.scrollTo({ top: 0, behavior: 'smooth' }); // scrolls smoothly to top
  }
  sectionData(event?: any) {
    this.sectionsData = event;
  }

  courseInformation(event?: any) {
    this.courseInformationData = event;
  }

  getDraftCourseId(event?: any) {
    this.courseId = event.toString();
  }
}
