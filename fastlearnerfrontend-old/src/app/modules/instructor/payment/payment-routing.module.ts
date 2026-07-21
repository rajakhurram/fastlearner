import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { AuthGuard } from "src/app/core/guards/auth.guard";
import { PaymentComponent } from "./payment.component";

const routes: Routes = [{ path: '', component: PaymentComponent, canActivate: [AuthGuard],  data: { title: 'Payment' }}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PaymentRoutingModule { }