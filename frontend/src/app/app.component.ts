import { Component, HostListener, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { HttpConstants } from './core/constants/http.constants';
import { UserService } from './core/services/user.service';
import { CacheService } from './core/services/cache.service';
import { Gtag } from 'angular-gtag';
import { CommunicationService } from './core/services/communication.service';
import { StateService } from './core/services/state.service';
import { Meta, Title } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { environment } from '../environments/environment';
import { NetworkStatusService } from './core/services/network-status.service';
import { NzMessageService } from 'ng-zorro-antd/message';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  title = 'FastLearnerApp';
  showNavbar: boolean = true;
  isLoggedIn: any;
  keyPressCount: number = 0;
  flLogin?: boolean = false;
  clientId?: string;
  showFooter?: boolean = true;
  isProductionServer?: any = false;

  _httpConstants: HttpConstants = new HttpConstants();
  private queryParamSubscription: Subscription | undefined;

  constructor(
    private _router: Router,
    private _authService: AuthService,
    private _userService: UserService,
    private _cacheService: CacheService,
    private _communicationService: CommunicationService,
    private networkStatus: NetworkStatusService,
    private message: NzMessageService,
    private _activatedRoute: ActivatedRoute
  ) {
    this._communicationService?.navbarAndFooterStateSubject$?.subscribe(
      (flag: any) => {
        this.showNavbar = flag;
        this.showFooter = flag;
      }
    );
    this.showOrHideNavbar();
    this.isUserLoggedIn();
    this.isProductionServer = environment.isProductionServer;
  }

  isOnline = true;


  ngOnInit(): void {
    this.queryParamSubscription = this._activatedRoute.queryParams?.subscribe(
      (params) => {
        this.clientId = params['clientId'];
        if (this.clientId) {
          this.flLogin = true;
        }
      }
    );

    this._router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
      }
    });

    let hasPreviouslyBeenOffline = false;

  this.networkStatus.isOnline$.subscribe(status => {
    this.isOnline = status;

    if (!status) {
      hasPreviouslyBeenOffline = true;
      this.message.warning('You are offline!', { nzDuration: 4000 }); 
          } else if (hasPreviouslyBeenOffline) {
      this.message.success('Back online!', { nzDuration: 3000 });  // auto-dismiss after 3s
    }
  });
  }
  
  isUserLoggedIn() {
    this.isLoggedIn = this._authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.getUserCompleteProfile();
    }
  }

  getUserCompleteProfile() {
    this._userService.getUserProfile().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._cacheService.saveInCache(
            'userProfile',
            JSON.stringify(response?.data)
          );
        }
      },
      error: (error: any) => {
        return;
      },
    });
  }

  showOrHideNavbar() {
    this._router?.events?.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        if (event.url.includes('/user/profile')) {
          this.showNavbar = true;
        } else if (event?.urlAfterRedirects == '/') {
          this.showNavbar = true;
          return;
        } else if (
          event.url.includes('/student/course-content/') ||
          event.url.includes('/instructor/instructor-dashboard') ||
          event.url.includes('/instructor/course') ||
          event.url.includes('/instructor/performance') ||
          event.url.includes('/instructor/notifications') ||
          event.url.includes('/instructor')
        ) {
          this.showNavbar = false;
        } else {
          this.showNavbar = true;
        }
      }
    });
  }
}
