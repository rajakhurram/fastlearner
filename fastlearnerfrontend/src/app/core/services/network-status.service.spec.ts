import { TestBed } from '@angular/core/testing';
import { NetworkStatusService } from './network-status.service';

describe('NetworkStatusService', () => {
  let service: NetworkStatusService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NetworkStatusService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should expose current online status', (done) => {
    service.isOnline$.subscribe((status) => {
      expect(typeof status).toBe('boolean');
      done();
    });
  });
});
