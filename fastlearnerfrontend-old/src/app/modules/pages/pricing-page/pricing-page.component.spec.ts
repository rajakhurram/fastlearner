import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PricingPageComponent } from './pricing-page.component';
import { TestRoutingModule } from '../../instructor/test/test-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AuthService } from 'src/app/core/services/auth.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { Router } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { of } from 'rxjs';

describe('PricingPageComponent', () => {
  let component: PricingPageComponent;
  let fixture: ComponentFixture<PricingPageComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockCacheService: jasmine.SpyObj<CacheService>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj<AuthService>('AuthService', [
      'getSubscriptionPlans',
      'verifyUserSubscription',
      'newUserSubscription',
    ]);

    mockAuthService.verifyUserSubscription.and.returnValue(of({
      data: {
        currentPlan: "Free Plan"
      }
    }));

    mockCacheService = jasmine.createSpyObj<CacheService>('CacheService', [
      'getDataFromCache',
    ]);

    await TestBed.configureTestingModule({
      declarations: [ PricingPageComponent ],
      imports: [
        TestRoutingModule,
        BrowserAnimationsModule,
        SharedModule
      ],
      providers: [
        { provide: NzModalService, useValue: {} },
        { provide: SubscriptionService, useValue: {} },
        { provide: MessageService, useValue: {} },
        { provide: Router, useValue: {} },
        { provide: CacheService, useValue: mockCacheService },
        { provide: AuthService, useValue: mockAuthService },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(PricingPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
