import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { AiGraderLandingPageComponent } from './ai-grader-landing-page.component';
import { PdfViewerModule } from 'ng2-pdf-viewer';

const routes: Routes = [
  {
    path: '',
    component: AiGraderLandingPageComponent,
    data: { title: 'AI Grader' },
  },
];

@NgModule({
  declarations: [AiGraderLandingPageComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AntDesignModule,
    PdfViewerModule,
    SharedModule,
    RouterModule.forChild(routes),
  ],
})
export class AiGraderLandingPageModule {}

