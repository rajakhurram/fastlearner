import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FastlearnerSigninComponent } from './fastlearner-signin.component';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TestRoutingModule } from '../modules/instructor/test/test-routing.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SharedModule } from '../modules/shared/shared.module';
import { SocialAuthService, SocialLoginModule } from '@abacritt/angularx-social-login';
import { ChangeDetectorRef, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { CacheService } from '../core/services/cache.service';
import { CommunicationService } from '../core/services/communication.service';
import { MessageService } from '../core/services/message.service';
import { NotificationService } from '../core/services/notification.service';
import { UserService } from '../core/services/user.service';
import { NzCardModule } from 'ng-zorro-antd/card';
import { of } from 'rxjs';
import { NzButtonModule } from 'ng-zorro-antd/button';

describe('FastlearnerSigninComponent', () => {
  let component: FastlearnerSigninComponent;
  let fixture: ComponentFixture<FastlearnerSigninComponent>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', [
      'isSubscribed'
    ]);

    const communicationServiceSpy = {
      removeEmitterData$: of({}),
    }

    await TestBed.configureTestingModule({
      declarations: [FastlearnerSigninComponent],
      imports: [
        ReactiveFormsModule,
        TestRoutingModule,
        HttpClientTestingModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule,
        NzCardModule,
        SocialLoginModule,
        NzButtonModule,
      ],
      providers: [
        // { provide: FormBuilder, useValue: {} },
        { provide: Router, useValue: {} },
        { provide: CacheService, useValue: {} },
        { provide: ActivatedRoute, useValue: {} },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MessageService, useValue: {} },
        { provide: SocialAuthService, useValue: {} },
        { provide: UserService, useValue: {} },
        { provide: CommunicationService, useValue: communicationServiceSpy },
        { provide: NotificationService, useValue: {} },
        { provide: ChangeDetectorRef, useValue: {} }
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(FastlearnerSigninComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
