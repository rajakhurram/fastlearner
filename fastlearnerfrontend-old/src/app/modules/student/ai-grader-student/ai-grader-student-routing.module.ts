import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AiGraderStudentComponent } from "./ai-grader-student.component";
import { AiGraderStudentResultComponent } from "./ai-grader-student-result/ai-grader-student-result/ai-grader-student-result.component";

const routes: Routes = [
    {path:'', component: AiGraderStudentComponent},
    {path:'view', component: AiGraderStudentResultComponent},

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})

export class AiGraderStudentRoutingModule{}