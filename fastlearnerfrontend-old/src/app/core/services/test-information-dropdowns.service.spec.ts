import { TestBed } from '@angular/core/testing';

import { TestInformationDropdownsService } from './test-information-dropdowns.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('TestInformationDropdownsService', () => {
  let service: TestInformationDropdownsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        {provide: AuthService, useValue: {}}
      ]
    });
    service = TestBed.inject(TestInformationDropdownsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
