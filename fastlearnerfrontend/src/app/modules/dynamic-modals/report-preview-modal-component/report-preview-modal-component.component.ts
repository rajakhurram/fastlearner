import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import { NzModalRef } from "ng-zorro-antd/modal";
@Component({
  selector: 'app-report-preview-modal',
  templateUrl: './report-preview-modal-component.component.html',
  styleUrls: ['./report-preview-modal-component.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportPreviewModalComponent {
  @Input() reportContent: string = '';
  @Input() quizTitle: string = '';

  constructor(
    private modalRef: NzModalRef,
    private sanitizer: DomSanitizer
  ) {}

  get sanitizedReportContent(): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(this.reportContent);
  }

  close(): void {
    this.modalRef.destroy();
  }

  downloadReport(): void {
    // Download as HTML file
    const blob = new Blob([this.reportContent], { type: 'text/html' });
    this.downloadBlob(blob, `quiz-report-${this.quizTitle || 'preview'}.html`);
  }

  downloadAsPdf(): void {
  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = this.reportContent;

  tempDiv.style.position = 'absolute';
  tempDiv.style.left = '-9999px';
  document.body.appendChild(tempDiv);

  html2canvas(tempDiv, { scale: 2 }).then(canvas => {
    const imgData = canvas.toDataURL('image/png');

    const pdf = new jsPDF('p', 'mm', 'a4');
    const imgProps = pdf.getImageProperties(imgData);
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;

    pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
    pdf.save(`quiz-report-${this.quizTitle || 'preview'}.pdf`);

    // Clean up
    document.body.removeChild(tempDiv);
  });
}

  private downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}