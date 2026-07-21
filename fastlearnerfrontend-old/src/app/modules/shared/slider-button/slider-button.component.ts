import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-slider-button',
  templateUrl: './slider-button.component.html',
  styleUrls: ['./slider-button.component.scss'],
})
export class SliderButtonComponent {
  @Output() slideRightEmitter: EventEmitter<any> = new EventEmitter();
  @Output() slideLeftEmitter: EventEmitter<any> = new EventEmitter();
  @Output() buttonClickEmitter: EventEmitter<any> = new EventEmitter();

  @Input() theme: string;
  @Input() buttonText: string;
  @Input() moveButtonRight: boolean = false;
  @Input() applyMargin: boolean = false;
  @Input() showButton: boolean = false;
  @Input() hideSliderButtons: boolean = false;
  @Input() enableLeftButton: boolean = false;
  @Input() enableRightButton: boolean = false;
  @Input() showButtonWhenLoggedIn: boolean = false;

  slideRight() {
    this.slideRightEmitter.emit();
  }

  slideLeft() {
    this.slideLeftEmitter.emit();
  }

  onButtonClick() {
    this.buttonClickEmitter.emit();
  }
}
