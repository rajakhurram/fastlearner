import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { VerifyCertificateComponent } from './verify-certificate.component';
import { CertificateService } from 'src/app/core/services/certificate.service';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from 'src/environments/environment.development';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';

class MockCertificateService {
  verifyCertificate(uuid: string) {
    return of({}); // mock successful response
  }
}

describe('VerifyCertificateComponent', () => {
  let component: VerifyCertificateComponent;
  let fixture: ComponentFixture<VerifyCertificateComponent>;
  let mockRouter: Router;
  let mockCertificateService: MockCertificateService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, AntDesignModule],
      declarations: [VerifyCertificateComponent],
      providers: [
        { provide: CertificateService, useClass: MockCertificateService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: (key: string) => 'some-uuid' } },
          },
        },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VerifyCertificateComponent);
    component = fixture.componentInstance;
    mockRouter = TestBed.inject(Router);
    mockCertificateService = TestBed.inject(
      CertificateService
    ) as unknown as MockCertificateService;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with the correct UUID and verify certificate', () => {
    spyOn(component, 'verifyCertificate').and.callThrough();
    component.ngOnInit();
    expect(component.uuid).toBe('some-uuid');
    expect(component.verifyCertificate).toHaveBeenCalledWith('some-uuid');
  });

  it('should verify certificate and update state on success', () => {
    component.verifyCertificate('some-uuid');
    fixture.detectChanges();
    expect(component.isVerified).toBeTrue();
    expect(component.certificateImageUrl).toBe(
      `${environment.baseUrl}certificate/verify/some-uuid`
    );
  });

  it('should handle error during certificate verification', () => {
    spyOn(mockCertificateService, 'verifyCertificate').and.returnValue(
      throwError('Error')
    );
    component.verifyCertificate('some-uuid');
    fixture.detectChanges();
    expect(component.isVerified).toBeFalse();
    expect(component.showSpin).toBeFalse();
  });

  it('should navigate to welcome-instructor page on routeToInstructorWelcomePage call', () => {
    spyOn(mockRouter, 'navigate');
    component.routeToInstructorWelcomePage();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['welcome-instructor']);
  });

  it('should hide the spinner after 2500ms', (done) => {
    component.ngOnInit();
    setTimeout(() => {
      fixture.detectChanges();
      expect(component.showSpin).toBeFalse();
      done();
    }, 2500);
  });

  it('should set certificateImageUrl and isVerified on successful verification', () => {
    const uuid = 'test-uuid';
    const imageUrl = `${environment.baseUrl}certificate/verify/${uuid}`;

    spyOn(mockCertificateService, 'verifyCertificate').and.returnValue(of({})); // Mock service response
    spyOn(component, 'verifyCertificate').and.callThrough();

    component.verifyCertificate(uuid);

    expect(mockCertificateService.verifyCertificate).toHaveBeenCalledWith(uuid);
    expect(component.certificateImageUrl).toBe(imageUrl);
    expect(component.isVerified).toBeTrue();
  });
  it('should handle error during certificate verification', () => {
    const uuid = 'test-uuid';

    spyOn(mockCertificateService, 'verifyCertificate').and.returnValue(
      throwError(() => new Error('Verification failed'))
    );
    spyOn(console, 'error'); // Mock console.error to suppress error logs

    component.verifyCertificate(uuid);

    expect(mockCertificateService.verifyCertificate).toHaveBeenCalledWith(uuid);
    expect(component.isVerified).toBeFalse();
    expect(component.showSpin).toBeFalse();
    expect(console.error).toHaveBeenCalledWith(
      'Error verifying certificate:',
      jasmine.any(Error)
    );
  });
  it('should navigate to instructor welcome page', () => {
    spyOn(mockRouter, 'navigate');

    component.routeToInstructorWelcomePage();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['welcome-instructor']);
  });
  it('should set certificateImageUrl correctly', () => {
    const uuid = 'test-uuid';
    const imageUrl = `${environment.baseUrl}certificate/verify/${uuid}`;

    component.certificateImageUrl = imageUrl;

    expect(component.certificateImageUrl).toBe(imageUrl);
  });
  it('should correctly calculate certificateImageUrl', () => {
    const uuid = 'test-uuid';
    const expectedUrl = `${environment.baseUrl}certificate/verify/${uuid}`;

    component.certificateImageUrl = expectedUrl;

    expect(component.certificateImageUrl).toBe(expectedUrl);
  });

  it('should hide spinner after successful verification', () => {
    const uuid = 'test-uuid';
    spyOn(mockCertificateService, 'verifyCertificate').and.returnValue(of({}));

    component.verifyCertificate(uuid);
    fixture.detectChanges();

    expect(component.showSpin).toBeFalse();
  });
});
