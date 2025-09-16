import { NgModule } from "@angular/core";
import { NgModel } from "@angular/forms";
import { RouterModule, Routes } from "@angular/router";
import { PremiumStudentComponent } from "./premium-student.component";
import { AuthGuard } from "src/app/core/guards/auth.guard";


  
const routes: Routes = [{ path: '', component: PremiumStudentComponent, canActivate: [AuthGuard],  data: { title: 'Premium Student' }}];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports : [RouterModule]
})
export class Premium_StudentRoutingModule{}