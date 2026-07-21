import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompletionModalComponent } from './completion-modal.component';
import { NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { Subject } from 'rxjs';
import { CommunicationService } from 'src/app/core/services/communication.service';

class MockCommunicationService {
  closeCompletionSubject = new Subject<void>();
  closeCourseCompletionModal() {
    this.closeCompletionSubject.next(null);
  }
}

describe('CompletionModalComponent', () => {
  let component: CompletionModalComponent;
  let fixture: ComponentFixture<CompletionModalComponent>;
  let mockCommunicationService: MockCommunicationService;

  beforeEach(async () => {
    mockCommunicationService = new MockCommunicationService();

    await TestBed.configureTestingModule({
      declarations: [CompletionModalComponent],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        { provide: NzModalService, useValue: {} },
        { provide: CommunicationService, useValue: mockCommunicationService } // Use mock service
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CompletionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('done', () => {
    it('should call closeCourseCompletionModal', () => {
      // Spy on the next method
      spyOn(mockCommunicationService.closeCompletionSubject, 'next');
      
      // Call the method
      component.done();
      
      // Assert that next was called
      expect(mockCommunicationService.closeCompletionSubject.next).toHaveBeenCalledWith(null);
    });
  });

  
});
