import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, fromEvent, merge, of } from 'rxjs';
import { mapTo } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class NetworkStatusService {
  private online$ = new BehaviorSubject<boolean>(navigator.onLine);

  constructor(private zone: NgZone) {
    this.monitorNetwork();
  }

  private monitorNetwork() {
    this.zone.runOutsideAngular(() => {
      merge(
        fromEvent(window, 'online').pipe(mapTo(true)),
        fromEvent(window, 'offline').pipe(mapTo(false)),
        of(navigator.onLine)
      ).subscribe(status => {
        this.zone.run(() => this.online$.next(status));
      });
    });
  }

  get isOnline$() {
    return this.online$.asObservable();
  }
}
