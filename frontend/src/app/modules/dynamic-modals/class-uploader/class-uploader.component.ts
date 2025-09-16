import { Component, Input } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';

@Component({
  selector: 'app-class-uploader',
  templateUrl: './class-uploader.component.html',
  styleUrls: ['./class-uploader.component.scss']
})
export class ClassUploaderComponent {
 @Input() studentName!: string;
  @Input() studentEmail!: string;
  @Input() studentScore!: number;
  @Input() aiResultId!: number;

success = false;

  constructor(
    private modalRef: NzModalRef, 
    private aiGraderService: AiGraderService


  ) {}

  onSend() {

    if (!this.isEmailValid(this.studentEmail)) {
    console.warn('Email is invalid, not sending.');
    return;
  }
  
  this.aiGraderService.sendEmail(this.studentEmail, this.aiResultId).subscribe({
    next: (res) => {
      console.log('Email sent successfully:', res);
      this.success = true;
    },
    error: (err) => {
      console.error('Failed to send email:', err);
    }
  });
}

  onCancel() {
  this.modalRef.destroy();
}

isEmailValid(email: string): boolean {
  if (!email || !email.trim()) return false;
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailPattern.test(email.trim());
}
}
