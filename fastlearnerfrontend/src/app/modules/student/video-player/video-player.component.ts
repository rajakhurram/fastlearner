import {
  AfterViewInit,
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
  SimpleChanges,
  ViewChild,
  ElementRef,
  ViewEncapsulation,
} from '@angular/core';
import { IMediaElement, VgApiService } from '@videogular/ngx-videogular/core';
import { Subscription, debounceTime, fromEvent, interval } from 'rxjs';
import { CourseService } from 'src/app/core/services/course.service';
import { environment } from 'src/environments/environment.development';
import { ChangeDetectorRef } from '@angular/core';
import { ɵNzResultUnauthorizedComponent } from 'ng-zorro-antd/result';
import { YouTubePlayer } from '@angular/youtube-player';
import { ActivatedRoute, ParamMap } from '@angular/router';

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class VideoPlayerComponent implements OnInit, AfterViewInit, OnDestroy {
  baseUrl = environment.videoUrl;
  mobileView: boolean = false;
  tabletView: boolean = false;
  smallestDesktopView: boolean = false;
  smallDesktopView: boolean = false;
  mediumDesktopView: boolean = false;
  largeDesktopView: boolean = false;
  xlDesktopView: boolean = false;
  @Input() currentSelectedTopic: any;
  @Input() autoPlayValue?: boolean = true;
  @Input() isTooltipVisible?: boolean = true;
  @Input() customSize?: boolean = false;
  @Input() fromCourseInformation?: boolean = false;
  @Input() disableSubtitles: boolean = false;
  isInView: boolean = false;
  @Output() currentVideoTime = new EventEmitter<{ videoTime: any, isVideoPlaying: any, playbackRate: any }>();
  @Output() videoCompleteEmitter = new EventEmitter<any>();
  videoUrl: any;
  webVTTString: any;
  isVideoPlaying: boolean = true;
  youtubeVideo: boolean = false;
  vgPlayerApi: VgApiService;
  @ViewChild('player', { static: false }) player: YouTubePlayer;
  videoId: string | null = null;
  private _keyEventListener: Function;
  private completeSubscription: Subscription;
  @ViewChild('media') media: ElementRef;
  private currentTimeEmitterSubscription: Subscription;
  private readonly sizes = {
    mobile: { width: 370.04, height: 191.51 },
    tablet: { width: 691.2, height: 345.6 },
    smallestDesktop: { width: 650, height: 320 },
    smallDesktop: { width: 609.06, height: 304.52 },
    mediumDesktop: { width: 740, height: 429.33 },
    largeDesktop: { width: 900, height: 450 },
    XlDesktop: { width: 1000.8, height: 550 },
  };

    isScrubBarVisible: boolean = false;
  private scrubBarVisibilitySubscription: Subscription;
  private mouseMoveSubscription: Subscription;
  private controlsVisibilitySubscription: Subscription;
  private lastMouseMoveTime: number = 0;
  private readonly SCRUB_BAR_TIMEOUT = 3000; // 3 seconds timeout

  private customSizes = {
    mobile: { width: 310, height: 235 },
    tablet: { width: 250.2, height: 180 },
    smallestDesktop: { width: 300, height: 200 },
    smallDesktop: { width: 380, height: 251.55 },
    mediumDesktop: { width: 450, height: 270.3 },
    largeDesktop: { width: 600.8, height: 338 },
    XlDesktop: { width: 720.8, height: 450 },
  };

  youtubePlayer?: any = false;
  subtitleUrl: string | undefined;
  routeSubscription: Subscription;
currentSubtitle: string = '';
private subtitleUpdateInterval: any;
private lastCueText: string = '';

  constructor(
    private _renderer: Renderer2,
    private _courseService: CourseService,
    private _activatedRoute: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private elementRef: ElementRef
  ) {
    this._keyEventListener = this._renderer.listen(
      'document',
      'keydown',
      (event) => {
        if (
          event &&
          event.code === 'Space' &&
          this.isInView &&
          !this.isInputFocused()
        ) {
          event.preventDefault();
        }
        if (event.target !== document.body) {
          return;
        }
        switch (event.key) {
          case 'ArrowLeft':
            this.vgPlayerApi.currentTime -= 10;
            break;
          case 'ArrowRight':
            this.vgPlayerApi.currentTime += 10;
            break;
          case 'm':
            if (this.vgPlayerApi.volume === 0) {
              this.vgPlayerApi.volume = 1;
            } else {
              this.vgPlayerApi.volume = 0;
            }
            break;
          case 'k':
            if (this.isVideoPlaying) {
              this.vgPlayerApi.pause();
              this.isVideoPlaying = false;
            } else {
              this.vgPlayerApi.play();
              this.isVideoPlaying = true;
            }
            break;
          case 'f':
            this.vgPlayerApi.fsAPI.toggleFullscreen();
            break;
          default:
            break;
        }
      }
    );
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.checkIfInView();
  }

  ngAfterViewInit() {
    this.checkIfInView();
    this.setupScrubBarVisibilityTracking();
  }
  onTrackChange(event: any) {
    const track = event.srcElement.value;
    const cueElement = document.querySelector('video');

    if (track === 'null' || track === '') {
      cueElement.classList.add('no-track'); // Add class to hide cues
    } else if (track === 'en') {
      cueElement.classList.remove('no-track'); // Show English track normally
    }
  }
  generateSubtitleBlob() {
    const cueElement = document.querySelector('video');
    const videoElement = document.querySelector('video');
    const tracks = videoElement.querySelectorAll('track');

    // Create a Blob from the string
    if (this.currentSelectedTopic?.vttContent) {
      this.webVTTString = this.currentSelectedTopic?.vttContent;
      const blob = new Blob([this.webVTTString], { type: 'text/vtt' });

      // Create a URL for the Blob
      this.subtitleUrl = URL.createObjectURL(blob);
      const textTracks = videoElement.textTracks; // Get all text tracks
      for (let i = 0; i < textTracks.length; i++) {
        textTracks[i].mode = 'showing'; // Disable all text tracks
      }
    }
  }

  checkIfInView() {
    if (this.media) {
      const rect = this.media?.nativeElement?.getBoundingClientRect();
      const isVisible = rect?.top >= 0 && rect?.bottom <= window.innerHeight;

      if (isVisible && !this.isInView) {
        this.isInView = true;
        this.handleVideoPlayerInView();
      } else if (!isVisible && this.isInView) {
        this.isInView = false;
      }
    }
  }

  @HostListener('window:keydown', ['$event'])
  handleVideoPlayerInView(event?: KeyboardEvent) {
    if (this.isInputFocused()) {
      ɵNzResultUnauthorizedComponent;
    }
    if (
      event &&
      event.code === 'Space' &&
      this.isInView &&
      !this.isInputFocused()
    ) {
      event.preventDefault();
    }

    if (!event) {
    }
  }

  isInputFocused(): boolean {
    const activeElement = document.activeElement;
    return (
      activeElement &&
      (activeElement.tagName === 'INPUT' ||
        activeElement.tagName === 'TEXTAREA' ||
        activeElement.tagName === 'DIV')
    );
  }

  setScreenWidth(screenWidth: number) {
    const MOBILE_WIDTH = 500;
    const TABLET_WIDTH = 768;
    const SMALLEST_DESKTOP_WIDTH = 880;
    const SMALL_DESKTOP_WIDTH = 1024;
    const MEDIUM_DESKTOP_WIDTH = 1500;
    const LARGE_DESKTOP_WIDTH = 1700;
    const XL_DESKTOP_WIDTH = 2200;

    switch (true) {
      case screenWidth <= MOBILE_WIDTH:
        this.mobileView = true;
        this.tabletView = false;
        this.smallestDesktopView = false;
        this.smallDesktopView = false;
        this.mediumDesktopView = false;
        this.largeDesktopView = false;
        this.xlDesktopView = false;
        break;
      case screenWidth <= TABLET_WIDTH:
        this.mobileView = false;
        this.tabletView = true;
        this.smallestDesktopView = false;
        this.smallDesktopView = false;
        this.mediumDesktopView = false;
        this.largeDesktopView = false;
        this.xlDesktopView = false;
        break;

      case screenWidth <= SMALLEST_DESKTOP_WIDTH:
        this.mobileView = false;
        this.tabletView = false;
        this.smallestDesktopView = true;
        this.smallDesktopView = false;
        this.mediumDesktopView = false;
        this.largeDesktopView = false;
        this.xlDesktopView = false;
        break;

      case screenWidth <= SMALL_DESKTOP_WIDTH:
        this.mobileView = false;
        this.tabletView = false;
        this.smallestDesktopView = false;
        this.smallDesktopView = true;
        this.mediumDesktopView = false;
        this.largeDesktopView = false;
        this.xlDesktopView = false;
        break;
      case screenWidth <= MEDIUM_DESKTOP_WIDTH:
        this.mobileView = false;
        this.tabletView = false;
        this.smallestDesktopView = false;
        this.smallDesktopView = false;
        this.mediumDesktopView = true;
        this.largeDesktopView = false;
        this.xlDesktopView = false;
        break;
      case screenWidth <= LARGE_DESKTOP_WIDTH:
        this.mobileView = false;
        this.tabletView = false;
        this.smallestDesktopView = false;
        this.smallDesktopView = false;
        this.mediumDesktopView = false;
        this.largeDesktopView = true;
        this.xlDesktopView = false;
        break;
      case screenWidth <= XL_DESKTOP_WIDTH:
        this.mobileView = false;
        this.tabletView = false;
        this.smallestDesktopView = false;
        this.smallDesktopView = false;
        this.mediumDesktopView = false;
        this.largeDesktopView = false;
        this.xlDesktopView = true;
        break;
      default:
        this.mobileView = false;
        this.tabletView = false;
        this.smallestDesktopView = false;
        this.smallDesktopView = false;
        this.mediumDesktopView = false;
        this.largeDesktopView = false;
        this.xlDesktopView = false;

        break;
    }

    this.getViewSize();
  }

  getViewSize() {
    if (this.mobileView) {
      return this.customSize ? this.customSizes.mobile : this.sizes.mobile;
    } else if (this.tabletView) {
      return this.customSize ? this.customSizes.tablet : this.sizes.tablet;
    } else if (this.smallestDesktopView) {
      return this.customSize
        ? this.customSizes.smallestDesktop
        : this.sizes.smallestDesktop;
    } else if (this.smallDesktopView) {
      return this.customSize
        ? this.customSizes.smallDesktop
        : this.sizes.smallDesktop;
    } else if (this.mediumDesktopView) {
      return this.customSize
        ? this.customSizes.mediumDesktop
        : this.sizes.mediumDesktop;
    } else if (this.largeDesktopView) {
      return this.customSize
        ? this.customSizes.largeDesktop
        : this.sizes.largeDesktop;
    } else if (this.xlDesktopView) {
      return this.customSize
        ? this.customSizes.XlDesktop
        : this.sizes.XlDesktop;
    } else {
      return this.customSize
        ? { width: 1115.2, height: 400 }
        : { width: 1000, height: 500 }; // default size
    }
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    if (event.code === 'Space' && !this.isInputFocused()) {
      if (this.isVideoPlaying) {
        this.vgPlayerApi.pause();
        this.isVideoPlaying = false;
      } else {
        this.vgPlayerApi.play();
        this.isVideoPlaying = true;
      }
    }
  }
  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.setScreenWidth(
      event.innerWidth ? event.innerWidth : event.target.innerWidth
    );
  }

  ngOnInit(): void {
    const videoElement = document.querySelector('video');
    const tracks = videoElement?.querySelectorAll('track');
    const styleElement = document.createElement('style');
    this.routeSubscription = this._activatedRoute.paramMap.subscribe(
      (params: ParamMap) => {
        // styleElement.textContent = `
        // ::cue {
        //   visibility: hidden !important;
        // }
        //   `;
        document.head.appendChild(styleElement);
      }
    );
    const tag = document.createElement('script');
    tag.src = 'https://www.youtube.com/iframe_api';
    document.body.appendChild(tag);
    this.subtitleUrl = '';
    this.cdr.detectChanges();
    this.getSelectedTopic(this.currentSelectedTopic);
    this.getVideoPreviewUrl();
    this.invokeVideoTime();

    this.startCurrentTimeEmitter();
    this.setVideoTime(
      this.currentSelectedTopic?.seekTime,
      this.currentSelectedTopic?.isCompleted
    );

    this.onResize(window);
  }

  onStateChange(event: YT.OnStateChangeEvent) {
    if (event.data === YT.PlayerState.ENDED) {
      this.videoEnded();
    }
  }

  videoEnded() {
    // Your logic here
    this.videoCompleteEmitter.emit(true);
  }

  ngOnDestroy() {
    // Clean up subscriptions
    if (this.subtitleUpdateInterval) {
    clearInterval(this.subtitleUpdateInterval);
  }

    if (this.mouseMoveSubscription) {
      this.mouseMoveSubscription.unsubscribe();
    }
    if (this.controlsVisibilitySubscription) {
      this.controlsVisibilitySubscription.unsubscribe();
    }
    this.stopCurrentTimeEmitter();
    this.subtitleUrl = '';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentSelectedTopic']) {
      this.cdr.detectChanges();
      this.getSelectedTopic(this.currentSelectedTopic);
      this.subtitleUrl = '';
      this.webVTTString = '';
      this.getVideoPreviewUrl();
      this.generateSubtitleBlob();
      this.invokeVideoTime();
      this.setVideoTime(
        this.currentSelectedTopic?.seekTime,
        this.currentSelectedTopic?.isCompleted
      );
    }

    // this.setVideoTime(
    //   this.currentSelectedTopic?.seekTime,
    //   this.currentSelectedTopic?.isCompleted
    // );
    this.onResize(window);

    if (changes['isTooltipVisible']) {
      return;
    }
  }

  invokeVideoTime() {
    this._courseService.currentVideoTime$?.subscribe((res: any) => {
      if (res) {
        this.getCurrentVideoTime();
      }
    });
  }

  getSelectedTopic(selectedTopic: any) {
    this.currentSelectedTopic = selectedTopic;
    if (
      selectedTopic?.filename == 'YOUTUBE' ||
      this.checkYoutubeLink(selectedTopic?.videoUrl)
    ) {
      const videoUrl = selectedTopic?.videoUrl;
      this.videoId = this.extractYoutubeVideoId(videoUrl);
      this.youtubePlayer = true;
    } else {
      this.videoUrl = selectedTopic?.videoUrl;
      this.videoId = null; // Clear the videoId if not YouTube
      this.youtubePlayer = false;
    }
  }

  checkYoutubeLink(url?: any): boolean {
    const youtubeRegex =
      /^(https?\:\/\/)?((www|m)\.youtube\.com|youtu\.?be)\/.+$/;
    if (youtubeRegex.test(url)) {
      return true;
    }
    return false;
  }

  getVideoPreviewUrl() {
    this._courseService?.$previewVideo?.subscribe((res: any) => {
      if (res) {
        this.videoUrl = res;
      }
    });
  }

  onPlayerReady(event: any) {
      this.vgPlayerApi = event;

    const videoElement = document.querySelector('video') as HTMLVideoElement;
    const styleElement = document.createElement('style');
    this.vgPlayerApi = event;
    this.vgPlayerApi
      .getDefaultMedia()
      .subscriptions.loadedMetadata.subscribe((event) => {
        styleElement.textContent = `
        ::cue {
          visibility: visible !important;
        }
          `;
        const textTracks = videoElement.textTracks;
        if (textTracks.length) {
          // Get all text tracks
          for (let i = 0; i < textTracks.length; i++) {
            textTracks[i].mode = 'disabled'; // Disable all text tracks
          }
          this.generateSubtitleBlob();
        } else {
          styleElement.textContent = `
            ::cue {
              visibility: hidden !important;
            }
              `;
        }

        videoElement.addEventListener('canplay', () => {
            videoElement.currentTime = 0.999; 
        }, { once: true });

        videoElement.addEventListener('seeked', () => {
            videoElement.pause(); // Keep it paused after seeking
        }, { once: true });
        
    });

    this.vgPlayerApi
    .getDefaultMedia()
    .subscriptions.loadedMetadata.subscribe(() => {
      setTimeout(() => {
        this.setupCustomSubtitleTracking();
      }, 1000);
    });
    this.completeSubscription = this.vgPlayerApi
      .getDefaultMedia()
      .subscriptions.ended?.subscribe((value) => {
        this.videoCompleteEmitter.emit(true);
        // Perform any actions you want to do when the video completes
      });
  }

  getCurrentVideoTime() {
    if (this.vgPlayerApi && !this.youtubePlayer) {
      const currentTime = this.vgPlayerApi?.getDefaultMedia()?.currentTime;
      this.currentVideoTime.emit({ videoTime: currentTime, isVideoPlaying: this.isVideoPlaying, playbackRate: this.vgPlayerApi?.playbackRate });
    }
    if (this.player && this.youtubePlayer) {
      const currentTime = this.player.getCurrentTime();
      this.currentVideoTime.emit({ videoTime: currentTime, isVideoPlaying: this.isVideoPlaying, playbackRate: this.vgPlayerApi?.playbackRate });
    }
  }

  getDuration() {
    if (this.vgPlayerApi) {
      this.vgPlayerApi
        .getDefaultMedia()
        .subscriptions.seeked?.subscribe((res: any) => {});
    }
  }

  getTime() {
    if (this.vgPlayerApi) {
      // Get the current time of the video
      const currentTime = this.vgPlayerApi.getDefaultMedia().time;
    }
  }

  togglePlayPause(): void {
    const videoElement: HTMLVideoElement = this.media.nativeElement;
    if (videoElement.paused) {
      videoElement.play();
    } else {
      videoElement.pause();
    }
  }

  toggleFullscreen(): void {
    const videoElement: HTMLVideoElement = this.media.nativeElement;

    if (videoElement.requestFullscreen) {
      videoElement.requestFullscreen();
    }
  }

  startCurrentTimeEmitter(): void {
    this.stopCurrentTimeEmitter(); // Ensure previous interval is cleared

    this.currentTimeEmitterSubscription = interval(1000).subscribe(() => {
      this.getCurrentVideoTime();
    });
  }

  // This method will stop emitting current video time
  stopCurrentTimeEmitter(): void {
    if (this.currentTimeEmitterSubscription) {
      this.currentTimeEmitterSubscription.unsubscribe();
    }
  }

  setVideoTime(timeInSeconds: number, isCompleted?: any): void {
    setTimeout(() => {
      if (
        this.vgPlayerApi &&
        timeInSeconds != null &&
        !isCompleted &&
        !this.youtubePlayer
      ) {
        this.vgPlayerApi.seekTime(timeInSeconds);
      } else if (this.player && this.youtubePlayer) {
        this.player.seekTo(timeInSeconds, true);
      }
    }, 500);
  }

  extractYoutubeVideoId(url: string): string | null {
    const regex = /(?<=\?v=)[^&]+/;
    const match = url.match(regex);
    return match ? match[0] : null;
  }

   setupScrubBarVisibilityTracking() {
  // Method 1: Track mouse movement
  this.mouseMoveSubscription = fromEvent(document, 'mousemove')
    .subscribe(() => {
      this.lastMouseMoveTime = Date.now();
      this.isScrubBarVisible = true;
      this.updateSubtitlePosition();
      
      // Hide after timeout
      setTimeout(() => {
        const timeSinceLastMove = Date.now() - this.lastMouseMoveTime;
        if (timeSinceLastMove >= this.SCRUB_BAR_TIMEOUT) {
          this.isScrubBarVisible = false;
          this.updateSubtitlePosition();
        }
      }, this.SCRUB_BAR_TIMEOUT);
    });

  // Method 2: Track touch events for mobile
  fromEvent(document, 'touchstart')
    .subscribe(() => {
      this.lastMouseMoveTime = Date.now();
      this.isScrubBarVisible = true;
      this.updateSubtitlePosition();
      
      setTimeout(() => {
        const timeSinceLastMove = Date.now() - this.lastMouseMoveTime;
        if (timeSinceLastMove >= this.SCRUB_BAR_TIMEOUT) {
          this.isScrubBarVisible = false;
          this.updateSubtitlePosition();
        }
      }, this.SCRUB_BAR_TIMEOUT);
    });

  // Always show scrub bar when video is paused
  if (this.vgPlayerApi) {
    this.vgPlayerApi
      .getDefaultMedia()
      .subscriptions.pause
      .subscribe(() => {
        this.isScrubBarVisible = true;
        this.updateSubtitlePosition();
      });
    
    this.vgPlayerApi
      .getDefaultMedia()
      .subscriptions.play
      .subscribe(() => {
        // Keep scrub bar visible for a moment when playing starts
        setTimeout(() => {
          this.isScrubBarVisible = false;
          this.updateSubtitlePosition();
        }, 2000);
      });
  }
}


  setupVideogularAutohideTracking() {
    // Monitor the controls container visibility
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
          const target = mutation.target as HTMLElement;
          const isHidden = target.classList.contains('ng-hide');
          this.isScrubBarVisible = !isHidden;
          this.updateSubtitlePosition();
        }
      });
    });

    // Observe the controls container
    const controlsContainer = document.querySelector('vg-controls');
    if (controlsContainer) {
      observer.observe(controlsContainer, {
        attributes: true,
        attributeFilter: ['class']
      });
    }
  }

  updateSubtitlePosition() {
  // Method 1: Update CSS variable for browser that support it
  document.documentElement.style.setProperty(
    '--subtitle-bottom-pos', 
    this.isScrubBarVisible ? '60px' : '30px'
  );
  
  // Method 2: Update CSS variable for WebKit
  document.documentElement.style.setProperty(
    '--subtitle-translate-y',
    this.isScrubBarVisible ? '-30px' : '0px'
  );
  
  // Method 3: Update custom subtitle display classes
  const customSubtitle = document.querySelector('.custom-subtitle-display');
  if (customSubtitle) {
    if (this.isScrubBarVisible) {
      customSubtitle.classList.add('subtitles-up');
      customSubtitle.classList.remove('subtitles-down');
    } else {
      customSubtitle.classList.add('subtitles-down');
      customSubtitle.classList.remove('subtitles-up');
    }
  }
  
  // Method 4: Force update through inline styles (most reliable)
  this.updateCueStylesDirectly();
}

updateCueStylesDirectly() {
  // Create or update a style element with dynamic cue positioning
  let styleElement = document.getElementById('dynamic-cue-styles');
  
  if (!styleElement) {
    styleElement = document.createElement('style');
    styleElement.id = 'dynamic-cue-styles';
    document.head.appendChild(styleElement);
  }
  
  const offset = this.isScrubBarVisible ? '-60px' : '-30px';
  
  // This is the most reliable way to update cue positioning
  styleElement.textContent = `
    ::cue {
      transform: translateY(${offset}) !important;
    }
    
    video::cue {
      transform: translateY(${offset}) !important;
    }
  `;
}

setupCustomSubtitleTracking() {
  const videoElement = this.media?.nativeElement;
  
  if (!videoElement) return;
  
  // Clear any existing interval
  if (this.subtitleUpdateInterval) {
    clearInterval(this.subtitleUpdateInterval);
  }
  
  // Track active subtitle cues
  this.subtitleUpdateInterval = setInterval(() => {
    const textTracks = videoElement.textTracks;
    
    if (textTracks && textTracks.length > 0) {
      for (let i = 0; i < textTracks.length; i++) {
        const track = textTracks[i];
        
        if (track.mode === 'showing') {
          const activeCues = Array.from(track.activeCues || []);
          
          if (activeCues.length > 0) {
            const cue = activeCues[0] as VTTCue;
            const cueText = cue.text;
            
            if (cueText !== this.lastCueText) {
              this.lastCueText = cueText;
              this.currentSubtitle = cueText;
              
              // Update custom subtitle display
              this.updateCustomSubtitleDisplay(cueText);
            }
          } else {
            this.currentSubtitle = '';
            this.lastCueText = '';
            this.updateCustomSubtitleDisplay('');
          }
        }
      }
    }
  }, 100); // Check every 100ms
}
updateCustomSubtitleDisplay(text: string) {
  // Find or create custom subtitle display element
  let customSubtitle = document.querySelector('.custom-subtitle-display');
  const videoContainer = this.elementRef.nativeElement.querySelector('.video-container');
  
  if (!customSubtitle && videoContainer) {
    customSubtitle = document.createElement('div');
    customSubtitle.className = 'custom-subtitle-display subtitles-down';
    videoContainer.appendChild(customSubtitle);
  }
  
  if (customSubtitle) {
    customSubtitle.textContent = text;
    
    // Update position class based on scrub bar visibility
    if (this.isScrubBarVisible) {
      customSubtitle.classList.add('subtitles-up');
      customSubtitle.classList.remove('subtitles-down');
    } else {
      customSubtitle.classList.add('subtitles-down');
      customSubtitle.classList.remove('subtitles-up');
    }
  }
}
  updateCueStyles() {
    let cueStyleElement = document.querySelector('style[cue-styles]');
    
    if (!cueStyleElement) {
      cueStyleElement = document.createElement('style');
      cueStyleElement.setAttribute('cue-styles', '');
      document.head.appendChild(cueStyleElement);
    }
    
    const offset = this.isScrubBarVisible ? '-60px' : '-30px';
    
    cueStyleElement.textContent = `
      ::cue {
        transform: translateY(${offset}) !important;
      }
      
      @media screen and (max-width: 1400px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-70px' : '-35px'}) !important;
        }
      }
      
      @media screen and (max-width: 1130px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-80px' : '-40px'}) !important;
        }
      }
      
      @media screen and (max-width: 991px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-90px' : '-45px'}) !important;
        }
      }
      
      @media screen and (max-width: 879px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-100px' : '-50px'}) !important;
        }
      }
      
      @media screen and (max-width: 750px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-110px' : '-55px'}) !important;
        }
      }
      
      @media screen and (max-width: 620px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-120px' : '-60px'}) !important;
        }
      }
      
      @media screen and (max-width: 472px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-130px' : '-65px'}) !important;
        }
      }
      
      @media screen and (max-width: 422px) {
        ::cue {
          transform: translateY(${this.isScrubBarVisible ? '-140px' : '-70px'}) !important;
        }
      }
    `;
  }

  // Add this method to reset subtitle position on fullscreen
  setupFullscreenTracking() {
    if (this.vgPlayerApi) {
      this.vgPlayerApi.fsAPI.onChangeFullscreen.subscribe((isFullscreen: boolean) => {
        // Adjust offsets for fullscreen mode
        setTimeout(() => this.updateSubtitlePosition(), 100);
      });
    }
  }
}
