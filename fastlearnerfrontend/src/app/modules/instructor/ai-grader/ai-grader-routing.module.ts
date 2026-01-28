import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AiGraderComponent } from './ai-grader.component';
import { GraderClassesComponent } from './grader-classes/grader-classes.component';
import { GraderAssessmentComponent } from './grader-assessment/grader-assessment.component';
import { GraderResultsComponent } from './grader-results/grader-results.component';
import { GraderUploaderComponent } from './grader-uploader/grader-uploader.component';
import { GraderResultViewComponent } from './grader-result-view/grader-result-view.component';

const routes: Routes = [
  {
    path: '',
    component: AiGraderComponent,
  },
  {
    path: 'results',
    component: GraderResultsComponent,
  },
  {
    path: 'uploader',
    component: GraderUploaderComponent,
  },
  {
    path: 'classes',
    component: GraderClassesComponent,
  },
  {
    path: 'assessments',
    component: GraderAssessmentComponent,
  },
  {
    path: 'result/view',
    component: GraderResultViewComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AiGraderRoutingModule {}
