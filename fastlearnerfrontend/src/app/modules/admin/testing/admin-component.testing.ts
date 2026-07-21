import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzTableModule } from 'ng-zorro-antd/table';
import { of } from 'rxjs';
import { MessageService } from 'src/app/core/services/message.service';
import { AdminService } from '../admin.service';

const listResponse = {
  status: 200,
  data: { content: [], totalElements: 0, totalPages: 0 },
};

const arrayResponse = { status: 200, data: [] };
const dataResponse = { status: 200, data: {} };

export function createAdminServiceSpy(): jasmine.SpyObj<AdminService> {
  const adminServiceSpy = jasmine.createSpyObj<AdminService>(
    'AdminService',
    [
      'getCouponsList',
      'getCoupenSubscription',
      'getCoursesList',
      'getCoupenById',
      'updateCoupon',
      'deleteCoupon',
      'createCoupon',
      'toggleCouponStatus',
      'getSettingsAdmins',
      'addAdmin',
      'deActivateAdmin',
      'getPayoutStats',
      'getPayoutsList',
      'getPayoutDetails',
      'getCourseCategory',
      'getCourseDetails',
      'getInstructorsList',
      'coursePublish',
      'courseUnpublish',
      'getEnrollmentsList',
      'getPaymentList',
      'downloadPaymentCSV',
      'getSubscriptionStats',
      'getSubscriptionsList',
      'getUserStats',
      'getUsersList',
      'getUserOverview',
      'getSubscriptionOverview',
      'getCourseOverview',
      'getTransactionOverview',
      'inviteAdmin',
      'exportUserCsv',
      'getInvoicesList',
      'getInvoiceDetailById',
      'downloadInvoice',
      'sendInvoice',
    ],
  );

  adminServiceSpy.currentPage = 1;

  adminServiceSpy.getCouponsList.and.returnValue(of(listResponse));
  adminServiceSpy.getCoupenSubscription.and.returnValue(of(arrayResponse));
  adminServiceSpy.getCoursesList.and.returnValue(
    of({ status: 200, data: { content: [] } }),
  );
  adminServiceSpy.getCoupenById.and.returnValue(of(dataResponse));
  adminServiceSpy.updateCoupon.and.returnValue(of(dataResponse));
  adminServiceSpy.deleteCoupon.and.returnValue(of(dataResponse));
  adminServiceSpy.createCoupon.and.returnValue(of(dataResponse));
  adminServiceSpy.toggleCouponStatus.and.returnValue(of(dataResponse));
  adminServiceSpy.getSettingsAdmins.and.returnValue(of(arrayResponse));
  adminServiceSpy.addAdmin.and.returnValue(of(dataResponse));
  adminServiceSpy.deActivateAdmin.and.returnValue(of(dataResponse));
  adminServiceSpy.getPayoutStats.and.returnValue(of(dataResponse));
  adminServiceSpy.getPayoutsList.and.returnValue(of(listResponse));
  adminServiceSpy.getPayoutDetails.and.returnValue(of(dataResponse));
  adminServiceSpy.getCourseCategory.and.returnValue(of(arrayResponse));
  adminServiceSpy.getCourseDetails.and.returnValue(of(dataResponse));
  adminServiceSpy.getInstructorsList.and.returnValue(of(arrayResponse));
  adminServiceSpy.coursePublish.and.returnValue(of(dataResponse));
  adminServiceSpy.courseUnpublish.and.returnValue(of(dataResponse));
  adminServiceSpy.getEnrollmentsList.and.returnValue(of(listResponse));
  adminServiceSpy.getPaymentList.and.returnValue(of(listResponse));
  adminServiceSpy.downloadPaymentCSV.and.returnValue(of(dataResponse));
  adminServiceSpy.getSubscriptionStats.and.returnValue(of(dataResponse));
  adminServiceSpy.getSubscriptionsList.and.returnValue(of(listResponse));
  adminServiceSpy.getUserStats.and.returnValue(of(dataResponse));
  adminServiceSpy.getUsersList.and.returnValue(of(listResponse));
  adminServiceSpy.getUserOverview.and.returnValue(of(dataResponse));
  adminServiceSpy.getSubscriptionOverview.and.returnValue(of(dataResponse));
  adminServiceSpy.getCourseOverview.and.returnValue(of(dataResponse));
  adminServiceSpy.getTransactionOverview.and.returnValue(of(dataResponse));
  adminServiceSpy.inviteAdmin.and.returnValue(of(dataResponse));
  adminServiceSpy.exportUserCsv.and.returnValue(of(dataResponse));
  adminServiceSpy.getInvoicesList.and.returnValue(of(listResponse));
  adminServiceSpy.getInvoiceDetailById.and.returnValue(of(dataResponse));
  adminServiceSpy.downloadInvoice.and.returnValue(of(dataResponse));
  adminServiceSpy.sendInvoice.and.returnValue(of(dataResponse));

  return adminServiceSpy;
}

export const adminComponentTestImports = [
  CommonModule,
  FormsModule,
  BrowserAnimationsModule,
  NzInputModule,
  NzSelectModule,
  NzDatePickerModule,
  NzTableModule,
  NzButtonModule,
  NzPaginationModule,
  NzSwitchModule,
  NzModalModule,
  NzDropDownModule,
  NzCheckboxModule,
];

export const adminComponentTestProviders = [
  { provide: AdminService, useValue: createAdminServiceSpy() },
  {
    provide: MessageService,
    useValue: jasmine.createSpyObj('MessageService', ['error', 'success']),
  },
  {
    provide: Router,
    useValue: jasmine.createSpyObj('Router', ['navigate'], {
      url: '/admin/users',
    }),
  },
];

export const adminComponentTestSchemas = [
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA,
];

export function createAdminComponentTestProviders(
  adminService: jasmine.SpyObj<AdminService> = createAdminServiceSpy(),
  extraProviders: unknown[] = [],
): unknown[] {
  return [
    ...adminComponentTestProviders.filter(
      (provider) => provider.provide !== AdminService,
    ),
    { provide: AdminService, useValue: adminService },
    ...extraProviders,
  ];
}

export async function configureAdminComponentTest<T>(
  component: new (...args: unknown[]) => T,
  extraProviders: unknown[] = [],
  adminService: jasmine.SpyObj<AdminService> = createAdminServiceSpy(),
): Promise<{
  fixture: ComponentFixture<T>;
  component: T;
  adminService: jasmine.SpyObj<AdminService>;
  messageService: jasmine.SpyObj<MessageService>;
}> {
  await TestBed.configureTestingModule({
    declarations: [component],
    imports: adminComponentTestImports,
    providers: createAdminComponentTestProviders(adminService, extraProviders),
    schemas: adminComponentTestSchemas,
  }).compileComponents();

  const fixture = TestBed.createComponent(component);
  const messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
  fixture.detectChanges();

  return {
    fixture,
    component: fixture.componentInstance,
    adminService,
    messageService,
  };
}
