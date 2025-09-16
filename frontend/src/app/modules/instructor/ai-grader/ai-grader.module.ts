import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiGraderComponent } from './ai-grader.component';
import { AiGraderRoutingModule } from './ai-grader-routing.module';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../../shared/shared.module';
import { FormsModule } from '@angular/forms';
import { ClassUploaderComponent } from '../../dynamic-modals/class-uploader/class-uploader.component';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { GraderResultsComponent } from './grader-results/grader-results.component';
import { GraderUploaderComponent } from './grader-uploader/grader-uploader.component';
import { GraderClassesComponent } from './grader-classes/grader-classes.component';
import { GraderAssessmentComponent } from './grader-assessment/grader-assessment.component';
import { NzInputModule } from 'ng-zorro-antd/input';
import { ClassAssessmentDropdownComponent } from '../../shared/class-assessment-dropdown/class-assessment-dropdown.component';
import { GraderResultViewComponent } from './grader-result-view/grader-result-view.component';
import { ClickOutsideDirective } from '../../directives/click-outside.directive';
import { NzProgressModule } from 'ng-zorro-antd/progress';



@NgModule({
  declarations: [
    AiGraderComponent,
    ClassUploaderComponent,
    GraderResultsComponent,
    GraderUploaderComponent,
    GraderClassesComponent,
    GraderAssessmentComponent,
    GraderResultViewComponent,
    ClickOutsideDirective
  ],
  imports: [
    CommonModule,
    AiGraderRoutingModule,
    AntDesignModule,
    SharedModule,
    FormsModule,
    PdfViewerModule,
    NzInputModule,
    NzProgressModule,
  ],
  exports: [
  ]
})
export class AiGraderModule { }
