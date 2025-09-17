import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AffiliateComponent } from './affiliate.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { PremiumCoursesComponent } from './premium-courses/premium-courses.component';
import { AffliateDetailsComponent } from './affliate-details/affliate-details.component';

const routes: Routes = [
  {
    path: 'profiles',
    component: AffiliateComponent,
    canActivate: [AuthGuard],
    data: { title: 'Affiliate Profiles' },
  },
  {
    path: 'premium-courses',
    component: PremiumCoursesComponent,
    canActivate: [AuthGuard],
    data: { title: 'Premium Courses' },
  },
  {
    path: 'affiliate-details/:affiliateId',
    component: AffliateDetailsComponent,
    canActivate: [AuthGuard],
    data: { title: 'Affliate Details' },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AffiliateRoutingModule {}
