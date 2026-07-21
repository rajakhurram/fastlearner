import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PremiumStudentComponent } from './premium-student.component';
import { CourseService } from 'src/app/core/services/course.service';
import { PremiumStudentsService } from 'src/app/core/services/premium-student.service';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import dayjs from 'dayjs';
import { of, throwError } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';
import { MessageService } from 'src/app/core/services/message.service';

describe('PremiumStudentComponent', () => {
  let component: PremiumStudentComponent;
  let fixture: ComponentFixture<PremiumStudentComponent>;
  let premiumStudentsService: jasmine.SpyObj<PremiumStudentsService>;
  let courseService: jasmine.SpyObj<CourseService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const premiumStudentsServiceSpy = jasmine.createSpyObj('PremiumStudentsService', ['getPremiumStudents', 'downloadExcel', 'getPremiumStudentsByDate']);
    const courseServiceSpy = jasmine.createSpyObj('CourseService', ['getSuggestions']);

    const routerSpy = jasmine.createSpyObj('Router', ['navigate', 'parseUrl']);
    const messageServiceSpy = jasmine.createSpyObj<MessageService>('MessageService', ['error', 'info', 'success']);

    await TestBed.configureTestingModule({
      declarations: [PremiumStudentComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: PremiumStudentsService, useValue: premiumStudentsServiceSpy },
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MessageService, useValue: messageServiceSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA] 
    }).compileComponents();

    premiumStudentsService = TestBed.inject(PremiumStudentsService) as jasmine.SpyObj<PremiumStudentsService>;
    courseService = TestBed.inject(CourseService) as jasmine.SpyObj<CourseService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    fixture = TestBed.createComponent(PremiumStudentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  // ============================================== //
  // NOTE: we are not setting data range by default //
  // ============================================== //

  // it('should initialize with default date range', () => {
  //   const startOfDay = dayjs().startOf('day');
  //   const endOfDay = dayjs().endOf('day');

  //   expect(component.selectedRangeCalendarTimeRight.startDate.isSame(startOfDay)).toBeTrue();
  //   expect(component.selectedRangeCalendarTimeRight.endDate.isSame(endOfDay)).toBeTrue();
  // });

  it('should call fetchPremiumStudents on init', () => {
    spyOn(component, 'fetchPremiumStudents');
    component.ngOnInit();
    expect(component.fetchPremiumStudents).toHaveBeenCalled();
  });


  it('should fetch students data and set total elements', () => {
    const mockResponse = {
      content: [{ name: 'John Doe', purchasedDate: new Date(), email: 'john@example.com', courseTitle: 'Course 101' }],
      totalElements: 1
    };
    premiumStudentsService.getPremiumStudents.and.returnValue(of({data: mockResponse}));

    component.fetchPremiumStudents();
    fixture.detectChanges();

    expect(component.students).toEqual(mockResponse.content);
    expect(component.totalElements).toBe(mockResponse.totalElements);
  });


  // it('should apply date range filter and fetch students', () => {
  //   spyOn(component, 'fetchPremiumStudentsByDateRange');
  //   component.applyDateRange();

  //   const startDate = component.selectedRangeCalendarTimeRight.startDate.toISOString();
  //   const endDate = component.selectedRangeCalendarTimeRight.endDate.toISOString();

  //   expect(component.fetchPremiumStudentsByDateRange).toHaveBeenCalledWith(startDate, endDate);
  // });


  // it('should fetch search suggestions when keyword length >= 3', () => {
  //   const mockSuggestions = { data: [{ title: 'Suggested Course' }] };
  //   courseService.getSuggestions.and.returnValue(of(mockSuggestions));

  //   component.getSearchSuggestions('Java');
  //   fixture.detectChanges();

  //   expect(courseService.getSuggestions).toHaveBeenCalledWith('Java');
  //   expect(component.searchSuggestions.length).toBe(1);
  // });

  // it('should clear search suggestions if keyword length is 0', () => {
  //   component.searchSuggestions = [{ title: 'Old Suggestion' }];
  //   component.getSearchSuggestions('');
  //   expect(component.searchSuggestions.length).toBe(0);
  // });

  it('should trigger file download when exportData is called', () => {
    const blob = new Blob(['test'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    premiumStudentsService.downloadExcel.and.returnValue(of(blob));
    component.students = [{id: 123}]

    spyOn(document.body, 'appendChild').and.callThrough();
    component.exportData();
    
    expect(premiumStudentsService.downloadExcel).toHaveBeenCalled();
  });


  it('should clear search keyword, results, and reset selectedIndex', () => {
    component.searchKeyword = 'Java';
    component.students = [{ title: 'Course 101' }];

    premiumStudentsService.getPremiumStudents.and.returnValue(of({data: {content: [], totalElements: 0}}));
    component.clearSearch();

    expect(component.searchKeyword).toBe('');
    expect(component.students.length).toBe(0);
    // expect(component.selectedIndex).toBe(-1);
  });

  it('should update page number and fetch students on page change', () => {
    spyOn(component, 'fetchPremiumStudents');
    component.onPageChange(2);

    expect(component.payLoad.pageNo).toBe(1);
    expect(component.fetchPremiumStudents).toHaveBeenCalled();
  });

  it('should handle no data returned from getPremiumStudents', () => {
    premiumStudentsService.getPremiumStudents.and.returnValue(of({ data: {content: [], totalElements: 0} }));
    component.fetchPremiumStudents();
    fixture.detectChanges();
  
    expect(component.students.length).toBe(0);
    expect(component.totalElements).toBe(0);
  });

  // it('should not fetch search suggestions if keyword length < 3', () => {
  //   component.getSearchSuggestions('Ja');
  //   expect(courseService.getSuggestions).not.toHaveBeenCalled();
  // });

  it('should call exportData on export button click', () => {
    spyOn(component, 'exportData');
  
    fixture.detectChanges();
  
    const button = fixture.debugElement.query(By.css('.export-button button'));
    button.triggerEventHandler('click', null);
  
    expect(component.exportData).toHaveBeenCalled();
  });
  



  it('should handle error while exporting data', () => {
    const errorMessage = 'Export failed';
    premiumStudentsService.downloadExcel.and.returnValue(throwError(() => new Error(errorMessage)));
    component.students = [{id: 123}]
    spyOn(console, 'error');
    component.exportData();
    fixture.detectChanges();
  
    expect(console.error).toHaveBeenCalledWith('Export failed:', jasmine.any(Error));
  });
  

  it('should update student list when new data is fetched', () => {
    const newMockResponse = {
      content: [{ name: 'Jane Doe', purchasedDate: new Date(), email: 'jane@example.com', courseTitle: 'Course 102' }],
      totalElements: 1
    };
    premiumStudentsService.getPremiumStudents.and.returnValue(of({data: newMockResponse}));
    component.students = [];
    component.totalElements = 0;
    component.fetchPremiumStudents();
    fixture.detectChanges();
  
    expect(component.students).toEqual(newMockResponse.content);
    expect(component.totalElements).toBe(newMockResponse.totalElements);
  });

  // it('should fetch search suggestions and set searchSuggestions when keyword length >= 3', () => {
  //   const mockSuggestions = { data: [{ title: 'Suggested Java Course' }] };
  //   courseService.getSuggestions.and.returnValue(of(mockSuggestions));
  
  //   component.getSearchSuggestions('Java');
  //   fixture.detectChanges();
  
  //   expect(courseService.getSuggestions).toHaveBeenCalledWith('Java');
  //   expect(component.searchSuggestions.length).toBe(1);
  //   expect(component.searchSuggestions[0].title).toBe('Suggested Java Course');
  // });
  
  it('should apply date range filter with correct start and end dates', () => {
    const startDate = '2024-01-01T00:00:00.000Z';
    const endDate = '2024-12-31T23:59:59.999Z';

    const payload = {
      startDate: dayjs(startDate).startOf('day').format('YYYY-MM-DDTHH:mm:ss'),
      endDate: dayjs(endDate).startOf('day').format('YYYY-MM-DDTHH:mm:ss'),
      pageNo: 0,
    };

    // Spy on the function
    spyOn(component, 'datesUpdatedRange').and.callThrough();

    // Trigger the function
    component.datesUpdatedRange({ startDate, endDate });

    expect(component.datesUpdatedRange).toHaveBeenCalledWith({ startDate, endDate });
  });

  it('should reset search keyword and results when clearSearch is called', () => {
    component.searchKeyword = 'Java';
    component.students = [{ title: 'Java Basics' }];
    premiumStudentsService.getPremiumStudents.and.returnValue(of({data: {
      content: [],
      totalElements: 0,
    }}));
    component.clearSearch();
  
    expect(component.searchKeyword).toBe('');
    expect(component.students.length).toBe(0);
    // expect(component.selectedIndex).toBe(-1);
  });

  it('should handle no data returned from getPremiumStudents', () => {
    premiumStudentsService.getPremiumStudents.and.returnValue(of({ data: {content: [], totalElements: 0} }));
    component.fetchPremiumStudents();
    fixture.detectChanges();
  
    expect(component.students.length).toBe(0);
    expect(component.totalElements).toBe(0);
  });

  // it('should not fetch search suggestions when keyword length < 3', () => {
  //   component.getSearchSuggestions('Ja');
  //   expect(courseService.getSuggestions).not.toHaveBeenCalled();
  // });

  it('should handle error when exportData fails', () => {
    const errorMessage = 'Export failed';
    component.students = [{id: 123}];
    premiumStudentsService.downloadExcel.and.returnValue(throwError(() => new Error(errorMessage)));
  
    spyOn(console, 'error');
    component.exportData();
    fixture.detectChanges();
  
    expect(console.error).toHaveBeenCalledWith('Export failed:', jasmine.any(Error));
  });
  it('should not trigger export if downloadExcel fails', () => {
    premiumStudentsService.downloadExcel.and.returnValue(throwError(() => new Error('Service error')));
    spyOn(document.body, 'appendChild').and.callThrough();
    component.students = [{id: 123}];
    component.exportData();
  
    expect(premiumStudentsService.downloadExcel).toHaveBeenCalled();
    expect(document.body.appendChild).not.toHaveBeenCalled();
  });
  
  it('should handle error when fetching premium students', () => {
    const errorMessage = 'Failed to fetch students';
    premiumStudentsService.getPremiumStudents.and.returnValue(throwError(() => new Error(errorMessage)));
  
    spyOn(console, 'error');
    component.fetchPremiumStudents();
    fixture.detectChanges();
  
    expect(component.students).toEqual([]);
    expect(component.totalElements).toEqual(0);
  });
  
})