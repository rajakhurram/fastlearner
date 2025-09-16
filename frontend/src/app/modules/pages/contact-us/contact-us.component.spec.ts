import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ContactUsComponent } from './contact-us.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { Meta, Title } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('ContactUsComponent', () => {
  let component: ContactUsComponent;
  let fixture: ComponentFixture<ContactUsComponent>;
  let titleService: Title;
  let metaService: Meta;
  let messageService: jasmine.SpyObj<MessageService>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['contactUs']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ContactUsComponent],
      providers: [
        FormBuilder,
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Title, useValue: { setTitle: jasmine.createSpy() } },
        { provide: Meta, useValue: { updateTag: jasmine.createSpy() } },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ContactUsComponent);
    component = fixture.componentInstance;
    titleService = TestBed.inject(Title);
    metaService = TestBed.inject(Meta);
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    fixture.detectChanges(); // Ensure form initialization is completed
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set page title and meta tags on initialization', () => {
    component.ngOnInit();
    expect(titleService.setTitle).toHaveBeenCalledWith(
      'Contact Us | Fastlearner.ai'
    );
    expect(metaService.updateTag).toHaveBeenCalledWith({
      name: 'Contact us',
      content: 'Contact Us page of Fastlearner.ai',
    });
  });

  it('should initialize the contact form with default values', () => {
    component.ngOnInit();
    expect(component.contactUsForm).toBeTruthy();
    expect(component.contactUsForm.controls['fullName']).toBeDefined();
    expect(component.contactUsForm.controls['email']).toBeDefined();
    expect(component.contactUsForm.controls['phoneNumber']).toBeDefined();
    expect(component.contactUsForm.controls['description']).toBeDefined();
  });

  it('should show an error message if the form is invalid or email is not valid', () => {
    component.contactUsForm.controls['fullName'].setValue('John Doe');
    component.contactUsForm.controls['email'].setValue('invalid-email');
    component.contactUsForm.controls['phoneNumber'].setValue('1234567890');
    component.contactUsForm.controls['description'].setValue(
      'Description text'
    );
    component.emailValid = false; // Simulate invalid email

    component.onFormSubmit({ preventDefault: jasmine.createSpy() });

    expect(messageService.error).toHaveBeenCalledWith('Complete all fields before submitting');
  });

  it('should call AuthService contactUs method on valid form submission', () => {
    component.contactUsForm.controls['fullName'].setValue('John Doe');
    component.contactUsForm.controls['email'].setValue('john.doe@example.com');
    component.contactUsForm.controls['phoneNumber'].setValue('1234567890');
    component.contactUsForm.controls['description'].setValue(
      'Description text'
    );
    component.emailValid = true;

    authService.contactUs.and.returnValue(of({})); // Mock successful response

    component.onFormSubmit({ preventDefault: jasmine.createSpy() });

    expect(authService.contactUs).toHaveBeenCalledWith(
      component.contactUsForm.value
    );
    expect(messageService.success).toHaveBeenCalledWith(
      'Form has been submitted'
    );
  });

  it('should handle error from AuthService contactUs method', () => {
    component.contactUsForm.controls['fullName'].setValue('John Doe');
    component.contactUsForm.controls['email'].setValue('john.doe@example.com');
    component.contactUsForm.controls['phoneNumber'].setValue('1234567890');
    component.contactUsForm.controls['description'].setValue(
      'Description text'
    );
    component.emailValid = true;

    const errorResponse = new Error('Submission failed');
    authService.contactUs.and.returnValue(throwError(() => errorResponse));

    component.onFormSubmit({ preventDefault: jasmine.createSpy() });

    expect(messageService.error).toHaveBeenCalled();
  });

  it('should set emailValid to true for a valid email', () => {
    const event = { target: { value: 'test@example.com' } };

    component.validateEmail(event);

    expect(component.emailValid).toBeTrue();
  });

  it('should set emailValid to false for an invalid email', () => {
    const event = { target: { value: 'invalid-email' } };

    component.validateEmail(event);

    expect(component.emailValid).toBeFalse();
  });

  it('should truncate description to 2000 characters', () => {
    const longDescription = 'a'.repeat(2500); // Create a string with 2500 characters
    component.contactUsForm.controls['description'].setValue(longDescription);

    component.validateDescription();

    const truncatedDescription =
      component.contactUsForm.controls['description'].value;
    expect(truncatedDescription.length).toBe(2000);
  });

  it('should open LinkedIn in a new tab', () => {
    spyOn(window, 'open');

    component.routeToLinkedIn();

    expect(window.open).toHaveBeenCalledWith(
      'https://www.linkedin.com/company/fastlearner/',
      '_blank'
    );
  });

  it('should open Facebook in a new tab', () => {
    spyOn(window, 'open');

    component.routeToFacebook();

    expect(window.open).toHaveBeenCalledWith(
      'http://www.facebook.com/FastlearnerAI',
      '_blank'
    );
  });

  it('should allow numeric input', () => {
    const event = new KeyboardEvent('keydown', { key: '5' });

    expect(component.onlyNumberAllowed(event)).toBeTrue();
  });

  it('should disallow non-numeric input', () => {
    const event = new KeyboardEvent('keydown', { key: 'a' });

    expect(component.onlyNumberAllowed(event)).toBeFalse();
  });
});
