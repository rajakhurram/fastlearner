import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';

@Component({
  selector: 'app-course-draft-modal',
  templateUrl: './course-draft-modal.component.html',
  styleUrls: ['./course-draft-modal.component.scss']
})
export class CourseDraftModalComponent {
  @Input() msg?: string;
  @Output() cancelClick: EventEmitter<any> = new EventEmitter();
  @Output() okClick: EventEmitter<any> = new EventEmitter();

  constructor(
    private modalRef: NzModalRef,
  ){}
  
  ngOnInit(): void {
  }

  onCancel() {
    this.cancelClick.emit();
    this.modalRef.close(); 
  }

  onOk() {
    this.okClick.emit();
    this.modalRef.close(); 
  }
}
