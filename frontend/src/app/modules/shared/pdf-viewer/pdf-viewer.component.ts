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

  download(){
    
  }

}
