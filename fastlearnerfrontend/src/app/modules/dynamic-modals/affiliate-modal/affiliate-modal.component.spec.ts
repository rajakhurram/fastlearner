import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AffiliateModalComponent } from './affiliate-modal.component';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { MessageService } from 'src/app/core/services/message.service';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { AffiliateMode } from 'src/app/core/enums/affiliate.enum';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('AffiliateModalComponent', () => {
  let component: AffiliateModalComponent;
  let fixture: ComponentFixture<AffiliateModalComponent>;
  let affiliateServiceSpy: jasmine.SpyObj<AffiliateService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let modalRefSpy: jasmine.SpyObj<NzModalRef>;

  beforeEach(async () => {
    affiliateServiceSpy = jasmine.createSpyObj('AffiliateService', [
      'createAffiliate',
      'editAffiliate',
    ]);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['success']);
    modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);

    await TestBed.configureTestingModule({
      declarations: [AffiliateModalComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: AffiliateService, useValue: affiliateServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NzModalRef, useValue: modalRefSpy },
        NzModalService,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AffiliateModalComponent);
    component = fixture.componentInstance;
    component.mode = AffiliateMode.ADD;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form on `ngOnInit`', () => {
    component.ngOnInit();
    expect(component.affiliateForm).toBeDefined();
    expect(component.affiliateForm.controls['name']).toBeDefined();
    expect(component.affiliateForm.controls['email']).toBeDefined();
  });

  it('should patch form values when data is provided', () => {
    const mockData = {
      name: 'John Doe',
      nickName: 'johnny',
      email: 'john.doe@example.com',
      defaultReward: 15,
    };
    component.data = mockData;
    component.ngOnInit();
    expect(component.affiliateForm.getRawValue()).toEqual({
      name: 'John Doe',
      nickName: 'johnny',
      email: 'john.doe@example.com',
      defaultReward: 15,
    });
    expect(component.affiliateForm.get('email').disabled).toBeTrue();
  });

  it('should validate email correctly', () => {
    const emailControl = component.affiliateForm.get('email');
    const eventMock = { target: { value: 'invalidemail@domain' } };
    component.validateEmail(eventMock);
    expect(emailControl.errors).toEqual({ invalidEmail: true });

    eventMock.target.value = 'valid.email@example.com';
    component.validateEmail(eventMock);
    expect(emailControl.errors).toBeNull();
  });

  it('should mark all controls as touched and dirty if the form is invalid on submit', () => {
    component.submitForm();
    const controls = component.affiliateForm.controls;
    Object.keys(controls).forEach((key) => {
      const control = controls[key];
      expect(control.touched).toBeTrue();
      expect(control.dirty).toBeTrue();
    });
  });

  it('should call `createAffiliate` if mode is ADD', () => {
    component.affiliateForm.setValue({
      name: 'Test Affiliate',
      nickName: 'test123',
      email: 'test@example.com',
      defaultReward: 15,
    });
    affiliateServiceSpy.createAffiliate.and.returnValue(
      of({ status: new HttpConstants().REQUEST_STATUS.SUCCESS_200.CODE })
    );

    component.submitForm();
    expect(affiliateServiceSpy.createAffiliate).toHaveBeenCalledWith(
      component.affiliateForm.value
    );
    expect(component.addedAffiliate).toBeTrue();
  });

  it('should call `editAffiliate` if mode is EDIT', () => {
    component.mode = AffiliateMode.EDIT;
    component.data = { instructorAffiliateId: 1 };
    component.affiliateForm.setValue({
      name: 'Updated Affiliate',
      nickName: 'update123',
      email: 'update@example.com',
      defaultReward: 20,
    });

    affiliateServiceSpy.editAffiliate.and.returnValue(
      of({ status: new HttpConstants().REQUEST_STATUS.SUCCESS_200.CODE })
    );

    component.submitForm();
    const expectedPayload = {
      ...component.affiliateForm.getRawValue(),
      instructorAffiliateId: 1,
    };
    expect(affiliateServiceSpy.editAffiliate).toHaveBeenCalledWith(
      expectedPayload
    );
    expect(messageServiceSpy.success).toHaveBeenCalledWith(
      'Affiliate updated successfully'
    );
  });

  it('should close the modal when `cancel` is called', () => {
    const mockEvent = { preventDefault: jasmine.createSpy() };
    component.cancel(mockEvent);
    expect(modalRefSpy.close).toHaveBeenCalledWith(false);
    expect(mockEvent.preventDefault).toHaveBeenCalled();
  });

  it('should handle Enter key and prevent default action', () => {
    const eventMock = jasmine.createSpyObj('KeyboardEvent', [
      'preventDefault',
      'stopPropagation',
    ]);
    component.handleEnterKey(eventMock as any);
    expect(eventMock.preventDefault).toHaveBeenCalled();
    expect(eventMock.stopPropagation).toHaveBeenCalled();
  });
});
