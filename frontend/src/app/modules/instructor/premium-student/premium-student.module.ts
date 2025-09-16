import { NgModule } from "@angular/core";
import { PremiumStudentComponent } from "./premium-student.component";
import { CommonModule, DatePipe } from "@angular/common";
import { AntDesignModule } from "src/app/ui-library/ant-design/ant-design.module";
import { HighchartsChartModule } from "highcharts-angular";
import { SharedModule } from "../../shared/shared.module";
import { Premium_StudentRoutingModule } from "./premium-student-routing.module";
import { FormsModule } from "@angular/forms";
import { LOCALE_CONFIG, NgxDaterangepickerBootstrapModule,NgxDaterangepickerLocaleService } from "ngx-daterangepicker-bootstrap";

@NgModule({
    declarations: [
      PremiumStudentComponent
    ],
    imports: [
      CommonModule,
      AntDesignModule,
      HighchartsChartModule,
      Premium_StudentRoutingModule,
      SharedModule,
      FormsModule,
      NgxDaterangepickerBootstrapModule.forRoot(),
    ],
    providers: [
      DatePipe,
      NgxDaterangepickerLocaleService,
    ],
  })
  export class Premium_StudentModule { }
  