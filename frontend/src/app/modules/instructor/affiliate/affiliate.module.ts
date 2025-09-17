import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { AntDesignModule } from "src/app/ui-library/ant-design/ant-design.module";
import { AffiliateRoutingModule } from "./affiliate-routing.module";
import { AffiliateComponent } from "./affiliate.component";
import { PremiumCoursesComponent } from './premium-courses/premium-courses.component';
import { SharedModule } from "../../shared/shared.module";
import { SearchFilterComponent } from "../../shared/search-filter/search-filter.component";
import { ButtonComponent } from "../../shared/button/button.component";
import { TableComponent } from "../../shared/table/table.component";
import { AffliateDetailsComponent } from './affliate-details/affliate-details.component';
@NgModule({
    declarations: [
      AffiliateComponent,
      PremiumCoursesComponent,
      AffliateDetailsComponent,
    ],
    imports: [
      CommonModule,
      FormsModule,
      AffiliateRoutingModule,
      AntDesignModule,
      SharedModule
    ]
  })
  export class AffiliateModule { }