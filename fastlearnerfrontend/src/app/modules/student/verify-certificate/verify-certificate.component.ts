import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CertificateService } from 'src/app/core/services/certificate.service';
import { environment } from 'src/environments/environment.development';

@Component({
  selector: 'app-verify-certificate',
  templateUrl: './verify-certificate.component.html',
  styleUrls: ['./verify-certificate.component.scss'],
})
export class VerifyCertificateComponent implements OnInit {
  uuid: string;
  isVerified: boolean = false;
  showSpin: boolean = true;
  certificateImageUrl: string = '';

  constructor(
    private route: ActivatedRoute,
    private _router: Router,
    private _certificateService: CertificateService
  ) {}

  ngOnInit(): void {
    this.uuid = this.route.snapshot.paramMap.get('uuid')!;
    this.verifyCertificate(this.uuid);
    setTimeout(() => {
      this.showSpin = false;
    }, 2500);
  }

  verifyCertificate(uuid: string): void {
    this._certificateService.verifyCertificate(uuid).subscribe({
      next: (response: any) => {
        this.certificateImageUrl = `${environment.baseUrl}certificate/verify/${uuid}`;
        this.showSpin = false;
        this.isVerified = true;
      },
      error: (error) => {
        console.error('Error verifying certificate:', error);
        this.isVerified = false;
        this.showSpin = false;
      },
    });
  }

  routeToInstructorWelcomePage() {
    this._router.navigate(['welcome-instructor']);
  }
}
