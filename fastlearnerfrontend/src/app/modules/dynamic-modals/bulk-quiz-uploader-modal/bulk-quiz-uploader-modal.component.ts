import { Component } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CourseService } from 'src/app/core/services/course.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { BulkQuizImportModalResult } from 'src/app/core/models/bulk-quiz-import.model';
import JSZip from 'jszip';
import { FileManager } from 'src/app/core/services/file-manager.service';

declare const require: any;

@Component({
  selector: 'app-bulk-quiz-uploader-modal',
  templateUrl: './bulk-quiz-uploader-modal.component.html',
  styleUrls: ['./bulk-quiz-uploader-modal.component.scss'],
})
export class BulkQuizUploaderModalComponent {
  selectedFile: File | null = null;
  selectedFileName: string | null = null;
  formattedFileSize = '';
  showReplaceButton = false;
  saving = false;
  zipUploading = false;
  selectedImage: string | null = null;
  imageSize: string | null = null;
  showZipIcon = false;
  imageUrlMap: Record<string, string> = {};
  private readonly httpConstants = new HttpConstants();

  constructor(
    private modalRef: NzModalRef,
    private courseService: CourseService,
    private message: NzMessageService,
    private fileManagerService: FileManager,
  ) {}

  downloadTemplate() {
    this.fileManagerService.downloadExcelTemplate();
  }

  async fileChangeEvent(files: FileList | null): Promise<void> {
    if (!files?.length) {
      return;
    }

    const file = files[0];
    if (
      !this.isExcelFile(file) &&
      !this.isImageFile(file) &&
      !this.isZipFile(file)
    ) {
      this.message.error(
        'Please select an Excel file (.xlsx only) or image file (.jpg, .jpeg, .png) or zip file (.zip).',
      );
      return;
    }

    if (this.isImageFile(file)) {
      this.showZipIcon = false;
      this.selectedImage = file.name;
      this.imageSize = this.formatBytes(file.size);
      const imgMap: { [filename: string]: File } = {};
      imgMap[file.name] = file;
      this.imageUrlMap = await this.uploadImages(imgMap);
      return;
    } else if (this.isZipFile(file)) {
      this.showZipIcon = true;
      this.selectedImage = file.name;
      this.imageSize = this.formatBytes(file.size);
      this.extractImagesFromZip(file);
      return;
    } else {
      this.selectedFile = file;
      this.selectedFileName = file.name;
      this.formattedFileSize = this.formatBytes(file.size);
      this.showReplaceButton = true;
    }
  }

  closeModal(): void {
    this.modalRef.close(null);
  }

  clearSelectedFile(type: string): void {
    if (type === 'excel') {
      this.selectedFile = null;
      this.selectedFileName = null;
      this.formattedFileSize = '';
      this.showReplaceButton = false;
    } else {
      this.selectedImage = null;
      this.imageSize = null;
      this.showZipIcon = false;
    }
  }

  async saveModal(): Promise<void> {
    if (!this.selectedFile || this.saving || this.zipUploading) {
      return;
    }
    this.saving = true;

    if (Object.keys(this.imageUrlMap).length) {
      this.selectedFile = await this.replaceQuestionImageUrlsInExcel(
        this.selectedFile,
      );
    }

    this.courseService
      .parseBulkTestQuestionsFromExcel(this.selectedFile)
      .subscribe({
        next: (response: any) => {
          this.saving = false;

          if (
            response?.status !==
            this.httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.message.error(
              response?.message || 'Failed to parse Excel file.',
            );
            return;
          }

          const data = response?.data;
          const questions = data?.questions ?? [];

          if (!questions.length) {
            this.message.error(
              data?.errorCount
                ? 'No valid questions imported. Fix row errors and try again.'
                : 'No valid questions found in file.',
            );
            return;
          }

          if (data?.errorCount > 0) {
            this.message.warning(
              `Imported ${data.successCount} question(s). ${data.errorCount} row(s) failed.`,
            );
          }

          const result: BulkQuizImportModalResult = {
            questions,
            fileName: this.selectedFileName,
            successCount: data.successCount,
            errorCount: data.errorCount,
            imageUrlMap: this.imageUrlMap,
          };
          this.modalRef.close(result);
        },
        error: (err: any) => {
          this.saving = false;
          this.message.error(
            err?.error?.message || 'Failed to parse Excel file.',
          );
        },
      });
  }

  private isExcelFile(file: File): boolean {
    const fileName = file.name.toLowerCase();
    return (
      fileName.endsWith('.xlsx') ||
      file.type ===
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    );
  }

  private isImageFile(file: File): boolean {
    const fileName = file.name.toLowerCase();
    return (
      fileName.endsWith('.jpg') ||
      fileName.endsWith('.jpeg') ||
      fileName.endsWith('.png') ||
      fileName.endsWith('.gif')
    );
  }

  private formatBytes(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  private isZipFile(file: File): boolean {
    const fileName = file.name.toLowerCase();
    return fileName.endsWith('.zip');
  }

  private async extractImagesFromZip(file: File) {
    this.zipUploading = true;
    const jszip = new JSZip();
    try {
      const zip = await jszip.loadAsync(file);
      const imageFiles: File[] = [];
      const imageExtensions = ['png', 'jpg', 'jpeg', 'gif', 'webp', 'svg'];
      const promises: Promise<void>[] = [];
      zip.forEach((relativePath: string, entry: any) => {
        const ext = relativePath.split('.').pop()?.toLowerCase();
        if (!entry.dir && ext && imageExtensions.includes(ext)) {
          const promise = entry.async('blob').then((blob: Blob) => {
            const mimeType = `image/${ext === 'jpg' ? 'jpeg' : ext}`;
            const imageFile = new File([blob], relativePath, {
              type: mimeType,
            });
            imageFiles.push(imageFile);
          });

          promises.push(promise);
        }
      });
      await Promise.all(promises);
      const imageMap: { [filename: string]: File } = {};
      imageFiles.forEach((img) => {
        const filename = img.name.split('/').pop()!;
        imageMap[filename] = img;
      });

      this.imageUrlMap = await this.uploadImages(imageMap);
      console.log(this.imageUrlMap);
    } finally {
      this.zipUploading = false;
    }
  }

  async uploadImages(imageMap: {
    [filename: string]: File;
  }): Promise<{ [filename: string]: string }> {
    const imageUrlMap: { [filename: string]: string } = {};

    const uploadPromises = Object.keys(imageMap).map((filename) => {
      return new Promise<void>((resolve, reject) => {
        const img = imageMap[filename];

        this.fileManagerService.uploadFile(img, 'PROFILE_IMAGE').subscribe({
          next: (response: any) => {
            imageUrlMap[filename] = response.data;
            console.log(`Mapped: ${filename} → ${response.data}`);
            resolve();
          },
          error: (err) => {
            console.error(`Failed to upload: ${filename}`, err);
            reject(err);
          },
        });
      });
    });

    await Promise.all(uploadPromises);
    return imageUrlMap;
  }

  private async replaceQuestionImageUrlsInExcel(file: File): Promise<File> {
    if (!Object.keys(this.imageUrlMap).length) {
      return file;
    }

    const XLSX = require('xlsx');
    const arrayBuffer = await file.arrayBuffer();
    const workbook = XLSX.read(arrayBuffer, { type: 'array' });
    const firstSheetName = workbook.SheetNames[0];
    if (!firstSheetName) {
      return file;
    }

    const worksheet = workbook.Sheets[firstSheetName];
    const rows: any[] = XLSX.utils.sheet_to_json(worksheet, { defval: '' });
    if (!rows.length) {
      return file;
    }

    const isImageName = (value: string): boolean =>
      /\.(png|jpg|jpeg|gif|webp|svg)$/i.test(value);

    const toMappedUrl = (value: any): any => {
      const raw = String(value ?? '').trim();
      if (!raw || !isImageName(raw)) {
        return value;
      }
      const baseName = raw.split(/[/\\]/).pop() ?? raw;
      return this.imageUrlMap[raw] ?? this.imageUrlMap[baseName] ?? value;
    };

    const mappedRows = rows.map((row) => ({
      ...row,
      questionImageUrl: toMappedUrl(row?.questionImageUrl),
      A: toMappedUrl(row?.A),
      B: toMappedUrl(row?.B),
      C: toMappedUrl(row?.C),
      D: toMappedUrl(row?.D),
      E: toMappedUrl(row?.E),
      F: toMappedUrl(row?.F),
    }));

    workbook.Sheets[firstSheetName] = XLSX.utils.json_to_sheet(mappedRows);
    const out = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    return new File([out], file.name, { type: file.type });
  }
}
