import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FastlearnerSignupComponent } from './fastlearner-signup.component';
import { TestRoutingModule } from '../modules/instructor/test/test-routing.module';
import { ActivatedRoute } from '@angular/router';
import { CacheService } from '../core/services/cache.service';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { AuthService } from '../core/services/auth.service';
import { MessageService } from '../core/services/message.service';
import { CommunicationService } from '../core/services/communication.service';
import { UserService } from '../core/services/user.service';
import { StateService } from '../core/services/state.service';
import { NzCardModule } from 'ng-zorro-antd/card';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzButtonModule } from 'ng-zorro-antd/button';

describe('FastlearnerSignupComponent', () => {
  let component: FastlearnerSignupComponent;
  let fixture: ComponentFixture<FastlearnerSignupComponent>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'isSubscribed'
    ]);

    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [ FastlearnerSignupComponent ],
      imports: [
        TestRoutingModule,
        NzModalModule,
        NzCardModule,
        FormsModule,
        ReactiveFormsModule,
        NzGridModule,
        NzCheckboxModule,
        NzButtonModule
      ],
      providers: [
        {provide: ActivatedRoute, useValue: {}},
        {provide: CacheService, useValue: {}},
        {provide: SocialAuthService, useValue: {}},
        {provide: AuthService, useValue: authService},
        {provide: MessageService, useValue: {}},
        {provide: NzModalService, useValue: {}},
        {provide: CommunicationService, useValue: {}},
        {provide: UserService, useValue: {}},
        {provide: StateService, useValue: {}},
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FastlearnerSignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
