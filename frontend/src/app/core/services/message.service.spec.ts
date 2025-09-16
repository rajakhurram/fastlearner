import { TestBed } from '@angular/core/testing';
import { NzMessageService } from 'ng-zorro-antd/message';
import { MessageService } from './message.service';

describe('MessageService', () => {
  let service: MessageService;
  let messageServiceSpy: jasmine.SpyObj<NzMessageService>;

  beforeEach(() => {
    // Create a spy for NzMessageService
    const spy = jasmine.createSpyObj('NzMessageService', ['success', 'error', 'warning', 'info']);

    TestBed.configureTestingModule({
      providers: [
        MessageService,
        { provide: NzMessageService, useValue: spy }
      ],
    });

    service = TestBed.inject(MessageService);
    messageServiceSpy = TestBed.inject(NzMessageService) as jasmine.SpyObj<NzMessageService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call success method on NzMessageService', () => {
    const message = 'Success message';
    service.success(message);
    expect(messageServiceSpy.success).toHaveBeenCalledWith(message, service.config);
  });

  it('should call error method on NzMessageService', () => {
    const message = 'Error message';
    service.error(message);
    expect(messageServiceSpy.error).toHaveBeenCalledWith(message, service.config);
  });

  it('should call warning method on NzMessageService', () => {
    const message = 'Warning message';
    service.warning(message);
    expect(messageServiceSpy.warning).toHaveBeenCalledWith(message, service.config);
  });

  it('should call info method on NzMessageService', () => {
    const message = 'Info message';
    service.info(message);
    expect(messageServiceSpy.info).toHaveBeenCalledWith(message, service.config);
  });
});
