import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-view-all-button',
  templateUrl: './view-all-button.component.html',
  styleUrls: ['./view-all-button.component.scss'],
})
export class ViewAllButtonComponent {
  @Input() applyRight: boolean = false;
  @Input() isFromPremium: boolean = false;
  @Input() theme: string = 'light';
  @Output() viewAllButtonClickEmitter: EventEmitter<any> = new EventEmitter();

  viewAllEmitter() {
    this.viewAllButtonClickEmitter.emit();
  }
}
