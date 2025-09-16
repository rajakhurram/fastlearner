import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NzHeaderComponent, NzLayoutModule } from 'ng-zorro-antd/layout';
import { NZ_I18N } from 'ng-zorro-antd/i18n';
import { en_US } from 'ng-zorro-antd/i18n';
import { registerLocaleData } from '@angular/common';
import en from '@angular/common/locales/en';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzIconModule } from 'ng-zorro-antd/icon';
import {
  MenuFoldOutline,
  MenuUnfoldOutline,
  ShareAltOutline,
  ExpandAltOutline,
  MoreOutline,
  UserOutline,
  PlusOutline,
  HeartOutline
} from '@ant-design/icons-angular/icons';
import { IconDefinition } from '@ant-design/icons-angular';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzMessageModule } from 'ng-zorro-antd/message';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzAutocompleteModule } from 'ng-zorro-antd/auto-complete';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzRateModule } from 'ng-zorro-antd/rate';
import { HttpClientModule } from '@angular/common/http';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzCommentModule } from 'ng-zorro-antd/comment';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzPopoverModule } from 'ng-zorro-antd/popover';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzResultModule } from 'ng-zorro-antd/result';
import { NzDrawerModule, NzDrawerPlacement } from 'ng-zorro-antd/drawer';

const icons: IconDefinition[] = [
  MenuFoldOutline,
  MenuUnfoldOutline,
  ShareAltOutline,
  ExpandAltOutline,
  MoreOutline,
  UserOutline,
  PlusOutline,
  HeartOutline
];
registerLocaleData(en);
@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NzLayoutModule,
    NzBreadCrumbModule,
    NzIconModule.forChild(icons),
    NzMenuModule,
    NzGridModule,
    NzCardModule,
    NzInputModule,
    NzButtonModule,
    NzAvatarModule,
    NzTableModule,
    NzModalModule,
    NzSelectModule,
    NzCheckboxModule,
    NzSwitchModule,
    NzFormModule,
    NzDividerModule,
    NzMessageModule,
    NzDropDownModule,
    NzProgressModule,
    NzPaginationModule,
    NzCollapseModule,
    NzToolTipModule,
    NzDatePickerModule,
    NzStepsModule,
    NzTagModule,
    NzDividerModule,
    NzAutocompleteModule,
    NzEmptyModule,
    NzRateModule,
    HttpClientModule,
    NzRadioModule,
    NzBadgeModule,
    NzTabsModule,
    NzCommentModule,
    NzListModule,
    NzPopoverModule,
    NzSpinModule,
    NzUploadModule,
    NzResultModule,
    NzDrawerModule
  ],
  exports: [
    FormsModule,
    ReactiveFormsModule,
    NzProgressModule,
    NzLayoutModule,
    NzBreadCrumbModule,
    NzIconModule,
    NzMenuModule,
    NzGridModule,
    NzCardModule,
    NzInputModule,
    NzButtonModule,
    NzAvatarModule,
    NzTableModule,
    NzModalModule,
    NzSelectModule,
    NzCheckboxModule,
    NzSwitchModule,
    NzFormModule,
    NzDividerModule,
    NzMessageModule,
    NzDropDownModule,
    NzPaginationModule,
    NzCollapseModule,
    NzToolTipModule,
    NzDatePickerModule,
    NzStepsModule,
    NzTagModule,
    NzDividerModule,
    NzAutocompleteModule,
    NzEmptyModule,
    NzRateModule,
    HttpClientModule,
    NzRadioModule,
    NzBadgeModule,
    NzTabsModule,
    NzCommentModule,
    NzListModule,
    NzPopoverModule,
    NzSpinModule,
    NzStepsModule,
    NzCheckboxModule,
    NzUploadModule,
    NzResultModule,
    NzDrawerModule
  ],
  providers: [{ provide: NZ_I18N, useValue: en_US }],
})
export class AntDesignModule {}
