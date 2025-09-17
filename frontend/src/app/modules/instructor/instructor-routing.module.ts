import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InstructorComponent } from './instructor.component';
import { InstructorProfileComponent } from './instructor-profile/instructor-profile.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { PermissionGuard } from 'src/app/core/guards/permission.guard';

const routes: Routes = [
  {
    path: '',
    component: InstructorComponent,
    children: [
      { path: '', redirectTo: 'instructor-dashboard', pathMatch: 'full' },
      {
        path: 'instructor-dashboard',
        loadChildren: () =>
          import('./dashboard/dashboard.module').then((m) => m.DashboardModule),
      },
      {
        path: 'course',
        loadChildren: () =>
          import('./course/course.module').then((m) => m.CourseModule),
      },
      {
        path: 'test',
        loadChildren: () =>
          // import('./test/test.module').then((m) => m.TestModule),
        import('./course/course.module').then((m) => m.CourseModule),
      },
      {
        path: 'performance',
        loadChildren: () =>
          import('./performance/performance.module').then(
            (m) => m.PerformanceModule
          ),
      },
      {
        path: 'notifications',
        loadChildren: () =>
          import('./notifications/notifications.module').then(
            (m) => m.NotificationsModule
          ),
      },
      {
        path: 'payment',
        loadChildren: () =>
          import('./payment/payment.module').then((m) => m.PaymentModule),
      },
      {
        path: 'profile',
        component: InstructorProfileComponent,
        canActivate: [AuthGuard],
        data: { title: 'Instructor Profile' },
      },
      {
        path: 'affiliate',
        loadChildren: () =>
          import('./affiliate/affiliate.module').then((m) => m.AffiliateModule),
        canLoad: [PermissionGuard],
      },
      {
        path: 'premium-student',
        loadChildren: () =>
          import('./premium-student/premium-student.module').then(
            (m) => m.Premium_StudentModule
          ),
        canLoad: [PermissionGuard],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class InstructorRoutingModule {}
