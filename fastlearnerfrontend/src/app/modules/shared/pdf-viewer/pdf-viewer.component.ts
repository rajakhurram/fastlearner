import { Component, Input } from '@angular/core';
import { buttonConfig } from 'src/app/core/models/button.model-config';

@Component({
  selector: 'app-pdf-viewer',
  templateUrl: './pdf-viewer.component.html',
  styleUrls: ['./pdf-viewer.component.scss'],
})
export class PdfViewerComponent {
  @Input() src: string = '';
  @Input() isDownloadable: boolean = false;

  download() {
    if (!this.src) return;

    const link = document.createElement('a');
    link.href = this.src;
    link.download = this.getFileNameFromUrl(this.src); 
    link.target = '_blank'; 
    link.click();
    link.remove();
  }

  private getFileNameFromUrl(url: string): string {
    try {
      return url.split('/').pop() || 'file.pdf';
    } catch {
      return 'file.pdf';
    }
  }

}
