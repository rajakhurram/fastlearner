import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentTypeComponent } from './content-type.component';
import { TestRoutingModule } from '../../instructor/test/test-routing.module';
import { AuthService } from 'src/app/core/services/auth.service';
import { Router } from '@angular/router';
import { NzCardModule } from 'ng-zorro-antd/card';

describe('ContentTypeComponent', () => {
  let component: ContentTypeComponent;
  let fixture: ComponentFixture<ContentTypeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContentTypeComponent ],
      imports: [
        TestRoutingModule,
        NzCardModule
      ],
      providers: [
        {provide: AuthService, useValue: {}},
        {
          provide: Router,
          useValue: jasmine.createSpyObj<Router>('Router', ['navigate'])
        },
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContentTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
