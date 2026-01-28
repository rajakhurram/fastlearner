import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'initialCharactor',
})
export class InitialCharactorPipe implements PipeTransform {
  transform(fullName: any, ...args: unknown[]): any {
    let initials = '';
    if (fullName) {
      let splitName = fullName.split(' ');
      if (splitName.length == 1) {
        let firstCharactorOfFirstName = splitName[0][0]?.toUpperCase();
        initials = firstCharactorOfFirstName;
      } else {
        let firstCharactorOfFirstName = splitName[0][0]?.toUpperCase();
        let firstCharactorOfLastName = splitName[1][0] ? splitName[1][0]?.toUpperCase() : '';
        initials = firstCharactorOfFirstName + firstCharactorOfLastName;
      }
    }

    return initials;
  }
}
