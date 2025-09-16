import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CourseComponent } from './course.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { CanDeactivateGuard } from 'src/app/core/guards/can-deactivate.guard';

const routes: Routes = [{ path: '', component: CourseComponent, canActivate: [AuthGuard], data: { title: 'Create Course' }}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CourseRoutingModule { }
