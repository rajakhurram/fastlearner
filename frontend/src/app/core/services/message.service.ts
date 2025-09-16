import { Injectable } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private currentMessageId: string | null = null;
  config: any = {
    nzDuration: 1500,
    nzAnimate: true,
  };

  constructor(private _message: NzMessageService) {}

  private closeCurrentMessage() {
    if (this.currentMessageId) {
      this._message.remove(this.currentMessageId);
      this.currentMessageId = null;
    }
  }

  success(message: string) {
    this.closeCurrentMessage();
    this.currentMessageId = this._message.success(message, this.config)?.messageId;
  }

  error(message: string) {
    this.closeCurrentMessage();
    this.currentMessageId = this._message.error(message, this.config)?.messageId;
  }

  warning(message: string) {
    this.closeCurrentMessage();
    this.currentMessageId = this._message.warning(message, this.config)?.messageId;
  }

  info(message: string) {
    this.closeCurrentMessage();
    this.currentMessageId = this._message.info(message, this.config)?.messageId;
  }

  remove() {
    if (this.currentMessageId) {
      this._message.remove(this.currentMessageId);
      this.currentMessageId = null;
    }
  }
  
}
