import { Component, EventEmitter, Inject, Input, Output } from '@angular/core';
import { NZ_MODAL_DATA, NzModalRef } from 'ng-zorro-antd/modal';
import { buttonConfig } from 'src/app/core/models/button.model-config';

@Component({
  selector: 'app-deletion-modal',
  templateUrl: './deletion-modal.component.html',
  styleUrls: ['./deletion-modal.component.scss'],
})
export class DeletionModalComponent {
  @Input() msg?: string;
  @Input() secondBtnText?: string;
  @Output() cancelClick: EventEmitter<any> = new EventEmitter();
  @Output() deleteClick: EventEmitter<any> = new EventEmitter();

  constructor(private modalRef: NzModalRef) {}

  buttonCancelConfig: buttonConfig = {
    backgroundColor: '#fff',
    color: '#fe4a55',
    borderColor: '#FE4A55',
    paddingRight: '50px',
    paddingLeft: '50px',
    border: '1px solid',
  };
  buttonAddConfig: buttonConfig = {
    paddingRight: '60px',
    paddingLeft: '60px',
  };

  ngOnInit(): void {
    if (!this.secondBtnText) {
      this.secondBtnText = 'Delete';
    }
  }

  onCancel() {
    this.cancelClick.emit();
    this.modalRef.close();
  }

  onDelete() {
    this.deleteClick.emit();
    this.modalRef.close(true);
  }
}
