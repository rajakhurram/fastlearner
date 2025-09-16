import { Component, Input } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { ImageCroppedEvent, LoadedImage } from 'ngx-image-cropper';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { User } from 'src/app/core/models/user.model';

@Component({
  selector: 'app-image-uploader-modal',
  templateUrl: './image-uploader-modal.component.html',
  styleUrls: ['./image-uploader-modal.component.scss'],
})
export class ImageUploaderModalComponent {
  imageChangedEvent: any = '';
  croppedImage: any = '';
  user: User = new User();
  uploading: boolean = false;
  showMessage: boolean = false;
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
    if (event.width >= 1920 && event.height >= 1080) {
      this.croppedImage = event.blob;
      this.uploading = false;
      this.showMessage = false;
      this.showReplaceButton = true;
    } else {
      this.croppedImage = null;
      this.uploading = true;
      this.showMessage = true;
      this.showReplaceButton = true;
      return;
    }
  }
  imageLoaded(image?: LoadedImage) {}

  cropperReady() {}

  loadImageFailed() {}

  closeModal(): void {
    this.modalRef.close();
  }

  onClose(data: any, name): void {
    if (data) {
      this.modalRef.close({ profilePicture: data?.data, fileName: name });
    } else {
      this.modalRef.close(null);
    }
  }

  saveModal(): void {
    if (this.croppedImage) {
      this.uploading = true;
      const file = new File([this.croppedImage], this.selectedFileName, {
        type: 'image/jpeg',
      });

      this._fileManagerService.uploadFile(file, 'PROFILE_IMAGE').subscribe({
        next: (response: any) => {
          this.uploading = false;
          this.showReplaceButton = false;
          if (response) {
            this.onClose(response, this.selectedFileName);
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
