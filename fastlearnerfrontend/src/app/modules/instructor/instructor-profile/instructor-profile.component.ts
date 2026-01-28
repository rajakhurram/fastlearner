import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';

@Component({
  selector: 'app-instructor-profile',
  templateUrl: './instructor-profile.component.html',
  styleUrls: ['./instructor-profile.component.scss']
})

export class InstructorProfileComponent {

  _httpConstants : HttpConstants = new HttpConstants();
  showDescription : boolean = false;
  courseList : Array<any> = [];
  instructorId : any = null;
  instructorPublicProfile : any;
  numberOfCourses : number = 0;
  noOfCount : any;
  coursePayLoad = {
    pageNo : 0,
    pageSize : 10
  }
  constructor(
    private _courseService : CourseService,
    private _messageService : MessageService,
    private _router : Router,
    private _activatedRoute : ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.getInstructorPublicProfile()
  }

  getInstructorPublicProfile(){
    this._courseService.getInstructorProfile()?.subscribe({
      next: (response: any) => {
        if(response?.status == this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE){
          this.instructorPublicProfile = response?.data;
          this.getInstructorCourseList(true);
        }
      },
      error: (error: any) => {
      }
    })
  }

  routeTo(route? : string){
    if(route) window.open(route,'_blank');
    else {
      this._messageService.info('No link found')
    }
  }


  getInstructorCourseList(isFirst: boolean){
    this._courseService.getInstructorCourses(this.coursePayLoad)?.subscribe({
      next: (response: any) => {
        if(response?.status == this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE){
          if(!isFirst){
            response?.data?.data?.forEach((x: any) => {
              x.courseDuration = this.convertSecondsToHoursAndMinutes(x.courseDuration);
              this.courseList.push(x)
            })
          }
          if(isFirst){
            this.courseList = response?.data?.data;
            this.noOfCount = response?.data?.pages;
            this.courseList?.forEach((res: any) => {
              res.courseDuration = this.convertSecondsToHoursAndMinutes(res.courseDuration)
            });
          }
        }
        else if(response?.status == this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE){
          this.courseList = [];
        }
      },
      error: (error: any) => {
        if(error?.error?.status == this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE){
          this.courseList = [];
        }
      }
    })  
  }

  showMore(){
    this.coursePayLoad.pageNo = this.coursePayLoad.pageNo + 1;
    this.noOfCount = this.noOfCount - 1;
    this.getInstructorCourseList(false);
  }

  routeToCourseDetails(courseUrl: any){
    this._router.navigate(['student/course-details', courseUrl]);
  }

  routeToCourseDetailsContent(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl], {
      fragment: 'course-content',
    });
  }

  convertSecondsToHoursAndMinutes(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
  
    if (hours === 0) {
      return `${minutes} minutes`;
    } else if (minutes === 0) {
      return `${hours} hours`;
    } else {
      return `${hours} hours ${minutes} minutes`;
    }
  }

}
