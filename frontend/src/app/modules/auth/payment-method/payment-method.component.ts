import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AppConstants } from 'src/app/core/constants/app.constants';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseType } from 'src/app/core/enums/course-status';
import { PaymentProfile } from 'src/app/core/models/payment-profile.model';
import { Subscription } from 'src/app/core/models/subscription.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { CookiesService } from 'src/app/core/services/cookie.service';
import { CourseService } from 'src/app/core/services/course.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';

@Component({
  selector: 'app-payment-method',
  templateUrl: './payment-method.component.html',
  styleUrls: ['./payment-method.component.scss'],
})
export class PaymentMethodComponent implements OnInit {
  paymentMessage: string = 'Please choose a payment method and subscribe to a plan';
  subscriptionId?: any;
  paymentProfile?: PaymentProfile = {};
  subscription?: Subscription = {};
  subscriptionData?: any = {};
  _httpConstants: HttpConstants = new HttpConstants();
  _appConstants: AppConstants = new AppConstants();
  lastKeyPressTime: number = 0;
  coursePrice: number = 0;
  courseUrl: string = '';
  courseId: number = 0;
  isPlanSelected?: boolean = false;
  isPurchaseMode?: boolean = false;
  courseDetails: any;
  affiliateUUID?: any;
  sessionId?: any;
  isMobile?: any;
  discountApplied: boolean = false;
  appliedCode: string = '';
  promoCode: string = '';
  isApplyingPromo: boolean = false;
  discountedPrice: any;
  discountInDollars: number;

  constructor(
    private _activatedRoute: ActivatedRoute,
    private _subscriptionService: SubscriptionService,
    private _message: NzMessageService,
    private _router: Router,
    private _authService: AuthService,
    private _cacheService: CacheService,
    private _courseService: CourseService,
    private _communicationService: CommunicationService,
    private _cookiesService: CookiesService
  ) {}

  ngOnInit(): void {
    this.sessionId = this._activatedRoute.snapshot.queryParams['sessionId'];
    this.isMobile = this._activatedRoute.snapshot.queryParams['isMobile'];


    if (this.sessionId && this.isMobile === 'true') {
      this.getTokenAgainstSessionId();
    } else {
      if (this._activatedRoute.snapshot.queryParams['premium']) {
        this.isPurchaseMode = true;
        this.paymentMessage = "Please choose a payment method to buy a course";
        this.courseId = this._activatedRoute.snapshot.queryParams['courseId'];
        this.coursePrice = this._activatedRoute.snapshot.queryParams['price'];
        this.courseUrl = this._activatedRoute.snapshot.queryParams['courseUrl'];
        this.affiliateUUID = this._activatedRoute.snapshot.queryParams['affiliate'];
        this.getCourseDetails(this.courseId);
        return;
      }
      this.subscriptionId =
        this._activatedRoute.snapshot.queryParams['subscriptionId'];
      this.getSavedPaymentProfile();
      this.getSubscriptionById();
    }
  }

  getTokenAgainstSessionId() {
    this._subscriptionService
      .getTokenAgainstSessionId(this.sessionId)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this._communicationService.navbarAndFooterStateChange(false);
            this.saveResponseInCache(response?.data?.tokenResponse);
            this.subscriptionId = response?.data?.subscriptionId;
            this.courseId = response?.data?.courseId;
            this.coursePrice = response?.data?.coursePrice;
            this.courseUrl = response?.data?.courseUrl;
            if (this.courseId) {
              this.isPurchaseMode = true;
            } else {
              this.getSubscriptionById();
              this.getSavedPaymentProfile();
            }
          }
        },
        error: (error: any) => {},
      });
  }

  saveResponseInCache(response: any) {
    this._cacheService.saveInCache('token', response?.token);
    this._cacheService.saveInCache('refresh_token', response?.refreshToken);
    this._cookiesService.setToken(response?.token);
    this._cookiesService.setRefreshToken(response?.refreshToken);
    const expiryTimeInSeconds =
      Math.floor(Date.now() / 1000) + response?.expiredInSec;
    this._cacheService.saveInCache('expiresIn', expiryTimeInSeconds);
    this._cacheService.saveInCache(
      'loggedInUserDetails',
      JSON.stringify(response)
    );
    this._cacheService.saveInCache('isLoggedIn', 'true');
    this._authService.startTokenTimer();
  }

  getToken(paymentDataObject) {
    let paymentData = {
      cardNumber: paymentDataObject?.cardNumber,
      expDate: paymentDataObject?.date,
      cvv: paymentDataObject?.cvv,
    };

    const cardData = {
      cardNumber: paymentData.cardNumber.toString(),
      month: paymentData.expDate.substring(0, 2),
      year: paymentData.expDate.substring(2),
      cardCode: paymentData.cvv,
    };

    //For Test
    const authData = {
      clientKey:
        '8UAyfxN6g5yJmeUz9p68TvSYupgFUpsQyHz3Jk3Rv9LmV2qE2ZkM4wd42uWTa47j',
      apiLoginID: '4sTcMd6dy9g',
    };

    //For Production
    // const authData = {
    //   clientKey:
    //     '34kks6SRFKgV3M9MTWe4RDkf235u22BHbrwaB8RuMupJRZH9pFbRX7F3ubzY9w7w',
    //   apiLoginID: '4kLujL6376mV',
    // };

    (window as any).Accept.dispatchData(
      { authData, cardData },
      (response: any) => this.handleResponse(response)
    );
  }

  getCourseDetails(courseId: number) {
    this._courseService?.getCourseDetails(courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseDetails = response?.data;

          if (this.courseDetails.isAlreadyBought) {
            this._router.navigate(
              ['student/course-content', this.courseUrl],
              {}
            );
          }
        }
      },
      error: (error: any) => {},
    });
  }

  handleResponse(response: any) {
    if (response.messages.resultCode === 'Ok') {
      const token = response.opaqueData.dataValue;
      this.sendPaymentToken(token);
    } else {
      console.error('Tokenization failed:', response);
      this._message.error(response?.message);
    }
  }
  sendPaymentToken(token: string) {
    const paymentDetails = {
      courseId: parseInt(this.courseId.toString()),
      affiliateUUID: this.affiliateUUID,
      opaqueData: token,
      coupon : this.discountApplied ? this.promoCode : null
    };
    this._subscriptionService.courseCheckout(paymentDetails).subscribe(
      (res) => {
        if(this.sessionId && this.isMobile === 'true'){
          this.onPaymentSuccess();
        }else {
          this._router.navigate(['student/course-content', this.courseUrl], {});
        }
        
      },
      (error) => {
        this._message.error(error?.error?.message);
      }
    );
  }
  ngOnDestroy(): void {
    if (!this.isPlanSelected && !this._authService.isSubscribed()) {
      let payLoad = {
        paypalPlanId: null,
        subscriptionId: 1,
      };
      this.subscribeToPlan(payLoad, 'Free Plan');
    }
  }

  allowNumericDigitsOnlyOnKeyUp(e: any) {
    const charCode = e.which ? e.which : e.keyCode;

    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }
    return true;
  }

  onExpiryDateKeyDown(event: any): void {
    const allowedKeys = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];
    let inputValue = event.target.value;

    if (Number(inputValue.slice(0, 2)) > 12) {
      event.preventDefault();
      this.paymentProfile.date = inputValue.slice(0, 1);
    } else if (
      inputValue.length == 5 &&
      Number(inputValue.replace(/\D/g, '').slice(2, 4)) <
        this.getCurrentYearTwoDigits()
    ) {
      this.paymentProfile.date = inputValue.slice(0, 2);
    } else {
      const numericValue = inputValue.replace(/\D/g, '');
      const formattedValue = this.formatExpiryDate(numericValue);

      if (inputValue !== formattedValue) {
        this.paymentProfile.date = formattedValue;
      }
    }
  }

  formatExpiryDate(value: string): string {
    if (value.length <= 2) {
      return value;
    } else {
      const formattedMonth = value.slice(0, 2);
      const formattedYear = value.slice(2, 4);
      return `${formattedMonth}/${formattedYear}`;
    }
  }

  getCurrentYearTwoDigits(): any {
    const fullYear = new Date().getFullYear();
    return Number(fullYear.toString().slice(-2));
  }

  onPaste(event: any): void {
    event.preventDefault();
  }

  getSavedPaymentProfile() {
    this._subscriptionService?.getSavedPaymentProfile()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.paymentProfile = response.data;
          this.paymentProfile.date =
            this.paymentProfile.expiryMonth +
            '/' +
            this.paymentProfile.expiryYear;
          this.paymentProfile.cvv = '';
        }
      },
      error: (error: any) => {
        // this._message.error(error?.error.message);
      },
    });
  }

  addDiscount() {
    this.promoCode = this.promoCode.trim();
  
    if (this.isApplyingPromo) return;
  
    if (!this.promoCode || this.promoCode.length < 6 || this.promoCode.length > 12) {
      this._message.remove();
      this._message.error('Please enter a valid promo code (6–12 characters)');
      return;
    }
  
    this.isApplyingPromo = true;
  
    const queryParams = this._activatedRoute.snapshot.queryParams;

    let couponType = '';
    let id: number | null = null;
    let originalPrice = 0;

  if (queryParams['subscriptionId']) {
    couponType = 'SUBSCRIPTION';
    id = +queryParams['subscriptionId'];
    originalPrice = this.subscriptionData?.price || 0;
  } else if (queryParams['courseId']) {
    couponType = 'PREMIUM';
    id = +queryParams['courseId'];
    originalPrice = this.coursePrice || 0;
  }else if(this.sessionId && this.isMobile === 'true' && this.courseId){
    couponType = 'PREMIUM';
    id = this.courseId;
    originalPrice = this.coursePrice || 0;
  }else if(this.sessionId && this.isMobile === 'true' && this.subscriptionId){
    couponType = 'SUBSCRIPTION';
    id = this.subscriptionId;
    originalPrice = this.subscriptionData?.price || 0;
  } else {
    this.isApplyingPromo = false;
    this._message.error('No valid course or subscription selected');
    return;
  }

  this._subscriptionService.getDisscount(
    this.promoCode,
    couponType,
    id
  ).subscribe({
      next: (response: any) => {
        this.isApplyingPromo = false;
  
        if (response?.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
          const discountPercent = response.data?.discount || 0;          
  
          const discountInDollars = (originalPrice * discountPercent) / 100;
          const discountedPrice = originalPrice - discountInDollars;
  
          this.discountApplied = true;
          this.appliedCode = this.promoCode;
  
          this.discountInDollars = discountInDollars;
          this.discountedPrice = discountedPrice;
  
          this._message.success(
            `Promo code applied: You saved $${discountInDollars.toFixed(2)}!`
          );
        }
      },
      error: (error: any) => {
        this.isApplyingPromo = false;
        this._message.remove();
        this._message.error(error?.error?.message || 'Failed to apply promo code');
      },
    });
  }

  addSubscription() {
    if (this.validateData()) {
      this.paymentProfile.expiryMonth =
        this.paymentProfile?.date?.split('/')[0];
      this.paymentProfile.expiryYear = this.paymentProfile?.date?.split('/')[1];
      this.paymentProfile.isSave =
        this.paymentProfile.isSave == null
          ? (this.paymentProfile.isSave = false)
          : this.paymentProfile.isSave;
      this.subscription.subscriptionId = this.subscriptionId;
      this.subscription.coupon = this.discountApplied ? this.promoCode : null;
      if (typeof this.paymentProfile.cardNumber == 'string') {
        const cardNumberAsInteger = parseInt(
          this.paymentProfile.cardNumber.replace(/\D/g, ''),
          10
        );
        this.paymentProfile.cardNumber = cardNumberAsInteger;
      } else {
        this.paymentProfile.cardNumber = this.paymentProfile.cardNumber;
      }

      this.subscription.paymentDetail = this.paymentProfile;
      if (this.isPurchaseMode) {
        this.getToken(this.paymentProfile);
      } else {
        this._subscriptionService.addSubscription(this.subscription).subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {

              this._subscriptionService.loadSubscriptionPermissions();

              if(this.sessionId && this.isMobile === 'true'){
                this.onPaymentSuccess();
              }else {
                this._subscriptionService.updateUserSubscriptionCheck(true);
                this.isPlanSelected = true;
                this.paymentProfile = response.data;
                this._message.success(response?.data);
                const redirectUrl =
                  this._cacheService.getDataFromCache('redirectUrl');
                if (redirectUrl?.includes('payment-method')) {
                  this._router.navigate(['subscription']);
                } else if (!redirectUrl?.includes('payment-method')) {
                  this._cacheService.removeFromCache('redirectUrl');
                  this._router.navigateByUrl(redirectUrl);
                } else {
                  this._router.navigate(['subscription']);
                }
              }
            }
          },
          error: (error: any) => {
            this._message.error(error?.error?.message);
            this.isPlanSelected = false;
            if(this.sessionId && this.isMobile === 'true'){
              this.onPaymentFailure(error);
            }
          },
        });
      }
    } else {
      this._message.remove(); 
      this._message.error('Please fill the fields');
    }
  }

  notifyFlutter(message: string): void {
    if ((window as any).PaymentChannel) {
      (window as any).PaymentChannel.postMessage(message);
    } else {
      console.error('PaymentChannel not available');
    }
  }

  onPaymentSuccess(): void {
    this.notifyFlutter('success');
  }

  onPaymentFailure(error?: any): void {
    this.notifyFlutter(error);
  }

  getSubscriptionById() {
    this._subscriptionService
      ?.getSubscriptionById(this.subscriptionId)
      ?.subscribe({
        next: (response: any) => {
          if (response) {
            this.subscriptionData = response.data;
          }
        },
        error: (error: any) => {
          // this._message.error(error?.error.data);
        },
      });
  }

  validateData() {
    if (
      this.paymentProfile.firstName != '' &&
      this.paymentProfile.lastName != '' &&
      this.paymentProfile.firstName?.length <= 50 &&
      this.paymentProfile.lastName?.length <= 50 &&
      this.paymentProfile.cardNumber != '' &&
      this.paymentProfile.date.split('/')[0] != '' &&
      this.paymentProfile.date.split('/')[0].length == 2 &&
      this.paymentProfile.date.split('/')[1].length == 2 &&
      this.paymentProfile.date.split('/')[1] != '' &&
      this.paymentProfile.cvv != ''
    ) {
      return true;
    }
    return false;
  }

  subscribeToPlan(payLoad: any, subscriptionPlanType: any) {
    this._authService?.createSubscription(payLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.BAD_REQUEST_400.CODE
        ) {
          this._subscriptionService.updateUserSubscriptionCheck(true);
          const redirectUrl =
            this._cacheService.getDataFromCache('redirectUrl');
          if (redirectUrl) {
            this._cacheService.removeFromCache('redirectUrl');
            this._router.navigateByUrl(redirectUrl);
          } else {
            this._router.navigate(['student']);
          }
          // this._message.error(error?.error?.message);
        }
      },
    });
  }

  checkInput(event: Event): void {
    let cardNumber = this.paymentProfile?.cardNumber?.replace(/\s+/g, '');
    cardNumber = cardNumber?.replace(/(.{4})/g, '$1 ');
    this.paymentProfile.cardNumber = cardNumber?.trim();
  }

  allowOnlyLetters(event: KeyboardEvent): void {
    const char = String.fromCharCode(event.keyCode || event.which);
    const pattern = /^[a-zA-Z\s\-']$/;
  
    if (!pattern.test(char)) {
      event.preventDefault();
    }
  }
  
  
  preventEmoji(event: KeyboardEvent) {
    const key = event.key;
    const emojiRegex = /\p{Extended_Pictographic}/u;
  
    if (emojiRegex.test(key)) {
      event.preventDefault();
    }
  }

  preventEmojiOnPaste(event: ClipboardEvent) {
    const pastedText = event.clipboardData?.getData('text') || '';
    const emojiRegex = /\p{Extended_Pictographic}/u;
  
    if (emojiRegex.test(pastedText)) {
      event.preventDefault();
    }
  }
  
  onPromoCodeChange(value: string) {
    // Remove all spaces and assign
    this.promoCode = value.replace(/\s/g, '');
  }
  

  removeDiscount() {
    this.discountApplied = false;
    this.appliedCode = '';
  }
}

