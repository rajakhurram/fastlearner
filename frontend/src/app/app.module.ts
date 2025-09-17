import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AntDesignModule } from './ui-library/ant-design/ant-design.module';
import { SharedModule } from './modules/shared/shared.module';
import {
  FacebookLoginProvider,
  GoogleLoginProvider,
  GoogleSigninButtonModule,
  SocialAuthServiceConfig,
  SocialLoginModule,
} from '@abacritt/angularx-social-login';
import { ConfirmationModalComponent } from './modules/dynamic-modals/confirmation-modal/confirmation-modal.component';
import { RatingModalComponent } from './modules/dynamic-modals/rating-modal/rating-modal.component';
import { AboutUsComponent } from './modules/pages/about-us/about-us.component';
import { LottieModule } from 'ngx-lottie';
import player from 'lottie-web';
import { ContactUsComponent } from './modules/pages/contact-us/contact-us.component';
import { environment } from 'src/environments/environment.development';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { RequestInterceptor } from './core/interceptors/request.interceptor';
import { NgxUiLoaderHttpModule, NgxUiLoaderModule } from 'ngx-ui-loader';
import { NgxLoaderCongif } from './core/config/loader.config';
import { DealComponent } from './modules/dynamic-modals/deal-modal/deal.component';
import { ShareModalComponent } from './modules/dynamic-modals/share-modal/share-modal.component';
import { ChatModalComponent } from './modules/dynamic-modals/chat-modal/chat-modal.component';
import { ReviewModalComponent } from './modules/dynamic-modals/review-modal/review-modal.component';
import { SummaryModalComponent } from './modules/dynamic-modals/summary-modal/summary-modal.component';
import { TranscriptModalComponent } from './modules/dynamic-modals/transcript-modal/transcript-modal.component';
import { SubmissionModalComponent } from './modules/dynamic-modals/submission-modal/submission-modal.component';
import { CompletionModalComponent } from './modules/dynamic-modals/completion-modal/completion-modal.component';
import { NzResultModule } from 'ng-zorro-antd/result';
import { PaymentModalComponent } from './modules/dynamic-modals/payment-modal/payment-modal.component';
import { OtpModalComponent } from './modules/dynamic-modals/otp-modal/otp-modal.component';
import { NgOtpInputModule } from 'ng-otp-input';
import { LocationStrategy, PathLocationStrategy } from '@angular/common';
import { DeletionModalComponent } from './modules/dynamic-modals/deletion-modal/deletion-modal.component';
import { ImageUploaderModalComponent } from './modules/dynamic-modals/image-uploader-modal/image-uploader-modal.component';
import { ImageCropperModule } from 'ngx-image-cropper';
import { SharedService } from './core/services/shared.service';
import { ProfileUploaderModalComponent } from './modules/dynamic-modals/profile-uploader-modal/profile-uploader-modal.component';
import { GoogleTagManagerModule } from 'angular-google-tag-manager';
import { GtagModule } from 'angular-gtag';
import { CourseDraftModalComponent } from './modules/dynamic-modals/course-draft-modal/course-draft-modal.component';
import { PreviewVideoModalComponent } from './modules/dynamic-modals/preview-video-modal/preview-video-modal.component';
import { MaintenancePageComponent } from './modules/pages/maintenance-page/maintenance-page.component';
import { WithdrawModalComponent } from './modules/dynamic-modals/withdraw-modal/withdraw-modal.component';
import { AffiliateModalComponent } from './modules/dynamic-modals/affiliate-modal/affiliate-modal.component';
import {
  googleTagId,
  googleTrackingId,
} from './core/constants/http.constants';
import { AssignCourseModalComponent } from './modules/dynamic-modals/assign-course-modal/assign-course-modal.component';
import { FastlearnerSigninComponent } from './fastlearner-signin/fastlearner-signin.component';
import { FastlearnerLoginComponent } from './fastlearner-login/fastlearner-login.component';
import { FastlearnerSignupComponent } from './fastlearner-signup/fastlearner-signup.component';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FormsModule } from '@angular/forms';
import { CookieService } from 'ngx-cookie-service';

export function playerFactory() {
  return player;
}

@NgModule({
  declarations: [
    AppComponent,
    AboutUsComponent,
    ConfirmationModalComponent,
    RatingModalComponent,
    ContactUsComponent,
    DealComponent,
    ShareModalComponent,
    ChatModalComponent,
    ReviewModalComponent,
    SummaryModalComponent,
    SubmissionModalComponent,
    TranscriptModalComponent,
    CompletionModalComponent,
    PaymentModalComponent,
    OtpModalComponent,
    DeletionModalComponent,
    ImageUploaderModalComponent,
    ProfileUploaderModalComponent,
    CourseDraftModalComponent,
    PreviewVideoModalComponent,
    MaintenancePageComponent,
    WithdrawModalComponent,
    AffiliateModalComponent,
    AssignCourseModalComponent,
    FastlearnerSigninComponent,
    FastlearnerLoginComponent,
    FastlearnerSignupComponent,
  ],
  imports: [
    FormsModule,
    BrowserModule,
    ImageCropperModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    AntDesignModule,
    SharedModule,
    SocialLoginModule,
    GoogleSigninButtonModule,
    NgOtpInputModule,
    NzTagModule,
    NzIconModule,
    // NzResultModule,
    GtagModule.forRoot({
      trackingId: googleTrackingId,
      trackPageviews: true,
    }),

    GoogleTagManagerModule.forRoot({
      id: googleTagId,
    }),
    GtagModule.forRoot({
      trackingId: googleTrackingId,
      trackPageviews: true,
    }),
    GoogleTagManagerModule.forRoot({
      id: googleTagId,
    }),
    LottieModule.forRoot({ player: playerFactory }),
    NgxUiLoaderHttpModule.forRoot({
      showForeground: true,
      exclude: [
        environment.baseUrl + `topic/course/`,
        environment.baseUrl + `chat/`,
        environment.baseUrl + `favourite-course/`,
        environment.baseUrl + `topic-notes/`,
        environment.baseUrl + `question/`,
        environment.baseUrl + `course-review`,
        environment.baseUrl + `user-course-progress/`,
        environment.baseUrl + `section-review/`,
        environment.baseUrl + 'ai-generator/',
        environment.baseUrl + 'uploader/',
        environment.baseUrl + `alternate-section/`,
        environment.baseUrl + `course/course-by-category`,
        environment.baseUrl + `course-category`,
        environment.baseUrl + `youtube-video/download`,
        environment.baseUrl + `youtube-video/duration`,
        environment.baseUrl + `course/course-by-teacher`,
        environment.baseUrl + `course/course-status`,
        environment.baseUrl + `answer/`,
        `${environment.baseUrl}course/autocomplete`,
        `${environment.baseUrl}user-profile/update`,
        `${environment.baseUrl}newsletter-subscription/subscribe`,
        `${environment.baseUrl}topic/summary/`,
        `${environment.baseUrl}chat-history/`,
        `${environment.baseUrl}topic/course/`,
        `${environment.baseUrl}home-page/trending-courses`,
        `${environment.baseUrl}home-page/premium-courses`,
        `${environment.baseUrl}home-page/new-courses`,
        `${environment.baseUrl}home-page/top-instructor`,
        `${environment.baseUrl}home-page/free-courses`,
        `${environment.baseUrl}course/search-by-filter`,
        `${environment.baseUrl}course/unique-course-title`,
        `${environment.baseUrl}enrollment/`,
        `${environment.baseUrl}course-review/like/`,
        `${environment.baseUrl}course/get/`,
        `${environment.baseUrl}course/unique-course-url`,
        `${environment.baseUrl}courspremiue/get-related-courses`,
        `${environment.baseUrl}affiliate-course/by-course`,
        environment.baseUrl + `watch-time/`,
        environment.basePath +`copilot/chat/`,
      ],
    }),
    NgxUiLoaderModule.forRoot(NgxLoaderCongif.masterLoaderConfig),
  ],
  providers: [
    {
      provide: 'SocialAuthServiceConfig',
      useValue: {
        autoLogin: false,
        providers: [
          {
            id: GoogleLoginProvider.PROVIDER_ID,
            // oneTapEnabled: true,
            provider: new GoogleLoginProvider(environment.googleClientId),
          },
        ],
        onError: (err) => {},
      } as SocialAuthServiceConfig,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: RequestInterceptor,
      multi: true,
    },
    { provide: LocationStrategy, useClass: PathLocationStrategy },
    CookieService
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
  constructor(private sharedService: SharedService) {
    this.sharedService.initialize();
  }
}
