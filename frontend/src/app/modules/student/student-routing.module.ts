import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StudentComponent } from './student.component';
import { StudentLandingPageComponent } from './student-landing-page/student-landing-page.component';
import { CourseDetailsComponent } from './course-details/course-details.component';
import { FavoriteCoursesComponent } from './favorite-courses/favorite-courses.component';
import { MyCoursesComponent } from './my-courses/my-courses.component';
import { CourseContentComponent } from './course-content/course-content.component';
import { CourseListComponent } from './course-list/course-list.component';
import { CertificateComponent } from './certificate/certificate.component';
import { FilterCoursesComponent } from './filter-courses/filter-courses.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { VerifyCertificateComponent } from './verify-certificate/verify-certificate.component';

const routes: Routes = [
  { path: '', component: StudentComponent,
    children: [
      { path: '', redirectTo : 'dashboard', pathMatch :'full', data: { title: 'Dashboard' }},
      { path: 'dashboard', component: StudentLandingPageComponent, data: { title: 'Dashboard' }},
      { path: 'course-details/:courseUrl', component: CourseDetailsComponent, data: { title: 'Course Details' }},
      { path: 'course-content/:courseUrl', component: CourseContentComponent, canActivate: [AuthGuard], data: { title: 'Course Content' }},
      { path: 'my-courses', component: MyCoursesComponent, data: { title: 'My Courses' }},
      { path: 'favorite-courses', component: FavoriteCoursesComponent, data: { title: 'Favourite Courses' }},
      { path: 'courses', component : CourseListComponent, data: { title: 'Courses' }},
      { path: 'generate-certificate', component : CertificateComponent, data: { title: 'Certificate' }},
      { path: 'filter-courses', component : FilterCoursesComponent, data: { title: 'Filter Courses' }},
      { path: 'verify-certificate/:uuid', component: VerifyCertificateComponent },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StudentRoutingModule { }
