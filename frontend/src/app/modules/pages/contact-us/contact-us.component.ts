import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { AuthService } from 'src/app/core/services/auth.service';
import { MessageService } from 'src/app/core/services/message.service';
import { Meta, Title } from '@angular/platform-browser';
@Component({
  selector: 'app-contact-us',
  templateUrl: './contact-us.component.html',
  styleUrls: ['./contact-us.component.scss'],
})
export class ContactUsComponent implements OnInit {
  contactUsForm: FormGroup;
  emailValid?: any = true;
  isSubmit?: any = false;
  constructor(
    private fb: FormBuilder,
    private _messageService: MessageService,
    private _authService: AuthService,
    private metaService: Meta,
    private titleService: Title
  ) {}

  ngOnInit(): void {
    this.titleService.setTitle('Get in Touch with Us | Fast Learner'); // Set the title of the page
    this.metaService.updateTag({
      name: 'description',
      content: `Have questions or need assistance? Contact Fast Learner today for support, queries, and collaboration. We're here to help you on your learning journey.`,
    });
    this.initializeForm();
  }

  initializeForm() {
    this.contactUsForm = this.fb.group({
      fullName: [null, [Validators.required, Validators.maxLength(50)]],
      email: [null, [Validators.required]],
      phoneNumber: [null, Validators.required],
      description: [null, [Validators.required]],
    });
  }

  onFormSubmit(event) {
    this.isSubmit = true;
    setTimeout(() => {
      this.isSubmit = false;
    }, 2000);
    if (this.contactUsForm.invalid || !this.emailValid) {
      this._messageService.error('Complete all fields before submitting');
      return;
    }
  
    this._authService.contactUs(this.contactUsForm.value).subscribe({
      next: (response: any) => {
        this._messageService.success('Form has been submitted');
      },
      error: (error: any) => {
        this._messageService.error('Error in submitting form');
      },
    });
  }

  routeToLinkedIn() {
    window.open('https://www.linkedin.com/company/fastlearner/', '_blank');
  }
  routeToFacebook() {
    window.open('http://www.facebook.com/FastlearnerAI', '_blank');
  }

  validateEmail(event?: any) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const valid = emailPattern.test(event.target.value);
    valid == false ? (this.emailValid = false) : (this.emailValid = true);
  }

  validateDescription($event?: any) {
    const maxLength = 2000;
    let description = this.contactUsForm.get('description').value;

    if (description && description.length > maxLength) {
      description = description.substring(0, maxLength);
      this.contactUsForm.get('description').setValue(description);
    }
  }

  onlyNumberAllowed(event: KeyboardEvent): any {
    if (Number(event.key) >= 0 || Number(event.key) <= 9) {
      return true;
    }
    return false;
  }
}
