import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PerformanceComponent } from './performance.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';

const routes: Routes = [{ path: '', component: PerformanceComponent, canActivate: [AuthGuard],  data: { title: 'Performance' }}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PerformanceRoutingModule { }
