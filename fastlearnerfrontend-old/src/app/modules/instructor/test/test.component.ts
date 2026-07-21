import { Component } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { InstructorTabs } from 'src/app/core/enums/instructor_tabs';

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.scss'],
})
export class TestComponent {
  stepOne?: boolean = true;
  stepTwo?: boolean = false;
  stepThree?: boolean = false;
  currentStep?: any = 0;
  sectionsData?: any = [];
  courseInformationData: FormGroup;
  courseId?: any;

  constructor(
    private _route: ActivatedRoute,
    private _communicationService: CommunicationService
  ) {
    this._communicationService.instructorTabChange(InstructorTabs.COURSE);
  }

  ngOnInit(): void {
    this._route.queryParams.subscribe((params) => {
      this.courseId = params['id'];
    });
  }

  step(event?: any) {
    this.currentStep = event;
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
