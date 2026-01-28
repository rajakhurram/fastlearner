import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranscriptModalComponent } from './transcript-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { FormsModule } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('TranscriptModalComponent', () => {
  let component: TranscriptModalComponent;
  let fixture: ComponentFixture<TranscriptModalComponent>;
  let modalService: NzModalService;
  let communicationService: CommunicationService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TranscriptModalComponent ],
      providers: [
        NzModalService,
        CommunicationService,
        FormsModule
      ],
      schemas : [NO_ERRORS_SCHEMA , CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TranscriptModalComponent);
    component = fixture.componentInstance;
    modalService = TestBed.inject(NzModalService);
    communicationService = TestBed.inject(CommunicationService);
    fixture.detectChanges();
  });

  describe('ngOnInit', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('saveTranscript', () => {
    it('should call sendVideoTranscript and close the modal', () => {
      const mockVideoData = { id: 1, transcript: 'Mock transcript' };
      spyOn(communicationService, 'sendVideoTranscript').and.returnValue(); // Adjusted
      spyOn(modalService, 'closeAll');

      component.videoData = mockVideoData;
      component.saveTranscript();

      expect(communicationService.sendVideoTranscript).toHaveBeenCalledWith(mockVideoData);
      expect(modalService.closeAll).toHaveBeenCalled();
    });
  });
});
