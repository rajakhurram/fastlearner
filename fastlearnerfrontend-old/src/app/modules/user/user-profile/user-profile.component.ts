import {
  Component,
  HostListener,
  OnDestroy,
  OnInit,
  ViewContainerRef,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { User } from 'src/app/core/models/user.model';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { UserService } from 'src/app/core/services/user.service';
import { SubmissionModalComponent } from '../../dynamic-modals/submission-modal/submission-modal.component';
import { Subscription } from 'rxjs';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzUploadChangeParam, NzUploadXHRArgs } from 'ng-zorro-antd/upload';
import { FileManager } from 'src/app/core/services/file-manager.service';
import {
  FacebookLoginProvider,
  GoogleLoginProvider,
  SocialAuthService,
} from '@abacritt/angularx-social-login';
import { ImageUploaderModalComponent } from '../../dynamic-modals/image-uploader-modal/image-uploader-modal.component';
import { ProfileUploaderModalComponent } from '../../dynamic-modals/profile-uploader-modal/profile-uploader-modal.component';
import { Providers } from 'src/app/core/enums/providers';
import { Role } from 'src/app/core/enums/Role';
import { AuthService } from 'src/app/core/services/auth.service';
import { AngularEditorConfig } from '@kolkov/angular-editor';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss'],
})
export class UserProfileComponent implements OnInit, OnDestroy {
  _httpConstants: HttpConstants = new HttpConstants();
  afterCloseSubscription: Subscription;
  fullWidth: boolean = true;
  user: User = new User();
  socialUser?: any = false;
  userNameValidationMsg?: any;
  currentLoggedInUserDetails: any;

  editorConfig: AngularEditorConfig = {
    editable: true,
    spellcheck: true,
    height: 'auto',
    minHeight: '150px',
    maxHeight: 'auto',
    width: 'auto',
    minWidth: '0',
    translate: 'yes',
    enableToolbar: true,
    showToolbar: true,
    placeholder: 'Enter text here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    toolbarHiddenButtons: [
      // Hides all other buttons except bold, italic, underline, and image
      [
        'strikeThrough',
        'subscript',
        'superscript',
        'justifyLeft',
        'justifyCenter',
        'justifyRight',
        'justifyFull',
        'indent',
        'outdent',
        'insertOrderedList',
        'insertUnorderedList',
        'heading',
        'fontSize',
        'textColor',
        'backgroundColor',
        'link',
        'unlink',
        'insertVideo',
        'insertHorizontalRule',
        'removeFormat',
        'toggleEditorMode',
        'undo',
        'redo',
        'fontName',
        'insertImage',
      ],
    ],
  };

  constructor(
    private _userService: UserService,
    private _messageService: MessageService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _sharedService: SharedService,
    private _cacheService: CacheService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _fileManagerService: FileManager,
    private _socialAuthService: SocialAuthService,
    private _authService: AuthService
  ) {}

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    const screenWidth = event.target.innerWidth;
    if (screenWidth > 768) {
      this.fullWidth = true;
    } else {
      this.fullWidth = false;
    }
  }

  ngOnInit(): void {
    this.getUserCompleteProfile();
    this.getAuthenticUserState();
  }

  getUserCompleteProfile() {
    this._userService.getUserProfile().subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.user = response?.data;
          // this._messageService.success(response?.message);
        }
      },
      error: (error: any) => {},
    });
  }

  updateUserProfile() {
    if (
      (this.user?.fullName && this.user.fullName.length <= 50) ||
      (this.user?.specialization && this.user.specialization.length <= 500) ||
      (this.user?.qualification && this.user.qualification.length <= 500) ||
      (this.user?.experience && this.user.experience.length <= 500) ||
      (this.user?.headline && this.user.headline.length <= 100) ||
      (this.user?.aboutMe && this.user.aboutMe.length <= 3000)
    ) {
      this._userService?.updateUserProfile(this.user)?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            // this._messageService.success(response?.message);
            const loggedInUserDetails = JSON.parse(
              this._cacheService?.getDataFromCache('loggedInUserDetails')
            );
            localStorage.setItem('userProfile', JSON.stringify(this.user));
            loggedInUserDetails.name = this.user.fullName;
            loggedInUserDetails.email = this.user.email;
            this._cacheService.saveInCache(
              'loggedInUserDetails',
              JSON.stringify(loggedInUserDetails)
            );
            this._sharedService.updateNavDetail();

            this.openShareCourseModal();
          }
        },
        error: (error: any) => {},
      });
    }
  }
  openShareCourseModal() {
    const modal = this._modal.create({
      nzContent: SubmissionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: 'Profile has been updated',
        buttonText: 'Done',
      },
      nzFooter: null,
      nzKeyboard: true,
    });

    this.afterCloseSubscription = modal?.afterClose.subscribe(() => {
      this.routeToStudentLandingPage();
    });
  }

  routeToStudentLandingPage() {
    this._router.navigate(['student']);
  }

  routeToChangePassword() {
    this._router.navigate(['../change-password'], {
      relativeTo: this._activatedRoute,
    });
  }

  ngOnDestroy(): void {
    if (this.afterCloseSubscription) {
      this.afterCloseSubscription.unsubscribe();
    }
  }

  customRequestImage = (item: NzUploadXHRArgs): Subscription => {
    const file = item.file as unknown as File;
    return this._fileManagerService
      .uploadFile(file, 'PROFILE_IMAGE')
      .subscribe({
        next: (response: any) => {
          this.user.profilePicture = response?.data;
        },
        error: (error: any) => {},
      });
  };

  getAuthenticUserState() {
    this._socialAuthService?.authState?.subscribe((user) => {
      if (user) {
        this.socialUser = true;
      }
    });
  }

  openImageUploaderModal(): void {
    const modalRef = this._modal.create({
      nzTitle: '',
      nzContent: ProfileUploaderModalComponent,
      nzFooter: null,
    });
    this.afterCloseSubscription = modalRef.afterClose.subscribe((data: any) => {
      if (data && data.profilePicture) {
        this.user.profilePicture = data.profilePicture;
      }
    });
  }

  checkLength(event: KeyboardEvent): void {
    const key = event.key;
    const isAllowed = /^[a-zA-Z\s!"#$%&'()*+,\-.\/:;<=>?@[\\\]^_`{|}~]*$/.test(
      key
    );
    if (!isAllowed) {
      event.preventDefault();
    }
  }

  checkInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.value.length > 50) {
      this.userNameValidationMsg =
        'Full Name cannot be greater than 50 characters long.';
    } else {
      this.userNameValidationMsg = '';
    }
  }
}
