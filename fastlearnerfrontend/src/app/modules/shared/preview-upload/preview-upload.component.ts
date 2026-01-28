import { Component, Input, HostBinding } from '@angular/core';

@Component({
  selector: 'app-preview-upload',
  templateUrl: './preview-upload.component.html',
  styleUrls: ['./preview-upload.component.scss'],
})
export class PreviewUploadComponent {
  @Input() isUploaded: boolean = false;
  @Input() fileType: string = 'img';
  @Input() videoSrc: string | null = null;
  @Input() imageSrc: string | null = null;
  @Input() fullWidth: boolean = false;
  @Input() currentSelectedTopic: any = {};
  @Input() src: string = '';
  @HostBinding('style.max-width') hostMaxWidth: string = '50%';
  @HostBinding('style.flex') hostFlex: string = '0 0 50%';

  ngOnInit() {
    if (this.fullWidth) {
      this.setFullWidth();
    } else {
      this.setHalfWidth();
    }
  }

  setFullWidth(): void {
    this.hostMaxWidth = '100%';
    this.hostFlex = '0 0 100%';
  }

  setHalfWidth(): void {
    this.hostMaxWidth = '50%';
    this.hostFlex = '0 0 50%';
  }
}
