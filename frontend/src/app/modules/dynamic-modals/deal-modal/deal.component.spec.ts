// // import { ComponentFixture, TestBed } from '@angular/core/testing';
// // import { NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
// // import { Router } from '@angular/router';
// // import { of, throwError } from 'rxjs';
// // import { HttpConstants } from 'src/app/core/constants/http.constants';
// // import { AuthService } from 'src/app/core/services/auth.service';
// // import { MessageService } from 'src/app/core/services/message.service';
// // import { DealComponent } from './deal.component';
// // import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
// // import { SubscriptionPlanComponent } from '../../auth/subscription-plan/subscription-plan.component';
// // import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
// // import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
// // import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

// // describe('DealComponent', () => {
// //   let component: DealComponent;
// //   let fixture: ComponentFixture<DealComponent>;
// //   let authServiceSpy: jasmine.SpyObj<AuthService>;
// //   let routerSpy: jasmine.SpyObj<Router>;
// //   let messageServiceSpy: jasmine.SpyObj<MessageService>;
// //   let modalServiceSpy: jasmine.SpyObj<NzModalService>;
// //   let modalRefSpy: jasmine.SpyObj<NzModalRef>;

// //   beforeEach(async () => {
// //     authServiceSpy = jasmine.createSpyObj('AuthService', [
// //       'cancelSubscription',
// //     ]);
// //     routerSpy = jasmine.createSpyObj('Router', ['navigate']);
// //     messageServiceSpy = jasmine.createSpyObj('MessageService', ['error']);
// //     modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
// //     modalRefSpy = jasmine.createSpyObj('NzModalRef', ['destroy']);

// //     await TestBed.configureTestingModule({
// //       declarations: [DealComponent],
// //       imports: [AntDesignModule, BrowserAnimationsModule],
// //       providers: [
// //         { provide: AuthService, useValue: authServiceSpy },
// //         { provide: Router, useValue: routerSpy },
// //         { provide: MessageService, useValue: messageServiceSpy },
// //         { provide: NzModalService, useValue: modalServiceSpy },
// //         { provide: NzModalRef, useValue: modalRefSpy },
// //         HttpConstants,
// //       ],
// //       schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
// //     }).compileComponents();
// //   });

// //   beforeEach(() => {
// //     fixture = TestBed.createComponent(DealComponent);
// //     component = fixture.componentInstance;
// //     fixture.detectChanges();
// //   });

// //   it('should create', () => {
// //     expect(component).toBeTruthy();
// //   });

// //   it('should call cancelSubscription and open confirmation modal on success', () => {
// //     const successResponse = {
// //       status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
// //     };
// //     authServiceSpy.cancelSubscription.and.returnValue(of(successResponse));
// //     spyOn(component, 'openConfirmationModal');

// //     component.cancelSubscription();

// //     expect(authServiceSpy.cancelSubscription).toHaveBeenCalled();
// //     expect(component.openConfirmationModal).toHaveBeenCalled();
// //   });

// //   it('should call cancelSubscription and handle error', () => {
// //     const errorResponse = { error: { message: 'Error occurred' } };
// //     authServiceSpy.cancelSubscription.and.returnValue(
// //       throwError(errorResponse)
// //     );

// //     component.cancelSubscription();

// //     expect(authServiceSpy.cancelSubscription).toHaveBeenCalled();
// //     expect(messageServiceSpy.error).toHaveBeenCalledWith(
// //       errorResponse.error.message
// //     );
// //   });

// //   it('should open confirmation modal', () => {
// //     component.openConfirmationModal();

// //     expect(modalServiceSpy.create).toHaveBeenCalledWith({
// //       nzContent: ConfirmationModalComponent,
// //       nzViewContainerRef: component['viewContainerRef'],
// //       nzComponentParams: { data: '', title: 'Cancel' },
// //       nzFooter: null,
// //       nzKeyboard: true,
// //     });
// //   });

// //   it('should open subscription plan modal', () => {
// //     component.openSubscriptionPlan();

// //     expect(modalServiceSpy.create).toHaveBeenCalledWith({
// //       nzContent: SubscriptionPlanComponent,
// //       nzViewContainerRef: component['viewContainerRef'],
// //       nzFooter: null,
// //       nzKeyboard: true,
// //       nzWidth: '80%',
// //     });
// //   });
// // });

// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
// import { Router } from '@angular/router';
// import { of, throwError } from 'rxjs';
// import { HttpConstants } from 'src/app/core/constants/http.constants';
// import { AuthService } from 'src/app/core/services/auth.service';
// import { MessageService } from 'src/app/core/services/message.service';
// import { DealComponent } from './deal.component';
// import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
// import { SubscriptionPlanComponent } from '../../auth/subscription-plan/subscription-plan.component';
// import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, ViewContainerRef } from '@angular/core';
// import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
// import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

// describe('DealComponent', () => {
//   let component: DealComponent;
//   let fixture: ComponentFixture<DealComponent>;
//   let authServiceSpy: jasmine.SpyObj<AuthService>;
//   let routerSpy: jasmine.SpyObj<Router>;
//   let messageServiceSpy: jasmine.SpyObj<MessageService>;
//   let modalServiceSpy: jasmine.SpyObj<NzModalService>;
//   let viewContainerRefSpy: jasmine.SpyObj<ViewContainerRef>;
//   let modalRefSpy: jasmine.SpyObj<NzModalRef>;

//   beforeEach(async () => {
//     authServiceSpy = jasmine.createSpyObj('AuthService', [
//       'cancelSubscription',
//     ]);
//     routerSpy = jasmine.createSpyObj('Router', ['navigate']);
//     messageServiceSpy = jasmine.createSpyObj('MessageService', ['error']);
//     modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
//     viewContainerRefSpy = jasmine.createSpyObj('ViewContainerRef', ['element']);
//     modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);

//     await TestBed.configureTestingModule({
//       declarations: [DealComponent],
//       imports : [AntDesignModule , BrowserAnimationsModule],
//       providers: [
//         { provide: AuthService, useValue: authServiceSpy },
//         { provide: Router, useValue: routerSpy },
//         { provide: NzModalRef, useValue: modalRefSpy },
//         { provide: MessageService, useValue: messageServiceSpy },
//         { provide: NzModalService, useValue: modalServiceSpy },
//         { provide: ViewContainerRef, useValue: viewContainerRefSpy },
//         HttpConstants,
//       ],
//       schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
//     }).compileComponents();
//   });

//   beforeEach(() => {
//     fixture = TestBed.createComponent(DealComponent);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });

//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });

//   it('should call cancelSubscription and open confirmation modal on success', () => {
//     const successResponse = {
//       status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
//     };
//     authServiceSpy.cancelSubscription.and.returnValue(of(successResponse));
//     spyOn(component, 'openConfirmationModal');

//     component.cancelSubscription();

//     expect(authServiceSpy.cancelSubscription).toHaveBeenCalled();
//     expect(component.openConfirmationModal).toHaveBeenCalled();
//   });

//   it('should call cancelSubscription and handle error', () => {
//     const errorResponse = { error: { message: 'Error occurred' } };
//     authServiceSpy.cancelSubscription.and.returnValue(
//       throwError(errorResponse)
//     );

//     component.cancelSubscription();

//     expect(authServiceSpy.cancelSubscription).toHaveBeenCalled();
//     expect(messageServiceSpy.error).toHaveBeenCalledWith(
//       errorResponse.error.message
//     );
//   });

//   it('should open confirmation modal', () => {
//     component['viewContainerRef'] = viewContainerRefSpy;

//     component.openConfirmationModal();

//     expect(modalServiceSpy.create).toHaveBeenCalledWith({
//       nzContent: ConfirmationModalComponent,
//       nzViewContainerRef: viewContainerRefSpy, // Expect the actual spy here
//       nzComponentParams: { data: '', title: 'Cancel' },
//       nzFooter: null,
//       nzKeyboard: true,
//     });
//   });

//   it('should open subscription plan modal', () => {
//     component['viewContainerRef'] = viewContainerRefSpy;

//     component.openSubscriptionPlan();

//     expect(modalServiceSpy.create).toHaveBeenCalledWith({
//       nzContent: SubscriptionPlanComponent,
//       nzViewContainerRef: viewContainerRefSpy, // Expect the actual spy here
//       nzFooter: null,
//       nzKeyboard: true,
//       nzWidth: '80%',
//     });
//   });
// });

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { DealComponent } from './deal.component';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
import { SubscriptionPlanComponent } from '../../auth/subscription-plan/subscription-plan.component';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA,
  ViewContainerRef,
} from '@angular/core';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('DealComponent', () => {
  let component: DealComponent;
  let fixture: ComponentFixture<DealComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let modalServiceSpy: jasmine.SpyObj<NzModalService>;
  let modalRefSpy: jasmine.SpyObj<NzModalRef>;
  let viewContainerRefSpy: jasmine.SpyObj<ViewContainerRef>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'cancelSubscription',
    ]);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['error']);
    modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    modalRefSpy = jasmine.createSpyObj('NzModalRef', ['destroy']);
    viewContainerRefSpy = jasmine.createSpyObj('ViewContainerRef', ['element']);

    await TestBed.configureTestingModule({
      declarations: [DealComponent],
      imports: [AntDesignModule, BrowserAnimationsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: NzModalRef, useValue: modalRefSpy },
        { provide: ViewContainerRef, useValue: viewContainerRefSpy },
        HttpConstants,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DealComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('cancelSubscription', () => {
    it('should call cancelSubscription and open confirmation modal on success', () => {
      const successResponse = {
        status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      };
      authServiceSpy.cancelSubscription.and.returnValue(of(successResponse));
      spyOn(component, 'openConfirmationModal');

      component.cancelSubscription();

      expect(authServiceSpy.cancelSubscription).toHaveBeenCalled();
      expect(component.openConfirmationModal).toHaveBeenCalled();
    });

    it('should call cancelSubscription and handle error', () => {
      const errorResponse = { error: { message: 'Error occurred' } };
      authServiceSpy.cancelSubscription.and.returnValue(
        throwError(errorResponse)
      );

      component.cancelSubscription();

      expect(authServiceSpy.cancelSubscription).toHaveBeenCalled();
      expect(messageServiceSpy.error).toHaveBeenCalledWith(
        errorResponse.error.message
      );
    });
  });

  describe('openConfirmationModal', () => {
    it('should open confirmation modal', () => {
      component.openConfirmationModal();

      expect(modalServiceSpy.create).toHaveBeenCalledWith(
        jasmine.objectContaining({
          nzContent: ConfirmationModalComponent,
          nzComponentParams: { data: '', title: 'Cancel' },
          nzFooter: null,
          nzKeyboard: true,
        })
      );
    });
  });
  describe('openSubscriptionPlan', () => {
    it('should open subscription plan modal', () => {
      component.openSubscriptionPlan();

      expect(modalServiceSpy.create).toHaveBeenCalledWith(
        jasmine.objectContaining({
          nzContent: SubscriptionPlanComponent,
          nzFooter: null,
          nzKeyboard: true,
          nzWidth: '80%',
        })
      );
    });
  });
});
