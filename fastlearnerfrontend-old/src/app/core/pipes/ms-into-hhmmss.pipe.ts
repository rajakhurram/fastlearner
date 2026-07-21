import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'msIntoHHMMSS'
})
export class MsIntoHHMMSSPipe implements PipeTransform {

  transform(ms: any, ...args: unknown[]): any {
    const seconds = Math.floor((ms / 1000) % 60);
    const minutes = Math.floor((ms / 1000 / 60) % 60);
    const hours = Math.floor((ms / 1000 / 3600 ) % 24)
  
    const humanized = [
      hours.toString() + 'h', 
      minutes.toString() + 'm',
      seconds.toString() + 's',
    ].join(' '+ ':' + ' ');

    return humanized;
  }

}
