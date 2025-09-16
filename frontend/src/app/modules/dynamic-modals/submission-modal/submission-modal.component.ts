import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';

@Component({
  selector: 'app-submission-modal',
  templateUrl: './submission-modal.component.html',
  styleUrls: ['./submission-modal.component.scss'],
})
export class SubmissionModalComponent implements OnInit {
  @Input() data?: any;
  @Input() buttonText?: any;

  constructor(private _modal: NzModalService, private _router: Router) {}

  ngOnInit(): void {

  }

  routeToStudentPage() {
    this._router.navigate(['student']);
  }
}
