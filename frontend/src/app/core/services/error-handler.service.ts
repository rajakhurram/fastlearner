import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {

  errorMessage: any;

  constructor(private _router : Router, private _messageService : MessageService) { }

  public handleError = (error: HttpErrorResponse) => {
    if (error.status === 500) {
      this.handle500Error(error);
    }
    else if (error.status === 404) {
      this.handle404Error(error)
    }
    else {
      this.handleOtherError(error);
    }
  }
  private handle500Error = (error: HttpErrorResponse) => {
    this.createErrorMessage(error);
    this._router.navigate(['/500']);
  }
  private handle404Error = (error: HttpErrorResponse) => {
    this.createErrorMessage(error);
    this._router.navigate(['/404']);
  }
  private handleOtherError = (error: HttpErrorResponse) => {
    this.createErrorMessage(error);
  }
  private createErrorMessage = (error: HttpErrorResponse) => {
    this.errorMessage = error.error ? error.error : error.statusText;
  }
}
