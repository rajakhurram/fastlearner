import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { ViewAllMap } from 'src/app/core/enums/course-status';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
})
export class FooterComponent implements OnInit {
  @ViewChild('mainContent', { static: false }) mainContent: ElementRef;
  constructor(
    private _router: Router,
    private route: ActivatedRoute,
    private _sharedService?: SharedService,
    private _messageService?: MessageService
  ) {}

  activeLink: string = '';
  subscribeEmail?: any;
  emptyEmail: boolean = false;
  emailValid?: any = false;
  viewAllMap = ViewAllMap;
  currentYear = new Date().getFullYear();

  ngOnInit(): void {
    this._router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.setActiveLink();
      });
  }

  setActiveLink(): void {
    const currentRoute = this.route.snapshot['_routerState'].url;
    if (currentRoute.includes('about-us')) {
      this.activeLink = 'about-us';
    } else if (currentRoute.includes('privacy-policy')) {
      this.activeLink = 'privacy-policy';
    } else if (currentRoute.includes('terms-and-conditions')) {
      this.activeLink = 'terms-and-conditions';
    } else if (currentRoute.includes('contact-us')) {
      this.activeLink = 'contact-us';
    } else if (currentRoute.includes('/student/courses')) {
      this.activeLink = 'courses';
    } else if (currentRoute.includes('become-instructor')) {
      this.activeLink = 'teach';
    } else if (currentRoute.includes('become-instructor')) {
      this.activeLink = 'become-instructor';
    } else {
      this.activeLink = '';
    }
  }
  routeToLandingPage() {
    this._router.navigate(['']);
  }
  routeToAboutUs() {
    this._router.navigate(['about-us']);
  }
  routeToPrivacyPolicy() {
    this._router.navigate(['privacy-policy']);
  }
  routeToTermsandConditions() {
    this._router.navigate(['terms-and-conditions']);
  }
  routeToCoursesList(selection?) {
    this._router.navigate(['/student/courses'], {
      queryParams: {
        selection: selection,
      },
    });
  }

  routeToInstagram() {
    window.open('https://www.instagram.com/fastlearner.lms/', '_blank');
  }
  routeToLinkedIn() {
    window.open('https://www.linkedin.com/company/fastlearner/', '_blank');
  }
  routeToTwitter() {
    window.open(
      'https://x.com/fastlearner_ai?s=11&t=Vt_WkfQUCv78CQwfkOBmGw',
      '_blank'
    );
  }
  routeToFacebook() {
    window.open('http://www.facebook.com/FastlearnerAI', '_blank');
  }
  routeToBlogs() {
    window.open('https://blog.fastlearner.ai/', '_blank');
  }
  routeToPressRelease() {
    window.open('https://blog.fastlearner.ai/press-release/', '_blank');
  }

  scrollToCoursesSection() {
    const coursesSection = document.getElementById('courses-section');
    if (coursesSection) {
      coursesSection.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
        inline: 'nearest',
      });
    }
  }

  routeToInstructorWelcomePage() {
    this._router.navigate(['become-instructor']);
  }
  routeToContactUs() {
    this._router.navigate(['contact-us']);
  }

  routeToVinncorp() {
    window.open('https://vinncorp.com/', '_blank');
  }

  validateEmail(event?: any) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const email = event.target.value;

    if (email.length > 255) {
      this.emailValid = false;
    } else {
      const valid = emailPattern.test(email);
      this.emailValid = valid;
    }
  }

  subscribeNewsLetter() {
    if (!this.subscribeEmail) {
      this.emptyEmail = true;
      return;
    }
    if (this.emailValid) {
      this._sharedService.subscribeNewsLetter(this.subscribeEmail).subscribe({
        next: (response: any) => {
          this.subscribeEmail = '';
          this.emptyEmail = false;
          this._messageService.success(response?.message);
        },
        error: (error: any) => {
          this.subscribeEmail = '';
          this.emptyEmail = false;
          this._messageService.error(error?.error?.message);
        },
      });
    }
  }
}
