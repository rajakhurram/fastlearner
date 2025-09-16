import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { FileManager } from 'src/app/core/services/file-manager.service';

@Component({
  selector: 'app-summary-modal',
  templateUrl: './summary-modal.component.html',
  styleUrls: ['./summary-modal.component.scss']
})
export class SummaryModalComponent implements OnInit {

  _httpConstants: HttpConstants = new HttpConstants();
  @Input() document?: any;
  @Input() file?: any;
  @Input() fileType?: any;
  @Input() videoData?: any;
  @Input() article?: any;
  @Input() documentSummary?: any;
  @Input() videoSummary?: any;
  @Input() articleSummary?: any;
  @Output() summaryClosed = new EventEmitter<string>();
  showSpinner?: any = false;
  url?: any = '';

  constructor(
    private _modal: NzModalService,
    private _communicationService: CommunicationService,
    private _fileManagerService: FileManager
  ) { }

  ngOnInit(): void {

  }

  regenerateSummary() {
    this.showSpinner = true;
    if (this.documentSummary) {
      this.document.documentSummary = '';
      this.url = this.document?.documentUrl;
    } else if (this.videoSummary) {
      this.videoData.videoSummary = '';
      this.url = this.videoData?.videoUrl;
    } else {
      this.url = this.article?.articleDocumnetUrl;
      this.article.articleSummary = '';
    }

    this._fileManagerService.regenerateSummary(this.url, this.fileType).subscribe({
      next: (response: any) => {
        if (response?.status === this._httpConstants.REQUEST_STATUS.CREATED_201.CODE) {
          if (this.documentSummary) {
            this.document.documentSummary = response?.data?.summary
          } else if (this.videoSummary) {
            this.videoData.videoSummary = response?.data?.transcriptData?.summary
          } else {
            this.article.articleSummary = response?.data?.summary
          }
          this.showSpinner = false;
        }
      },
      error: (error: any) => {
        this.showSpinner = false;
      }
    });
  }

  saveSummary() {
    if (this.documentSummary) {
      this._communicationService.sendDocumentSummary(this.document);
    } else if (this.videoSummary) {
      this._communicationService.sendVideoSummary(this.videoData);
    } else {
      this._communicationService.sendArticleSummary(this.article);
    }
    this._modal.closeAll();
  }

}
