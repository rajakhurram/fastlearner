import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { ImageUploaderModalComponent } from './image-uploader-modal.component';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { User } from 'src/app/core/models/user.model';
import { ImageCroppedEvent } from 'ngx-image-cropper';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('ImageUploaderModalComponent', () => {
  let component: ImageUploaderModalComponent;
  let fixture: ComponentFixture<ImageUploaderModalComponent>;
  let fileManagerService: jasmine.SpyObj<FileManager>;
  let modalRef: jasmine.SpyObj<NzModalRef>;

  beforeEach(async () => {
    const fileManagerServiceSpy = jasmine.createSpyObj('FileManager', [
      'uploadFile',
    ]);
    const modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);

    await TestBed.configureTestingModule({
      declarations: [ImageUploaderModalComponent],
      providers: [
        { provide: FileManager, useValue: fileManagerServiceSpy },
        { provide: NzModalRef, useValue: modalRefSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ImageUploaderModalComponent);
    component = fixture.componentInstance;
    fileManagerService = TestBed.inject(
      FileManager
    ) as jasmine.SpyObj<FileManager>;
    modalRef = TestBed.inject(NzModalRef) as jasmine.SpyObj<NzModalRef>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should handle file change event and read image file', () => {
    const file = new File([''], 'test-image.jpg', { type: 'image/jpeg' });

    const fileList = {
      0: file,
      length: 1,
      item: (index: number) => (index === 0 ? file : null),
    } as unknown as FileList;

    const reader = new FileReader();
    spyOn(reader, 'readAsDataURL').and.callFake((file: Blob) => {
      reader.onload({
        target: { result: 'data:image/jpeg;base64,test' },
      } as any);
    });

    spyOn(window, 'FileReader').and.returnValue(reader as any);

    component.fileChangeEvent(fileList);

    expect(component.imageChangedEvent).toBe('');
    expect(component.selectedFileName).toBe('test-image.jpg');
  });

  it('should handle image cropping correctly', () => {
    const event: ImageCroppedEvent = {
      base64: '',
      blob: new Blob([''], { type: 'image/jpeg' }),
      width: 1920,
      height: 1080,
      cropperPosition: { x1: 0, y1: 0, x2: 0, y2: 0 },
      imagePosition: { x1: 0, y1: 0, x2: 0, y2: 0 },
    };

    component.imageCropped(event);

    expect(component.croppedImage).toBe(event.blob);
    expect(component.uploading).toBeFalse();
    expect(component.showMessage).toBeFalse();
    expect(component.showReplaceButton).toBeTrue();
  });

  it('should show message when image dimensions are not sufficient', () => {
    const event: ImageCroppedEvent = {
      base64: '',
      blob: new Blob([''], { type: 'image/jpeg' }),
      width: 1000,
      height: 500,
      cropperPosition: { x1: 0, y1: 0, x2: 0, y2: 0 },
      imagePosition: { x1: 0, y1: 0, x2: 0, y2: 0 },
    };

    component.imageCropped(event);

    expect(component.croppedImage).toBeNull();
    expect(component.uploading).toBeTrue();
    expect(component.showMessage).toBeTrue();
    expect(component.showReplaceButton).toBeTrue();
  });

  it('should call modalRef.close() on closeModal', () => {
    component.closeModal();
    expect(modalRef.close).toHaveBeenCalled();
  });

  it('should call modalRef.close() with data on onClose', () => {
    const mockResponse = { data: 'mockProfileImage' , fileName: 'Selected File'  };
    component.onClose(mockResponse , 'Selected File');
    expect(modalRef.close).toHaveBeenCalledWith({
      profilePicture: 'mockProfileImage',
      fileName: 'Selected File'
    });
  });

  it('should call modalRef.close() with null on onClose when no data', () => {
    component.onClose(null , {});
    expect(modalRef.close).toHaveBeenCalledWith(null);
  });

  it('should handle saveModal correctly', () => {
    const file = new File(
      [new Blob([''], { type: 'image/jpeg' })],
      'profile_image.jpeg',
      { type: 'image/jpeg' }
    );
    const mockResponse = { data: 'mockProfileImage' };

    component.croppedImage = new Blob([''], { type: 'image/jpeg' });

    fileManagerService.uploadFile.and.returnValue(of(mockResponse));

    component.saveModal();

    expect(component.uploading).toBeFalse();
    expect(fileManagerService.uploadFile).toHaveBeenCalledWith(
      file,
      'PROFILE_IMAGE'
    );
  });

  it('should handle saveModal error scenario', () => {
    component.croppedImage = new Blob([''], { type: 'image/jpeg' });
    fileManagerService.uploadFile.and.returnValue(
      throwError(() => new Error('Upload failed'))
    );

    component.saveModal();

    expect(component.uploading).toBeFalse();
    expect(component.showReplaceButton).toBeFalse();
  });
});
