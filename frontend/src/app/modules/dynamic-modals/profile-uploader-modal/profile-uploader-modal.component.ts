import { Component, Input } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { ImageCroppedEvent, LoadedImage } from 'ngx-image-cropper';
import { User } from 'src/app/core/models/user.model';
import { FileManager } from 'src/app/core/services/file-manager.service';

@Component({
  selector: 'app-profile-uploader-modal',
  templateUrl: './profile-uploader-modal.component.html',
  styleUrls: ['./profile-uploader-modal.component.scss'],
})
export class ProfileUploaderModalComponent {
  imageChangedEvent: any = '';
  croppedImage: any = '';
  user: User = new User();
  uploading: boolean = false;
  showReplaceButton: boolean = false;
  selectedFileName: string | undefined;
  @Input() imageAspectRatio: any;

  constructor(
    private modalRef: NzModalRef,
    private _fileManagerService: FileManager
  ) {}

  fileChangeEvent(files: FileList | null): void {
    if (files && files.length > 0) {
      const file = files[0];
      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (event: any) => {
          const image = new Image();
          image.src = event.target.result;
          image.onload = () => {
            const imageWidth = image.width;
            const imageHeight = image.height;
            this.imageAspectRatio = imageWidth / imageHeight;

            this.imageChangedEvent = {
              target: {
                files: [file],
              },
            };
          };
        };
        reader.readAsDataURL(file);

        this.selectedFileName = file.name;
      } else {
        alert('Please select an image file.');
      }
    }
  }

  imageCropped(event: ImageCroppedEvent) {
    this.croppedImage = event.blob;
    this.showReplaceButton = true;
  }

  imageLoaded(image?: LoadedImage) {}

  cropperReady() {}

  loadImageFailed() {}

  closeModal(): void {
    this.modalRef.close();
    this.showReplaceButton = false;
  }

  onClose(data: any): void {
    if (data) {
      this.modalRef.close({ profilePicture: data?.data });
    } else {
      this.modalRef.close(null);
    }
  }

  saveModal(): void {
    if (this.croppedImage) {
      this.uploading = true;
      this.showReplaceButton = true;
      const file = new File([this.croppedImage], 'profile_image.jpeg', {
        type: 'image/jpeg',
      });

      this._fileManagerService.uploadFile(file, 'PROFILE_IMAGE').subscribe({
        next: (response: any) => {
          this.uploading = false;
          this.showReplaceButton = false;
          if (response) {
            this.onClose(response);
          }
        },
        error: (error: any) => {
          this.uploading = false;
          this.showReplaceButton = false;
        },
        complete: () => {
          this.modalRef.close();
        },
      });
    }
  }
}
