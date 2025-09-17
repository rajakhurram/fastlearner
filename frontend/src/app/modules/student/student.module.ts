import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StudentRoutingModule } from './student-routing.module';
import { StudentComponent } from './student.component';
import { StudentLandingPageComponent } from './student-landing-page/student-landing-page.component';
import { SharedModule } from '../shared/shared.module';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { CourseDetailsComponent } from './course-details/course-details.component';
import { MsIntoHHMMSSPipe } from 'src/app/core/pipes/ms-into-hhmmss.pipe';
import { MyCoursesComponent } from './my-courses/my-courses.component';
import { FavoriteCoursesComponent } from './favorite-courses/favorite-courses.component';
import { CourseContentComponent } from './course-content/course-content.component';
import { CourseListComponent } from './course-list/course-list.component';
import { CertificateComponent } from './certificate/certificate.component';
import { QuizPlayerComponent } from './quiz-player/quiz-player.component';
import { FilterCoursesComponent } from './filter-courses/filter-courses.component';
import { ArticleComponent } from './article/article.component';
import { NotificationsModule } from '../instructor/notifications/notifications.module';
import { VerifyCertificateComponent } from './verify-certificate/verify-certificate.component';
import { QuizReviewQuestionsComponent } from './quiz-review-questions/quiz-review-questions.component';
// import { LazyLoadDirective } from '../directives/lazy-load.directive';

@NgModule({
  declarations: [
    StudentComponent,
    StudentLandingPageComponent,
    CourseDetailsComponent,
    MsIntoHHMMSSPipe,
    MyCoursesComponent,
    FavoriteCoursesComponent,
    CourseContentComponent,
    CourseListComponent,
    CertificateComponent,
    QuizPlayerComponent,
    FilterCoursesComponent,
    ArticleComponent,
    VerifyCertificateComponent,
    QuizReviewQuestionsComponent,
    // LazyLoadDirective
  ],
  imports: [
    CommonModule,
    StudentRoutingModule,
    SharedModule,
    AntDesignModule,
    NotificationsModule
  ],
  // exports: [LazyLoadDirective],
})
export class StudentModule { }
