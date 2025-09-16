import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NzDrawerPlacement } from 'ng-zorro-antd/drawer';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { Subscription, take } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import {
  contentTypeArray,
  courseTypeArray,
  courseTypeMapper,
  filterFeaturedArray,
  mapCourseType,
  ViewAllMap,
} from 'src/app/core/enums/course-status';
import { Filter } from 'src/app/core/models/filter.model';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { environment } from 'src/environments/environment.development';

@Component({
  selector: 'app-course-list',
  templateUrl: './course-list.component.html',
  styleUrls: ['./course-list.component.scss'],
})
export class CourseListComponent implements OnInit, OnDestroy {
  imageUrl = environment.imageUrl;
  _httpConstants: HttpConstants = new HttpConstants();
  courseList: Array<any> = [];
  categoryList: Array<any> = [];
  category?: any;
  selection?: string;
  totalElements?: number = 0;
  totalPages?: any;
  totalCoursePages?: any;
  courseTypeMapper = courseTypeMapper;
  showErrorMsg?: boolean = false;
  private queryParamSubscription: Subscription | undefined;
  courseTypes = courseTypeArray;
  contentTypes = contentTypeArray;
  featuredArray = filterFeaturedArray;
  visible = false;
  isMobileView = false;
  placement: NzDrawerPlacement = 'left';
  selectedCourseType?: string;
  selectedContentType?: string;
  selectedFeatureType?: string;
  filterPayload?: Filter;

  payLoad = {
    categoryId: null,
    courseLevelId: null,
    pageNo: 0,
    pageSize: 9,
  };
  viewAllpayLoad = {
    pageNo: 0,
    pageSize: 9,
    type: '',
  };

  filterRating = [];
  selectedRating: number | null = null;

  constructor(
    private _courseService: CourseService,
    private _messageService: MessageService,
    private _activatedRoute: ActivatedRoute,
    private _router: Router,
    private ngxUiLoaderService: NgxUiLoaderService,
    private metaService: Meta,
    private router: Router,
    private route: ActivatedRoute,
    private titleService: Title
  ) {
    this.filterPayload = new Filter();
  }

ngOnInit(): void {
  for (let i = 5; i > 0; i--) {
    this.filterRating.push({
      value: i,
    });
  }

  this.titleService.setTitle('Courses | Fastlearner.ai');
  this.metaService.updateTag({
    name: 'Courses',
    content: 'Courses page of Fastlearner.ai',
  });

  // Initialize default filter values
  this.selectedCourseType = 'ALL_COURSE';
  this.filterPayload = {
    pageNo: 0,
    pageSize: 9,
    categoriesId: [],
    courseType: null,
    feature: null,
    rating: null,
    contentType: null,
    search: null
  };

  this.getCategoryList();
  this.onResize(window);
  this.getRouteQueryParam(); // Uncomment this
}

  ngOnDestroy(): void {
    if (this.queryParamSubscription) {
      this.queryParamSubscription.unsubscribe();
    }
  }

  onChangeOfCategory(event: any) {
    this.clearCategoryParam();
    if (event) {
      this.payLoad.pageNo = 0;
      if (event == 0) {
        this.payLoad.categoryId = null;
        this.getCourseListByCategory();
        return;
      }
      this.payLoad.categoryId = event;
      this.getCourseListByCategory();
    } else {
      this.payLoad.pageNo = 0;
      this.payLoad.categoryId = null;
      this.getCourseListByCategory();
    }
  }

  onChangeOfLevel(event: any) {
    this.clearLevelParam();
    if (event) {
      this.payLoad.pageNo = 0;
      if (event == 0) {
        this.payLoad.courseLevelId = null;
        this.getCourseListByCategory();
        return;
      }
      this.payLoad.courseLevelId = +event;
      this.getCourseListByCategory();
    } else {
      this.payLoad.pageNo = 0;
      this.payLoad.courseLevelId = null;
      this.getCourseListByCategory();
    }
  }

getRouteQueryParam() {
  this.queryParamSubscription = this._activatedRoute.queryParams.subscribe(
    (params) => {
      // Handle category/selection params (existing logic)
      this.category = params['category'];
      this.selection = params['selection'];
      
      if (this.selection) {
        this.courseList = [];
        this.getCourses(this.selection);
        return;
      }

      // Restore filter state from query params
      if (Object.keys(params).length > 0) {
        // Page number
        if (params['pageNo']) {
          this.filterPayload.pageNo = +params['pageNo'];
        }

        // Categories
        if (params['categoriesId']) {
          const categoryIds = Array.isArray(params['categoriesId']) 
            ? params['categoriesId'] 
            : [params['categoriesId']];
          
          this.categoryList.forEach((category: any) => {
            category.selected = categoryIds.includes(category.id.toString());
          });
          this.filterPayload.categoriesId = categoryIds.map(id => +id);
        }

        // Course type
        if (params['courseType']) {
          this.selectedCourseType = params['courseType'];
          this.filterPayload.courseType = this.selectedCourseType === 'ALL_COURSE' 
            ? null 
            : this.selectedCourseType;
        }

        // Other filters
        this.filterPayload.feature = params['feature'] || null;
        this.filterPayload.rating = params['rating'] ? +params['rating'] : null;
        this.filterPayload.contentType = params['contentType'] || null;
        this.filterPayload.search = params['search'] || null;

        // Update UI selections
        this.selectedFeatureType = this.filterPayload.feature;
        this.selectedRating = this.filterPayload.rating;
        this.selectedContentType = this.filterPayload.contentType;

        // Get category by name if specified
        if (this.category) {
          this.getCategoryByName(this.category);
        } else {
          this.getAllCourses();
        }
      } else {
        // No params - load default courses
        this.getAllCourses();
      }
    }
  );
}

  @HostListener('window:resize', ['$event'])
  onResize(event?: any) {
    if (event?.innerWidth < 800 || event?.target?.innerWidth < 800) {
      this.isMobileView = true;
    } else {
      this.isMobileView = false;
    }
  }

  getCourses(selection?, fromShowMore?) {
    if (!fromShowMore) {
      const mappedType = mapCourseType(courseTypeMapper[selection]);
      // If total pages haven't been loaded yet, return early

      // this.viewAllpayLoad.type = mappedType || '';
      if (!mappedType) {
        this.categoryList?.forEach((category) => {
          if (category?.name == selection) {
            category.selected = true;
          } else {
            category.selected = false;
          }
        });
      } else if (mappedType == ViewAllMap.CATEGORY) {
        this.categoryList.forEach((category) => (category.selected = true));
      } else if (this.courseTypes.some((item) => item.value === mappedType)) {
        this.selectedCourseType = mappedType;
      } else if (this.featuredArray.some((item) => item.value === mappedType)) {
        this.selectedFeatureType = mappedType;
      }
    }
    this.applyFilter();
  }

getCategoryList() {
  this._courseService?.getCourseCategory()?.subscribe({
    next: (response: any) => {
      if (response?.status == this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.categoryList = response?.data.map((category: any) => ({
          ...category,
          selected: false,
        }));
        
        // After loading categories, check for category filter in URL
        this._activatedRoute.queryParams.pipe(take(1)).subscribe(params => {
          if (params['categoriesId']) {
            const categoryIds = Array.isArray(params['categoriesId']) 
              ? params['categoriesId'] 
              : [params['categoriesId']];
            
            this.categoryList.forEach((category: any) => {
              category.selected = categoryIds.includes(category.id.toString());
            });
          }
          
          // Now get courses with proper filters
          this.getRouteQueryParam();
        });
      }
    },
    error: (error: any) => {
      this.categoryList = [];
      this.getRouteQueryParam();
    },
  });
}

  getCourseListByCategory(flag?: boolean) {
    this.ngxUiLoaderService.start();
    this._courseService.getCoursesByCategory(this.payLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.showErrorMsg = false;
          this.ngxUiLoaderService.stop();
          this.totalPages = response?.data?.pages;
          if (!flag) {
            this.courseList = response?.data?.data;
            this.courseList?.forEach((res: any) => {
              res.courseDuration = this.convertSecondsToHoursAndMinutes(
                res.courseDuration
              );
            });
          } else {
            response?.data?.data.forEach((res: any) => {
              res.courseDuration = this.convertSecondsToHoursAndMinutes(
                res.courseDuration
              );
              this.courseList.push(res);
            });
          }
        } else if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.ngxUiLoaderService.stop();
          this.courseList = [];
          this.showErrorMsg = true;
        }
      },
      error: (error: any) => {
        this.showErrorMsg = true;
        this.ngxUiLoaderService.stop();
        if (
          error?.error?.status ==
          this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
        ) {
          this.courseList = [];
          // this._messageService.info('No Course Found');
        }
      },
    });
  }

  showMoreCourse() {
    if (this.totalCoursePages != this.filterPayload.pageNo + 1) {
      this.filterPayload.pageNo += 1;
      this.applyFilter();
    }
    // this.payLoad.pageNo += 1;
    // this.viewAllpayLoad.pageNo += 1;
    // if (this.selection) {
    //   this.getCourses(this.selection, true);
    // } else {
    //   this.getCourseListByCategory(true);
    // }
  }

  getCategoryByName(categoryName?: string) {
    this.categoryList.forEach((category) => {
      if (category.name === categoryName) {
        this.payLoad.categoryId = category.id;
        this.getCourseListByCategory();
        return;
      }
    });
  }

  clearCategoryParam() {
    this._router.navigate(['./'], {
      relativeTo: this._activatedRoute,
      queryParams: { category: null },
      queryParamsHandling: 'merge',
    });
  }
  clearLevelParam() {
    this._router.navigate(['./'], {
      relativeTo: this._activatedRoute,
      queryParams: { level: null },
      queryParamsHandling: 'merge',
    });
  }

  onFilterRatingChange(value: number): void {
    this.selectedRating = value;
  }

  selectCategory(category?: any) {
    category.selected = !category?.selected;
  }

  selectCourseType(courseType?: any) {
    this.selectedCourseType == courseType
      ? (this.selectedCourseType = null)
      : (this.selectedCourseType = courseType);
  }

  selectContentType(contentType?: any) {
    this.selectedContentType == contentType
      ? (this.selectedContentType = null)
      : (this.selectedContentType = contentType);
  }

  selectFeatureType(featureType?: any) {
    this.selectedFeatureType == featureType
      ? (this.selectedFeatureType = null)
      : (this.selectedFeatureType = featureType);
  }
applyFilter(fromFilter?: any, searchTerm?: string) {
  if (fromFilter) {
    this.filterPayload.pageNo = 0;
    this.courseList = [];
  }

  this.filterClose();

  // Update filter payload from current selections
  this.filterPayload.categoriesId = this.categoryList
    .filter((category: any) => category?.selected)
    .map((category: any) => category.id);

  this.filterPayload.courseType = this.selectedCourseType === 'ALL_COURSE' 
    ? null 
    : this.selectedCourseType;
  this.filterPayload.feature = this.selectedFeatureType;
  this.filterPayload.rating = this.selectedRating;
  this.filterPayload.contentType = this.selectedContentType;
  this.filterPayload.search = searchTerm?.trim() || null;

  // Build query params
  const queryParams: any = {};
  
  // Only include parameters that have values
  if (this.filterPayload.categoriesId?.length) {
    queryParams.categoriesId = this.filterPayload.categoriesId;
  }
  if (this.filterPayload.courseType) {
    queryParams.courseType = this.filterPayload.courseType;
  }
  if (this.filterPayload.feature) {
    queryParams.feature = this.filterPayload.feature;
  }
  if (this.filterPayload.rating) {
    queryParams.rating = this.filterPayload.rating;
  }
  if (this.filterPayload.contentType) {
    queryParams.contentType = this.filterPayload.contentType;
  }
  if (this.filterPayload.search) {
    queryParams.search = this.filterPayload.search;
  }

  // Navigate with new query params
  this._router.navigate([], {
    relativeTo: this._activatedRoute,
    queryParams,
    replaceUrl: true
  });

  // Get courses with current filters - SINGLE CALL
  this.getAllCourses();
}

  getAllCourses() {
  // Clear the array when starting a new fetch (pageNo = 0)
  if (this.filterPayload.pageNo === 0) {
    this.courseList = [];
  }

  this._courseService?.getAllCourses(this.filterPayload)?.subscribe({
    next: (response: any) => {
      if (response?.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.totalElements = response?.data?.totalElements;
        this.totalCoursePages = response?.data?.pages;

        // Create new array items with converted duration
        const newCourses = response?.data?.data.map(element => ({
          ...element,
          courseDuration: this.convertSecondsToHoursAndMinutes(element.courseDuration)
        }));

        // Either replace or append based on pagination
        if (this.filterPayload.pageNo === 0) {
          this.courseList = newCourses;
        } else {
          this.courseList = [...this.courseList, ...newCourses];
        }
      } else if (
        response?.status === 
        this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
      ) {
        this.courseList = [];
        this.totalCoursePages = 0;
        this.moveToCourseCards();
      }
    },
    error: (error: any) => {
      if (
        error?.error?.status === 
        this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
      ) {
        this.courseList = [];
        this.totalCoursePages = 0;
        this.moveToCourseCards();
      }
    },
  });
}
clearFilter() {
  // Reset all filter selections
  this.categoryList?.forEach((el) => {
    el.selected = false;
  });
  
  this.selectedCourseType = 'ALL_COURSE';
  this.selectedContentType = null;
  this.selectedFeatureType = null;
  this.selectedRating = null;
  
  // Reset filter payload
  this.filterPayload = {
    pageNo: 0,  // Reset to first page
    pageSize: 9,
    categoriesId: [],
    courseType: null,
    feature: null,
    rating: null,
    contentType: null,
    search: null
  };

  // Clear the course list before fetching new results
  this.courseList = [];

  // Clear all query parameters
  this._router.navigate([], {
    relativeTo: this._activatedRoute,
    queryParams: {},
    replaceUrl: true
  });
  this.filterClose();
  // Apply the cleared filters
  this.getAllCourses();
}
  moveToCourseCards() {
    const targetDiv = document.getElementById(`top-container`);
    if (targetDiv) {
      // Scroll smoothly to the target div
      targetDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  routeToCourseDetails(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl]);
  }

  routeToCourseDetailsContent(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl], {
      fragment: 'course-content',
    });
  }

  convertSecondsToHoursAndMinutes(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours === 0) {
      return `${minutes} minutes`;
    } else if (minutes === 0) {
      return `${hours} hours`;
    } else {
      return `${hours} hours ${minutes} minutes`;
    }
  }

  routeToInstructorProfile(event?: any) {
    this._router.navigate(['user/profile'], {
      queryParams: { url: event?.profileUrl || event },
    });
    if (event?.event) {
      event.event.stopPropagation();
      return;
    }
    if (typeof event?.stopPropagation === 'function') {
      event.stopPropagation();
    }
  }

  filterOpen(): void {
    this.visible = true;
  }

  filterClose(): void {
    this.visible = false;
  }
}
