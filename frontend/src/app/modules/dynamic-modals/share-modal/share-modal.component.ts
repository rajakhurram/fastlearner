import { Component, Input, OnInit } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { Location } from '@angular/common';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { environment } from 'src/environments/environment.development';
import { Router } from '@angular/router';

@Component({
  selector: 'app-share-modal',
  templateUrl: './share-modal.component.html',
  styleUrls: ['./share-modal.component.scss'],
})
export class ShareModalComponent implements OnInit {
  @Input() data?: any;
  @Input() title?: any;
  @Input() label?: any;
  @Input() url?: any;
  shareURL: any;

  _httpConstants: HttpConstants = new HttpConstants();

  constructor(
    private _courseService: CourseService,
    private _messageService: MessageService,
    private location: Location,
    private router: Router,
    private _modal: NzModalService
  ) {}

  ngOnInit(): void {
      if(this.url){
        this.shareURL = this.url
      }else {
        const fullUrl = this.location.prepareExternalUrl(this.location.path());
        this.shareURL = `${window.location.href}`;
      }
      
  }

  copyURL() {
    let copyText = document.getElementById('myInput') as HTMLInputElement;
    copyText?.select();
    navigator.clipboard.writeText(copyText.value);
  }

  unsecuredCopyToClipboard = (text) => {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    try {
      document.execCommand('copy');
    } catch (err) {
      console.error('Unable to copy to clipboard', err);
    }
    document.body.removeChild(textArea);
  };

  /**
   * Copies the text passed as param to the system clipboard
   * Check if using HTTPS and navigator.clipboard is available
   * Then uses standard clipboard API, otherwise uses fallback
   */
  copyToClipboard() {
    let copyText = document.getElementById('myInput') as HTMLInputElement;
    if (window.isSecureContext && navigator.clipboard) {
      copyText?.select();
      navigator.clipboard.writeText(copyText.value);
    } else {
      this.unsecuredCopyToClipboard(copyText.value);
    }
  }
}
