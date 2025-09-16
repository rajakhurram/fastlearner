import { TestBed } from '@angular/core/testing';

import { TestInformationDropdownsService } from './test-information-dropdowns.service';

describe('TestInformationDropdownsService', () => {
  let service: TestInformationDropdownsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TestInformationDropdownsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
