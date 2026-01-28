import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LazyLoadDirective } from './lazy-load.directive';
import { Component, DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

@Component({
  template: '<div appLazyLoad (visible)="onVisible()"></div>'
})
class TestHostComponent {
  // A property to track if the 'visible' event was emitted
  wasVisible = false; 
  onVisible() {
    this.wasVisible = true;
  }
}

describe('LazyLoadDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let hostComponent: TestHostComponent;
  let directiveEl: DebugElement;
  let directiveInstance: LazyLoadDirective;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // Declare the Host Component and the Directive
      declarations: [LazyLoadDirective, TestHostComponent],
    }).compileComponents();
  });
  
  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    hostComponent = fixture.componentInstance;
    
    // Find the element in the Host template that has the directive applied
    directiveEl = fixture.debugElement.query(By.directive(LazyLoadDirective));
    
    // Get the actual instance of the directive attached to the element
    directiveInstance = directiveEl.injector.get(LazyLoadDirective);
    
    // Ensure the component and directive are initialized (calls ngAfterViewInit)
    fixture.detectChanges();
  });

  // The original passing test is no longer a unit test, but a setup verification:
  it('should create an instance and attach it to the host element', () => {
    // Assert that the directive instance was successfully created and injected
    expect(directiveInstance).toBeTruthy(); 
  });
});
