import { Component, OnInit, ViewContainerRef } from '@angular/core';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { CertificateService } from 'src/app/core/services/certificate.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Certificate } from 'src/app/core/models/certificate.model';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthService } from 'src/app/core/services/auth.service';
import { ShareModalComponent } from '../../dynamic-modals/share-modal/share-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  selector: 'app-certificate',
  templateUrl: './certificate.component.html',
  styleUrls: ['./certificate.component.scss'],
})
export class CertificateComponent implements OnInit {
  _httpConstants: HttpConstants = new HttpConstants();
  public certificate?: Certificate = {};
  courseId?: any;
  showCertificate?: boolean = false;
  certificateUrl?: string = '';
  private _viewContainerRef: ViewContainerRef;
  isLoading: boolean = false;

  uuid?: string = '';

  constructor(
    private _certificateService: CertificateService,
    private _activatedRoute: ActivatedRoute,
    private _authService: AuthService,
    private _modal: NzModalService,
    private loader: NgxUiLoaderService
  ) {}

  ngOnInit(): void {
    this.getRouteQueryParam();
  }

  getRouteQueryParam() {
    this._activatedRoute.queryParams?.subscribe((params) => {
      this.courseId = params['courseId'];
      this.showCertificate = true;
      this.getCertificateData(this.courseId);
      this.getCertificate(this.courseId);
    });
  }

  getCertificateData(courseId?: any) {
    this._certificateService?.getCertificateData(courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.certificate = response?.data;
          this.uuid = this.certificate.uuid;
          this.certificate.certifiedAt = this.formatDate(
            this.certificate.certifiedAt
          );
        }
      },
      error: (error: any) => {},
    });
  }

  getCertificate(courseId?: any): void {
    this.isLoading = true; 
    this.loader.start(); 

    try {
      this.certificateUrl = this._certificateService.getCertificateUrl(courseId, false);
      this.isLoading = false; 
    } catch (error) {
      console.error('Error fetching certificate URL:', error);
      this.isLoading = false; 
    }
  }

  onImageLoad(): void {
    this.loader.stop(); 
  }

  onImageError(): void {
    this.loader.stop(); 
  }

  downloadPDF() {
    const certificateUrl = this._certificateService.getCertificateUrl(this.courseId, true);
    if (certificateUrl) {
      this.convertImageToPDF(certificateUrl);
    } else {
      console.error('Certificate URL not found');
    }
  }

  downloadJPG() {
    const certificateUrl = this._certificateService.getCertificateUrl(this.courseId, true);
    if (certificateUrl) {
      this.convertImageToJPG(certificateUrl);
    } else {
      console.error('Certificate URL not found');
    }
  }

  formatDate(date?: any) {
    const formattedDate = new Date(date);
    const day = String(formattedDate.getDate()).padStart(2, '0');
    const month = String(formattedDate.getMonth() + 1).padStart(2, '0');
    const year = formattedDate.getFullYear();
    const d = `${month}/${day}/${year}`;
    return d;
  }

  openShareCourseModal() {
    const modal = this._modal.create({
      nzContent: ShareModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: null,
        url: `https://fastlearner.ai/student/verify-certificate/${this.uuid}`,
        title: 'Share Certificate',
        label: 'Share Certificate URL',
      },
      nzFooter: null,
      nzKeyboard: true,
    });
  }

  async convertImageToPDF(imageUrl: string) {
    try {
      const response = await fetch(imageUrl);
      const blob = await response.blob();
      const reader = new FileReader();
  
      reader.onload = function (e: any) {
        const imgData = e.target.result;
  
        const pdf = new jsPDF('landscape', 'pt', 'a4');
        const pdfWidth = pdf.internal.pageSize.getWidth();
        const pdfHeight = pdf.internal.pageSize.getHeight();
  
        const image = new Image();
        image.src = imgData;
        image.onload = function () {
          const imgWidth = image.width;
          const imgHeight = image.height;
  
          // Scale the image to fill the entire PDF page
          pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
          pdf.save('certificate.pdf');
        };
      };
  
      reader.readAsDataURL(blob);
    } catch (error) {
      console.error('Error converting image to PDF', error);
    }
  }

  async convertImageToJPG(imageUrl: string) {
    try {
      const response = await fetch(imageUrl);
      const blob = await response.blob();
      const reader = new FileReader();
  
      reader.onload = function (e: any) {
        const imgData = e.target.result;
  
        const image = new Image();
        image.src = imgData;
        image.onload = function () {
          const canvas = document.createElement('canvas');
          canvas.width = image.width;
          canvas.height = image.height;
  
          const ctx = canvas.getContext('2d');
          ctx.drawImage(image, 0, 0);
  
          // Convert canvas content to a JPG blob
          canvas.toBlob((blob) => {
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = 'certificate.jpg';
            link.click();
          }, 'image/jpeg', 1.0);
        };
      };
  
      reader.readAsDataURL(blob);
    } catch (error) {
      console.error('Error converting image to JPG', error);
    }
  }

}
