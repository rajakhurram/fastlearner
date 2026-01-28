import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-static-card',
  templateUrl: './static-card.component.html',
  styleUrls: ['./static-card.component.scss'],
})
export class StaticCardComponent {
  @Input() staticCards: any;
  @Input() instructorView: boolean = false;
  @Input() showButtonWhenLoggedIn: boolean = false;
  @Input() showButton: boolean = false;

  constructor(private _router: Router) {}

  routeToTeachOnFastlearner() {
    this._router.navigate(['welcome-instructor']);
  }

  routeToSignIn() {
    this._router.navigate(['auth/sign-in']);
  }
  routeToInstructor(url) {
    if (this.instructorView) {
      window.open(url, '_blank');
    }
  }
}
