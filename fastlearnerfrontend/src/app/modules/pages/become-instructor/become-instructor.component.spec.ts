import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BecomeInstructorComponent } from './become-instructor.component';
import { Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { Meta, Title } from '@angular/platform-browser';
import { PreviewVideoModalComponent } from '../../dynamic-modals/preview-video-modal/preview-video-modal.component';
import { of } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('BecomeInstructorComponent', () => {
  let component: BecomeInstructorComponent;
  let fixture: ComponentFixture<BecomeInstructorComponent>;
  let metaService: Meta;
  let titleService: Title;
  let modalService: NzModalService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BecomeInstructorComponent],
      providers: [
        { provide: Router, useValue: { navigate: jasmine.createSpy() } },
        { provide: NzModalService, useValue: { create: jasmine.createSpy() } },
        { provide: Meta, useClass: Meta },
        { provide: Title, useClass: Title },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(BecomeInstructorComponent);
    component = fixture.componentInstance;
    metaService = TestBed.inject(Meta);
    titleService = TestBed.inject(Title);
    modalService = TestBed.inject(NzModalService);
    router = TestBed.inject(Router);
  });

  it('should set title and meta tag on initialization', () => {
    spyOn(titleService, 'setTitle');
    spyOn(metaService, 'updateTag');

    component.ngOnInit();

    expect(titleService.setTitle).toHaveBeenCalledWith(
      'Become an Instructor at Fast Learner – Share Your Knowledge'
    );
    expect(metaService.updateTag).toHaveBeenCalledWith({
      name: 'description',
      content:
        'Become an instructor at Fast Learner. Share your expertise, design your courses, earn, and empower learners worldwide with AI-powered teaching tools.',
    });
  });

  it('should change text every 3 seconds', (done) => {
    component.ngOnInit();

    setTimeout(() => {
      expect(component.currentText).toBe('an Instructor'); // Check if text has changed
      done();
    }, 3100); // Wait slightly longer than the interval
  });

  it('should fade out text and then fade in the new text', () => {
    const element = document.createElement('div');
    element.id = 'transitionText';
    document.body.appendChild(element);

    component.currentText = 'Test';
    component.fadeOutText();

    setTimeout(() => {
      expect(element.style.opacity).toBe('1');
      component.fadeInText();
      expect(element.style.opacity).toBe('1');
      document.body.removeChild(element);
    }, 500);
  });

  it('should navigate to instructor welcome page', () => {
    const navigateSpy = router.navigate as jasmine.Spy;

    component.routeToInstructorWelcomePage();

    expect(navigateSpy).toHaveBeenCalledWith(['welcome-instructor']);
  });
});
