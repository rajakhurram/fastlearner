import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FastlearnerLoginComponent } from './fastlearner-login.component';
import { of } from 'rxjs';
import { CommunicationService } from '../core/services/communication.service';
import { FastlearnerSigninComponent } from '../fastlearner-signin/fastlearner-signin.component';
import { CacheService } from '../core/services/cache.service';
import { FastlearnerSignupComponent } from '../fastlearner-signup/fastlearner-signup.component';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from '../core/services/auth.service';
import { MessageService } from '../core/services/message.service';
import { SocialAuthService, SocialLoginModule } from '@abacritt/angularx-social-login';
import { NzCardModule } from 'ng-zorro-antd/card';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('FastlearnerLoginComponent', () => {
  let component: FastlearnerLoginComponent;
  let fixture: ComponentFixture<FastlearnerLoginComponent>;

  beforeEach(async () => {
    const communicationServiceSpy = {
      flLoginSubject$: of({}),
    }
    
    const cacheServiceSpy = jasmine.createSpyObj<CacheService>('CacheService', [
      'getDataFromCache',
    ])
    
    const authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', [
      'isSubscribed',
    ])

    authServiceSpy.isSubscribed.and.returnValue(of())

    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [
        FastlearnerLoginComponent,
      ],
      imports: [
        NzCardModule,
        HttpClientTestingModule,
        SocialLoginModule,
      ],
      providers: [
        { provide: CommunicationService, useValue: communicationServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ActivatedRoute, useValue: {} },
        { provide: MessageService, useValue: {} },
        { provide: SocialAuthService, useValue: {} },
      ],
    })
      .compileComponents();

    fixture = TestBed.createComponent(FastlearnerLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
