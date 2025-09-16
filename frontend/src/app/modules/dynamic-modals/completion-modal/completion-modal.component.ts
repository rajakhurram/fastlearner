import { Component } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CommunicationService } from 'src/app/core/services/communication.service';

@Component({
  selector: 'app-completion-modal',
  templateUrl: './completion-modal.component.html',
  styleUrls: ['./completion-modal.component.scss']
})
export class CompletionModalComponent {

  constructor(
    private _modal: NzModalService,
    private _communicationService: CommunicationService
  ){}

  done(){
    this._communicationService.closeCourseCompletionModal();
  }
}
