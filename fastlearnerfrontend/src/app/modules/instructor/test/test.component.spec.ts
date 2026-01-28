import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestComponent } from './test.component';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { ActivatedRoute} from '@angular/router';
import { of } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('TestComponent', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let communicationService: jasmine.SpyObj<CommunicationService>;
  let route: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(async () => {
    communicationService = jasmine.createSpyObj<CommunicationService>(
      'CommunicationService',
      ['instructorTabChange',]  // List ALL methods you need to mock
    );

    route = jasmine.createSpyObj<ActivatedRoute>('ActivatedRoute', [], {
      queryParams: of({
        id: "123",
      })
    })

    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [TestComponent],
      providers: [
        { provide: ActivatedRoute, useValue: route },
        { provide: CommunicationService, useValue: communicationService },
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
