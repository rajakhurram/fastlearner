import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchDropdownComponent } from './search-dropdown.component';
import { SharedModule } from '../shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('SearchDropdownComponent', () => {
  let component: SearchDropdownComponent;
  let fixture: ComponentFixture<SearchDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchDropdownComponent ],
      imports: [
        SharedModule,
        BrowserAnimationsModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SearchDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
