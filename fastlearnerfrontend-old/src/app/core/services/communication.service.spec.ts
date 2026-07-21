import { TestBed } from '@angular/core/testing';
import { CommunicationService } from './communication.service';
import { BehaviorSubject } from 'rxjs';

describe('CommunicationService', () => {
  let service: CommunicationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CommunicationService]
    });
    service = TestBed.inject(CommunicationService);
  });

  it('should create the service', () => {
    expect(service).toBeTruthy();
  });

  it('should emit document summary', (done) => {
    const testDocument = 'Test document summary';
    service.documentSummary$.subscribe(value => {
      expect(value).toBe(testDocument);
      done();
    });
    service.sendDocumentSummary(testDocument);
  });

  it('should emit article summary', (done) => {
    const testArticle = 'Test article summary';
    service.articleSummary$.subscribe(value => {
      expect(value).toBe(testArticle);
      done();
    });
    service.sendArticleSummary(testArticle);
  });

  it('should emit video transcript', (done) => {
    const testVideoTranscript = 'Test video transcript';
    service.videoTranscript$.subscribe(value => {
      expect(value).toBe(testVideoTranscript);
      done();
    });
    service.sendVideoTranscript(testVideoTranscript);
  });

  it('should emit notification data', (done) => {
    service.notificationData$.subscribe(value => {
      expect(value).toBeNull();
      done();
    });
    service.showNotificationData();
  });

  it('should emit notification count data', (done) => {
    service.notificationCountData$.subscribe(value => {
      expect(value).toBeNull();
      done();
    });
    service.showNotificationCountData();
  });

  it('should emit remove emitter signal', (done) => {
    service.removeEmitterData$.subscribe(value => {
      expect(value).toBeNull();
      done();
    });
    service.removeEmitter();
  });

  it('should emit close completion signal', (done) => {
    service.closeCompletionData$.subscribe(value => {
      expect(value).toBeNull();
      done();
    });
    service.closeCourseCompletionModal();
  });
});
