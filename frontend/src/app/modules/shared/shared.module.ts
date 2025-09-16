import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './footer/footer.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { LandingPageComponent } from '../pages/landing-page/landing-page.component';
import { BannerAnimationComponent } from '../animations/banner-animation/banner-animation.component';
import { SecretAnimationComponent } from '../animations/secret-animation/secret-animation.component';
import { InitialCharactorPipe } from 'src/app/core/pipes/initial-charactor.pipe';
import { VideoPlayerComponent } from '../student/video-player/video-player.component';
import { VgCoreModule } from '@videogular/ngx-videogular/core';
import { VgControlsModule } from '@videogular/ngx-videogular/controls';
import { VgOverlayPlayModule } from '@videogular/ngx-videogular/overlay-play';
import { VgBufferingModule } from '@videogular/ngx-videogular/buffering';
import { TextTruncate } from 'src/app/core/pipes/text-truncate.pipe';
import { YouTubePlayerModule } from '@angular/youtube-player';
import { ButtonComponent } from './button/button.component';
import { SearchFilterComponent } from './search-filter/search-filter.component';
import { TableComponent } from './table/table.component';
import { CardSliderComponent } from './card-slider/card-slider.component';
import { SliderButtonComponent } from './slider-button/slider-button.component';
import { StaticCardComponent } from './static-card/static-card.component';
import { ViewAllButtonComponent } from './view-all-button/view-all-button.component';
import { CourseCardComponent } from './course-card/course-card.component';
import { AssignCourseAffliateComponent } from './assign-course-affliate/assign-course-affliate.component';
import { PremiumCourseAssignCardsComponent } from './premium-course-assign-cards/premium-course-assign-cards.component';
import { ContainerComponent } from './container/container.component';
import { FormsModule } from '@angular/forms';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { LazyLoadDirective } from '../directives/lazy-load.directive';
import { DropdownComponent } from './dropdown/dropdown.component';
import { InputComponent } from './input/input.component';
import { PreviewUploadComponent } from './preview-upload/preview-upload.component';
import { SearchDropdownComponent } from './search-dropdown/search-dropdown.component';
import { ClassAssessmentDropdownComponent } from './class-assessment-dropdown/class-assessment-dropdown.component';
import { PdfViewerComponent } from './pdf-viewer/pdf-viewer.component';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { AiGraderLandingPageComponent } from '../pages/ai-grader-landing-page/ai-grader-landing-page.component';
import { NzCarouselModule } from "ng-zorro-antd/carousel";
import { AiGraderLoaderComponent } from './ai-grader-loader/ai-grader-loader.component';

@NgModule({
  declarations: [
    NavbarComponent,
    FooterComponent,
    LandingPageComponent,
    BannerAnimationComponent,
    SecretAnimationComponent,
    InitialCharactorPipe,
    VideoPlayerComponent,
    TextTruncate,
    ButtonComponent,
    SearchFilterComponent,
    TableComponent,
    CardSliderComponent,
    SliderButtonComponent,
    StaticCardComponent,
    ButtonComponent,
    ViewAllButtonComponent,
    CourseCardComponent,
    AssignCourseAffliateComponent,
    PremiumCourseAssignCardsComponent,
    ContainerComponent,
    InputComponent,
    PreviewUploadComponent,
    DropdownComponent,
    LazyLoadDirective,
    SearchDropdownComponent,
    ClassAssessmentDropdownComponent,
    PdfViewerComponent,
    AiGraderLandingPageComponent,
    AiGraderLoaderComponent
  ],

  imports: [
    CommonModule,
    AntDesignModule,
    VgCoreModule,
    VgControlsModule,
    VgOverlayPlayModule,
    VgBufferingModule,
    YouTubePlayerModule,
    NzInputModule,
    NzSelectModule,
    FormsModule,
    PdfViewerModule,
    NzCarouselModule
],
  exports: [
    NavbarComponent,
    FooterComponent,
    LandingPageComponent,
    BannerAnimationComponent,
    SecretAnimationComponent,
    InitialCharactorPipe,
    VideoPlayerComponent,
    TextTruncate,
    ButtonComponent,
    SearchFilterComponent,
    TableComponent,
    CardSliderComponent,
    SliderButtonComponent,
    StaticCardComponent,
    ButtonComponent,
    ViewAllButtonComponent,
    CourseCardComponent,
    AssignCourseAffliateComponent,
    InputComponent,
    PreviewUploadComponent,
    DropdownComponent,
    SearchDropdownComponent,
    ClassAssessmentDropdownComponent,
    PdfViewerComponent,
    AiGraderLoaderComponent
  ],
})
export class SharedModule {}
