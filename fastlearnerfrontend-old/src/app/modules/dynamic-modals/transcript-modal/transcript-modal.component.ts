import { Component, Input, OnInit } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CommunicationService } from 'src/app/core/services/communication.service';

@Component({
  selector: 'app-transcript-modal',
  templateUrl: './transcript-modal.component.html',
  styleUrls: ['./transcript-modal.component.scss']
})
export class TranscriptModalComponent implements OnInit{
  @Input() videoData?: any = {};

  constructor(
    private _modal: NzModalService,
    private _communicationService: CommunicationService,
  ) { }

  ngOnInit(): void {
  }

  saveTranscript() {
    this._communicationService.sendVideoTranscript(this.videoData);
    this._modal.closeAll();
  }
}
