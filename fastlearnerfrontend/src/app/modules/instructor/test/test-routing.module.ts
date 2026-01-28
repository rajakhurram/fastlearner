import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TestComponent } from './test.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { CanDeactivateGuard } from 'src/app/core/guards/can-deactivate.guard';

const routes: Routes = [
  {
    path: '',
    component: TestComponent,
    canActivate: [AuthGuard],
    data: { title: 'Create Test' },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TestRoutingModule {}
