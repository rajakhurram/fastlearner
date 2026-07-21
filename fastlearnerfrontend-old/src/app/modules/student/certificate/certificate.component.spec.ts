import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CertificateComponent } from './certificate.component';
import { CertificateService } from 'src/app/core/services/certificate.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { ShareModalComponent } from '../../dynamic-modals/share-modal/share-modal.component';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Certificate } from 'src/app/core/models/certificate.model';
import { ActivatedRoute } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';

describe('CertificateComponent', () => {
  let component: CertificateComponent;
  let fixture: ComponentFixture<CertificateComponent>;
  let mockCertificateService: jasmine.SpyObj<CertificateService>;
  let mockModalService: jasmine.SpyObj<NzModalService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockActivatedRoute: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(async () => {
    const certificateServiceSpy = jasmine.createSpyObj('CertificateService', [
      'getCertificateData',
      'getCertificateUrl',
    ]);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['signIn']);
    const activatedRouteSpy = jasmine.createSpyObj(
      'ActivatedRoute',
      ['queryParams'],
      {
        queryParams: of({ courseId: '123' }),
      }
    );

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        SharedModule,
        BrowserAnimationsModule,
        AntDesignModule,
      ],
      declarations: [CertificateComponent, ShareModalComponent],
      providers: [
        { provide: CertificateService, useValue: certificateServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(CertificateComponent);
    component = fixture.componentInstance;
    mockCertificateService = TestBed.inject(
      CertificateService
    ) as jasmine.SpyObj<CertificateService>;
    mockModalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
    mockAuthService = TestBed.inject(
      AuthService
    ) as jasmine.SpyObj<AuthService>;
    mockActivatedRoute = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getRouteQueryParam on init', () => {
    spyOn(component, 'getRouteQueryParam').and.callThrough();
    component.ngOnInit();
    expect(component.getRouteQueryParam).toHaveBeenCalled();
  });

  it('should get courseId from route and call getCertificateData and getCertificate', fakeAsync(() => {
    const courseId = '123';
    spyOn(component, 'getCertificateData').and.callThrough();
    spyOn(component, 'getCertificate').and.callThrough();
    component.getRouteQueryParam();
    tick();
    expect(component.courseId).toBe(courseId);
    expect(component.getCertificateData).toHaveBeenCalledWith(courseId);
    expect(component.getCertificate).toHaveBeenCalledWith(courseId);
  }));

  it('should format date correctly', () => {
    const date = new Date('2024-08-09T00:00:00Z');
    const formattedDate = component.formatDate(date.toISOString());
    expect(formattedDate).toBe('08/09/2024');
  });

  it('should open the share modal with correct parameters', () => {
    component.uuid = 'test-uuid';
    mockModalService.create.and.returnValue({ afterClose: of(null) } as any);
    component.openShareCourseModal();
    expect(mockModalService.create).toHaveBeenCalledWith({
      nzContent: ShareModalComponent,
      nzViewContainerRef: component['_viewContainerRef'],
      nzComponentParams: {
        data: null,
        url: `https://fastlearner.ai/student/verify-certificate/${component.uuid}`,
        title: 'Share Certificate',
        label: 'Share Certificate URL',
      },
      nzFooter: null,
      nzKeyboard: true,
    });
  });

  it('should call convertImageToPDF with correct URL', async () => {
    const imageUrl = 'test-image-url';
    spyOn(component as any, 'convertImageToPDF').and.callThrough();
    mockCertificateService.getCertificateUrl.and.returnValue(imageUrl);
    component.downloadPDF();
    expect(component['convertImageToPDF']).toHaveBeenCalledWith(imageUrl);
  });

  it('should call convertImageToJPG with correct URL', async () => {
    const imageUrl = 'test-image-url';
    spyOn(component as any, 'convertImageToJPG').and.callThrough();
    mockCertificateService.getCertificateUrl.and.returnValue(imageUrl);
    component.downloadJPG();
    expect(component['convertImageToJPG']).toHaveBeenCalledWith(imageUrl);
  });

  it('should handle error in convertImageToPDF', async () => {
    const error = new Error('Error converting image');
    spyOn(window, 'fetch').and.returnValue(Promise.reject(error));
    spyOn(console, 'error');
    await component['convertImageToPDF']('invalid-url');
    expect(console.error).toHaveBeenCalledWith(
      'Error converting image to PDF',
      error
    );
  });

  it('should handle error in convertImageToJPG', async () => {
    const error = new Error('Error converting image');
    spyOn(window, 'fetch').and.returnValue(Promise.reject(error));
    spyOn(console, 'error');
    await component['convertImageToJPG']('invalid-url');
    expect(console.error).toHaveBeenCalledWith(
      'Error converting image to JPG',
      error
    );
  });

});
