import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AffiliateComponent } from './affiliate.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { By } from '@angular/platform-browser';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('AffiliateComponent', () => {
  let component: AffiliateComponent;
  let fixture: ComponentFixture<AffiliateComponent>;
  let mockAffiliateService: jasmine.SpyObj<AffiliateService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockModalService: jasmine.SpyObj<NzModalService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    mockAffiliateService = jasmine.createSpyObj('AffiliateService', [
      'getAffiliates',
      'deleteAffiliate',
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockModalService = jasmine.createSpyObj('NzModalService', ['create']);
    mockMessageService = jasmine.createSpyObj('MessageService', ['success']);

    await TestBed.configureTestingModule({
      declarations: [AffiliateComponent],
      providers: [
        { provide: AffiliateService, useValue: mockAffiliateService },
        { provide: Router, useValue: mockRouter },
        { provide: NzModalService, useValue: mockModalService },
        { provide: MessageService, useValue: mockMessageService },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AffiliateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch affiliate data on init', () => {
    const mockResponse = {
      status: component._httpConstants['REQUEST_STATUS'].SUCCESS_200,
      data: {
        affiliates: [],
        pageNo: 0,
        pageSize: 8,
        totalElements: 0,
      },
    };
    mockAffiliateService.getAffiliates.and.returnValue(of(mockResponse));
    component.ngOnInit();
    expect(mockAffiliateService.getAffiliates).toHaveBeenCalledWith(
      component.payload
    );
  });

  it('should handle error when fetching affiliate data', () => {
    mockAffiliateService.getAffiliates.and.returnValue(throwError({}));
    component.getAffiliateData(component.payload);
    expect(component.tableConfig.rowData.length).toBe(0);
    expect(component.showLoader).toBeFalse();
  });

  it('should open modal for adding an affiliate', () => {
    const modalRef = {
      afterClose: of(true),
    } as any;
    mockModalService.create.and.returnValue(modalRef);
    component.addAffiliate('EDIT');
    expect(mockModalService.create).toHaveBeenCalled();
  });

  it('should open delete modal and handle confirmation', () => {
    const modalRef = {
      afterClose: of(true),
    } as any;
    mockModalService.create.and.returnValue(modalRef);
    spyOn(component, 'deleteAffiliate');
    component.openDeleteModal({ id: 1 });
    expect(mockModalService.create).toHaveBeenCalled();
    expect(component.deleteAffiliate).toHaveBeenCalled();
  });

  it('should handle page change and fetch new data', () => {
    spyOn(component, 'getAffiliateData');
    component.handlePageChange(2);
    expect(component.payload.pageNo).toBe(1); // Since pages are 0-indexed
    expect(component.getAffiliateData).toHaveBeenCalledWith(component.payload);
  });

  it('should navigate to affiliate details on row click', () => {
    const mockEvent = {
      event: { stopPropagation: jasmine.createSpy() },
      row: { instructorAffiliateId: 1 },
    };
    component.onRowClick(mockEvent);
    expect(mockEvent.event.stopPropagation).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      'instructor/affiliate/affiliate-details',
      1,
    ]);
  });

  it('should trigger search when search term is typed', () => {
    spyOn(component, 'getAffiliateData');
    component.searchCallBack('test');
    expect(component.searchTerm).toBe('test');
  });
});
