import { Component, Input } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';

@Component({
  selector: 'app-class-assessment-modal',
  templateUrl: './class-assessment-modal.component.html',
  styleUrls: ['./class-assessment-modal.component.scss'],
})
export class ClassAssessmentModalComponent {
  @Input() type: 'class' | 'assessment' = 'class';
  @Input() classId?: number; // Required only for assessment

  name = '';
  loading = false;
  result: { id: number; name: string; type: 'class' | 'assessment' } | null =
    null;

  constructor(
    private modalRef: NzModalRef,
    private aiGraderService: AiGraderService
  ) {}

  success = false;

  setSuccessTrueAndDisableClose(): void {
    this.success = true;
    this.modalRef.updateConfig({
      nzClosable: false, // ✅ hide the 'X' button
      nzMaskClosable: false, // (optional) disable clicking backdrop to close
    });
  }

  onSend(): void {
    const trimmed = this.name.trim();
    if (!trimmed) return;

    this.loading = true;

    if (this.type === 'class') {
      this.aiGraderService.createClass({ name: trimmed }).subscribe({
        next: (res) => {
          const created = res?.data;
          if (created?.id) {
            this.setSuccessTrueAndDisableClose();
            // store the result to close later
            this.result = {
              id: created.id,
              name: created.name,
              type: 'class',
            };
          }
          this.loading = false;
        },
        error: (err) => {
          console.error('Failed to create class:', err);
          this.loading = false;
        },
      });
    } else {
      const classId = Number(this.classId);
      if (!classId) {
        console.error('Missing class ID for assessment');
        this.loading = false;
        return;
      }

      this.aiGraderService
        .createAssessment({ name: trimmed, classId })
        .subscribe({
          next: (res) => {
            const created = res?.data;
            if (created?.id) {
              this.setSuccessTrueAndDisableClose();
              this.result = {
                id: created.id,
                name: created.name,
                type: 'assessment',
              };
            }
            this.loading = false;
          },
          error: (err) => {
            console.error('Failed to create assessment:', err);
            this.loading = false;
          },
        });
    }
  }

  onCancel(): void {
  if (this.success && this.result) {
    this.modalRef.close(this.result); 
  } else {
    this.modalRef.close(null); 
  }
}

}
