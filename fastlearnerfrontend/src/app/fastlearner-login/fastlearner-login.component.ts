import { Component } from '@angular/core';
import { CommunicationService } from '../core/services/communication.service';

@Component({
  selector: 'app-fastlearner-login',
  templateUrl: './fastlearner-login.component.html',
  styleUrls: ['./fastlearner-login.component.scss']
})
export class FastlearnerLoginComponent {

  constructor(   
    private _communicationService: CommunicationService,
  ){
    this._communicationService.flLoginSubject$?.subscribe((flag: any) => {
      this.flLoginStateChange(flag);
    });
  }

  ngOnInit(): void {
  }

  flSignIn?: boolean = true;

  flLoginStateChange(flag?: any){
    this.flSignIn = flag;
  }

}
