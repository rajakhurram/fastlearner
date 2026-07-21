import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { CourseService } from 'src/app/core/services/course.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { BulkQuizUploaderModalComponent } from './bulk-quiz-uploader-modal.component';

describe('BulkQuizUploaderModalComponent', () => {
  let component: BulkQuizUploaderModalComponent;
  let fixture: ComponentFixture<BulkQuizUploaderModalComponent>;
  let modalRefSpy: jasmine.SpyObj<NzModalRef>;
  let messageSpy: jasmine.SpyObj<NzMessageService>;
  let courseServiceSpy: jasmine.SpyObj<CourseService>;

  const successCode = new HttpConstants().REQUEST_STATUS.SUCCESS_200.CODE;

  beforeEach(async () => {
    modalRefSpy = jasmine.createSpyObj<NzModalRef>('NzModalRef', ['close']);
    messageSpy = jasmine.createSpyObj<NzMessageService>('NzMessageService', [
      'error',
      'warning',
      'success',
    ]);
    courseServiceSpy = jasmine.createSpyObj<CourseService>('CourseService', [
      'parseBulkTestQuestionsFromExcel',
    ]);
    const fileManagerSpy = jasmine.createSpyObj<FileManager>('FileManager', [
      'uploadFile',
    ]);

    await TestBed.configureTestingModule({
      declarations: [BulkQuizUploaderModalComponent],
      providers: [
        { provide: NzModalRef, useValue: modalRefSpy },
        { provide: NzMessageService, useValue: messageSpy },
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: FileManager, useValue: fileManagerSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(BulkQuizUploaderModalComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call zip extraction and show zip icon', async () => {
    const zipFile = new File(['zip-content'], 'images.zip', {
      type: 'application/zip',
    });
    spyOn<any>(component, 'extractImagesFromZip').and.resolveTo();

    await component.fileChangeEvent({
      0: zipFile,
      length: 1,
      item: (i: number) => (i === 0 ? zipFile : null),
    } as unknown as FileList);

    expect(component.showZipIcon).toBeTrue();
    expect((component as any).extractImagesFromZip).toHaveBeenCalledWith(
      zipFile,
    );
  });

  it('should upload single image and hide zip icon', async () => {
    const imageFile = new File(['img'], 'img1.png', { type: 'image/png' });
    spyOn(component, 'uploadImages').and.resolveTo({
      'img1.png': 'https://cdn/img1.png',
    });

    await component.fileChangeEvent({
      0: imageFile,
      length: 1,
      item: (i: number) => (i === 0 ? imageFile : null),
    } as unknown as FileList);

    expect(component.showZipIcon).toBeFalse();
    expect(component.selectedImage).toBe('img1.png');
    expect(component.uploadImages).toHaveBeenCalled();
    expect(component.imageUrlMap['img1.png']).toBe('https://cdn/img1.png');
  });

  it('should call excel replacement before parse when image map exists', async () => {
    const excelFile = new File(['excel'], 'questions.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const replacedFile = new File(['updated'], 'questions.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    component.selectedFile = excelFile;
    component.selectedFileName = 'questions.xlsx';
    component.imageUrlMap = { 'img1.png': 'https://cdn/img1.png' };
    spyOn<any>(component, 'replaceQuestionImageUrlsInExcel').and.resolveTo(
      replacedFile,
    );

    courseServiceSpy.parseBulkTestQuestionsFromExcel.and.returnValue(
      of({
        status: successCode,
        data: {
          questions: [{ questionText: 'Q1', answers: [] }],
          successCount: 1,
          errorCount: 0,
        },
      }),
    );

    await component.saveModal();

    expect(
      (component as any).replaceQuestionImageUrlsInExcel,
    ).toHaveBeenCalledWith(excelFile);
    expect(
      courseServiceSpy.parseBulkTestQuestionsFromExcel,
    ).toHaveBeenCalledWith(replacedFile);
    expect(modalRefSpy.close).toHaveBeenCalled();
  });

  it('should map question and option image names to urls in excel', async () => {
    const XLSX = require('xlsx');
    component.imageUrlMap = {
      'q.png': 'https://cdn/q.png',
      'a.jpg': 'https://cdn/a.jpg',
    };

    const workbook = XLSX.utils.book_new();
    const rows = [
      {
        questionType: 'SINGLE_CHOICE',
        questionImageUrl: 'q.png',
        A: 'a.jpg',
        B: 'Plain text option',
      },
    ];
    const ws = XLSX.utils.json_to_sheet(rows);
    XLSX.utils.book_append_sheet(workbook, ws, 'Sheet1');
    const out = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const file = new File([out], 'questions.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    const updatedFile = await (
      component as any
    ).replaceQuestionImageUrlsInExcel(file);
    const updatedWorkbook = XLSX.read(await updatedFile.arrayBuffer(), {
      type: 'array',
    });
    const updatedRows = XLSX.utils.sheet_to_json(
      updatedWorkbook.Sheets[updatedWorkbook.SheetNames[0]],
      { defval: '' },
    );

    expect(updatedRows[0].questionImageUrl).toBe('https://cdn/q.png');
    expect(updatedRows[0].A).toBe('https://cdn/a.jpg');
    expect(updatedRows[0].B).toBe('Plain text option');
  });
});
