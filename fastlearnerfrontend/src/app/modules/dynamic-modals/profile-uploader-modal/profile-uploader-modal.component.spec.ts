import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ProfileUploaderModalComponent } from './profile-uploader-modal.component';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { ImageCroppedEvent } from 'ngx-image-cropper';
import { User } from 'src/app/core/models/user.model';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('ProfileUploaderModalComponent', () => {
  let component: ProfileUploaderModalComponent;
  let fixture: ComponentFixture<ProfileUploaderModalComponent>;
  let modalRef: jasmine.SpyObj<NzModalRef>;
  let fileManagerService: jasmine.SpyObj<FileManager>;

  beforeEach(async () => {
    const modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);
    const fileManagerServiceSpy = jasmine.createSpyObj('FileManager', [
      'uploadFile',
    ]);

    await TestBed.configureTestingModule({
      declarations: [ProfileUploaderModalComponent],
      providers: [
        { provide: NzModalRef, useValue: modalRefSpy },
        { provide: FileManager, useValue: fileManagerServiceSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileUploaderModalComponent);
    component = fixture.componentInstance;
    modalRef = TestBed.inject(NzModalRef) as jasmine.SpyObj<NzModalRef>;
    fileManagerService = TestBed.inject(
      FileManager
    ) as jasmine.SpyObj<FileManager>;
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

    expect(component.croppedImage).not.toBeNull();
    expect(component.uploading).toBeFalse();
    expect(component.showReplaceButton).toBeTrue();
  });

  it('should call modalRef.close() on closeModal', () => {
    component.closeModal();
    expect(modalRef.close).toHaveBeenCalled();
  });

  it('should call modalRef.close() with data on onClose', () => {
    const mockResponse = { data: 'mockProfileImage' };
    component.onClose(mockResponse);
    expect(modalRef.close).toHaveBeenCalledWith({
      profilePicture: 'mockProfileImage',
    });
  });

  it('should call modalRef.close() with null on onClose when no data', () => {
    component.onClose(null);
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
