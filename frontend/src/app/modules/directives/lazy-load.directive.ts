import { Directive, ElementRef, EventEmitter, Output, AfterViewInit } from '@angular/core';

@Directive({
  selector: '[appLazyLoad]'  // Custom directive for lazy loading
})
export class LazyLoadDirective implements AfterViewInit {
  @Output() visible = new EventEmitter<void>();  // Emit event when element is visible
  private observer!: IntersectionObserver;

  constructor(private el: ElementRef) {}

  ngAfterViewInit() {
    // Intersection Observer to track visibility
    this.observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          this.visible.emit();  // Emit the event to trigger section load
          this.observer.disconnect();  // Disconnect observer if loadOnce is true (to load only once)
        }
      },
      {
        threshold: 0.25  // Trigger when 25% of element is visible
      }
    );

    // Start observing the element
    this.observer.observe(this.el.nativeElement);
  }
}
