import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SummaryModalComponent } from './summary-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { of, throwError } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { BrowserModule } from '@angular/platform-browser';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { SocialAuthService } from '@abacritt/angularx-social-login';

describe('SummaryModalComponent', () => {
  let component: SummaryModalComponent;
  let fixture: ComponentFixture<SummaryModalComponent>;
  let modalService: NzModalService;
  let communicationService: CommunicationService;
  let fileManagerService: FileManager;
  let httpConstants: HttpConstants;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });
    await TestBed.configureTestingModule({
      declarations: [SummaryModalComponent],
      imports: [AntDesignModule, BrowserModule],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        NzModalService,
        CommunicationService,
        FileManager,
        { provide: SocialAuthService, useValue: spy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SummaryModalComponent);
    component = fixture.componentInstance;
    modalService = TestBed.inject(NzModalService);
    communicationService = TestBed.inject(CommunicationService);
    fileManagerService = TestBed.inject(FileManager);
    httpConstants = new HttpConstants();
    component._httpConstants = httpConstants;
    fixture.detectChanges();
  });

  describe('ngOnInit', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('regenerateSummary', () => {
    it('should regenerate document summary and update component state', () => {
      const mockResponse = {
        status: httpConstants.REQUEST_STATUS.CREATED_201.CODE,
        data: { summary: 'New Summary' },
      };
      spyOn(fileManagerService, 'regenerateSummary').and.returnValue(
        of(mockResponse)
      );

      component.documentSummary = true;
      component.document = {
        documentSummary: '',
        documentUrl: 'http://example.com',
      };

      component.regenerateSummary();

      expect(component.showSpinner).toBeFalse();
      expect(fileManagerService.regenerateSummary).toHaveBeenCalledWith(
        'http://example.com',
        undefined
      );
      setTimeout(() => {
        // Ensure the async call completes
        expect(component.document.documentSummary).toBe('New Summary');
        expect(component.showSpinner).toBeFalse();
      }, 0);
    });

    it('should handle error in regenerateSummary', () => {
      spyOn(fileManagerService, 'regenerateSummary').and.returnValue(
        throwError({})
      );

      component.documentSummary = true;
      component.document = {
        documentSummary: '',
        documentUrl: 'http://example.com',
      };

      component.regenerateSummary();

      expect(component.showSpinner).toBeFalsy();
      setTimeout(() => {
        expect(component.showSpinner).toBeFalse();
      }, 0);
    });
  });

  describe('saveSummary', () => {
    it('should call sendDocumentSummary when documentSummary is true', () => {
      spyOn(communicationService, 'sendDocumentSummary');
      spyOn(modalService, 'closeAll');

      component.documentSummary = true;
      component.document = {
        /* mock document */
      };

      component.saveSummary();

      expect(communicationService.sendDocumentSummary).toHaveBeenCalledWith(
        component.document
      );
      expect(modalService.closeAll).toHaveBeenCalled();
    });

    it('should call sendVideoSummary when videoSummary is true', () => {
      spyOn(communicationService, 'sendVideoSummary');
      spyOn(modalService, 'closeAll');

      component.videoSummary = true;
      component.saveSummary();

      expect(communicationService.sendVideoSummary).toHaveBeenCalledWith(
        component.videoData
      );
      expect(modalService.closeAll).toHaveBeenCalled();
    });

    it('should call sendArticleSummary when articleSummary is true', () => {
      spyOn(communicationService, 'sendArticleSummary');
      spyOn(modalService, 'closeAll');

      component.articleSummary = true;
      component.saveSummary();

      expect(communicationService.sendArticleSummary).toHaveBeenCalledWith(
        component.article
      );
      expect(modalService.closeAll).toHaveBeenCalled();
    });
  });
});
