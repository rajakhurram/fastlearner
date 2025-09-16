import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { CacheService } from '../services/cache.service';
import { Router } from '@angular/router';
import { MessageService } from '../services/message.service';
import { AuthService } from '../services/auth.service';
// import { MessageService } from '../services/message.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(
    private _cacheService : CacheService,
    private _router : Router,
    private _messageService : MessageService,
    private _authService: AuthService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe( tap(() =>{},
    (err : any) => {
        if (err instanceof HttpErrorResponse) {
            if (err.status !== 401 && err.status !== 403) {
              
                return;
            }
            return
          }
    }))
  }
}
