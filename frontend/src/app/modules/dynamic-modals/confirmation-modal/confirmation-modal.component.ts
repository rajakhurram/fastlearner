import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { AnimationItem } from 'lottie-web';
import { AnimationOptions } from 'ngx-lottie';

@Component({
  selector: 'app-confirmation-modal',
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.scss']
})
export class ConfirmationModalComponent {

  ngOnDestroy (){
    this._router.navigate(['student']);
  }

  constructor(private _router : Router) {}

  @Input() title?: string;
  @Input() subtitle?: string;
  @Input() data?: any;

  options: AnimationOptions = {
    path: '../../../../assets/animations/check Mark.json',
  };

  styling: Partial<CSSStyleDeclaration> = {
     height : '100px'
  };
  animationCreated(animationItem: AnimationItem): void {
  }

  routeToStudentDashboard(){
    this._router.navigate(['student']);
  }



}
