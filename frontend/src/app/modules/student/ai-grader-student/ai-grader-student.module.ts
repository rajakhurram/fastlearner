import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../../shared/shared.module';
import { FormsModule } from '@angular/forms'; 
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzCardModule } from 'ng-zorro-antd/card';
import { AiGraderStudentComponent } from './ai-grader-student.component';
import { AiGraderStudentRoutingModule } from './ai-grader-student-routing.module';
import { AiGraderStudentResultComponent } from './ai-grader-student-result/ai-grader-student-result/ai-grader-student-result.component';

@NgModule({
  declarations: [
   AiGraderStudentComponent,
   AiGraderStudentResultComponent,
  //  ClassAssessmentDropdownComponent
  ],
  imports: [
    CommonModule,
    NzCardModule,
    AntDesignModule,
    SharedModule,
    FormsModule,
    PdfViewerModule,
    NzInputModule,
    AiGraderStudentRoutingModule
],
providers: [DatePipe]
})
export class AiGraderStudentModule {}
