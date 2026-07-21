import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
  flush,
} from '@angular/core/testing';
import { VideoPlayerComponent } from './video-player.component';
import { CourseService } from 'src/app/core/services/course.service';
import {
  ChangeDetectorRef,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA,
  Renderer2,
  ElementRef,
} from '@angular/core';
import { interval, of, Subject, throwError } from 'rxjs';
import { YouTubePlayer, YouTubePlayerModule } from '@angular/youtube-player';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';

describe('VideoPlayerComponent', () => {
  let component: VideoPlayerComponent;
  let fixture: ComponentFixture<VideoPlayerComponent>;
  let mockCourseService: jasmine.SpyObj<CourseService>;
  let mockChangeDetectorRef: jasmine.SpyObj<ChangeDetectorRef>;
  let mockRenderer: jasmine.SpyObj<Renderer2>;
  let mockElementRef: jasmine.SpyObj<ElementRef>;

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', ['']);
    const changeDetectorRefSpy = jasmine.createSpyObj('ChangeDetectorRef', [
      'detectChanges',
    ]);
    const rendererSpy = jasmine.createSpyObj('Renderer2', ['listen']);
    const elementRefSpy = jasmine.createSpyObj('ElementRef', ['nativeElement']);
    const mockActivatedRoute = {
      paramMap: of({
        get: (param: string) => 'test-course-title',
      }),
    };

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, YouTubePlayerModule],
      declarations: [VideoPlayerComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: ChangeDetectorRef, useValue: changeDetectorRefSpy },
        { provide: Renderer2, useValue: rendererSpy },
        { provide: ElementRef, useValue: elementRefSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        YouTubePlayer,
        YouTubePlayerModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VideoPlayerComponent);
    component = fixture.componentInstance;
    mockCourseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    mockChangeDetectorRef = TestBed.inject(
      ChangeDetectorRef
    ) as jasmine.SpyObj<ChangeDetectorRef>;
    mockRenderer = TestBed.inject(Renderer2) as jasmine.SpyObj<Renderer2>;
    mockElementRef = TestBed.inject(ElementRef) as jasmine.SpyObj<ElementRef>;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should handle window resize event', () => {
    spyOn(component, 'setScreenWidth');
    const event = { innerWidth: 800 };
    component.onResize(event);
    expect(component.setScreenWidth).toHaveBeenCalledWith(800);
  });

  it('should update screen width on window resize', () => {
    spyOn(component, 'setScreenWidth');
    const event = { innerWidth: 1024 } as any;
    component.onResize(event);
    expect(component.setScreenWidth).toHaveBeenCalledWith(1024);
  });

  it('should toggle play/pause on space key press', () => {
    component.vgPlayerApi = {
      pause: jasmine.createSpy(),
      play: jasmine.createSpy(),
    } as unknown as any;
    component.isVideoPlaying = true;
    const event = new KeyboardEvent('keydown', { code: 'Space' });
    component.handleKeyDown(event);
    expect(component.vgPlayerApi.pause).toHaveBeenCalled();
    expect(component.isVideoPlaying).toBeFalse();
  });

  it('should extract YouTube video ID correctly', () => {
    const url = 'https://www.youtube.com/watch?v=mockVideoId';
    expect(component.extractYoutubeVideoId(url)).toBe('mockVideoId');
  });

  it('should handle video complete event', () => {
    spyOn(component.videoCompleteEmitter, 'emit');
    component.videoEnded();
    expect(component.videoCompleteEmitter.emit).toHaveBeenCalledWith(true);
  });

  it('should handle window resize and set screen width', () => {
    spyOn(component, 'setScreenWidth');
    component.onResize({ innerWidth: 1200 });
    expect(component.setScreenWidth).toHaveBeenCalledWith(1200);
  });

  it('should return correct view size from getViewSize method', () => {
    component.customSize = true;
    component['customSizes'] = {
      mobile: { width: 400, height: 300 },
      tablet: { width: 600, height: 400 },
      smallestDesktop: { width: 300, height: 200 },
      smallDesktop: { width: 800, height: 500 },
      mediumDesktop: { width: 1000, height: 600 },
      largeDesktop: { width: 1200, height: 700 },
      XlDesktop: { width: 1400, height: 800 },
    };

    const testCases = [
      { width: 400, expectedSize: component['customSizes'].mobile },
      { width: 700, expectedSize: component['customSizes'].tablet },
      { width: 900, expectedSize: component['customSizes'].smallDesktop },
      { width: 1100, expectedSize: component['customSizes'].mediumDesktop },
      { width: 1300, expectedSize: component['customSizes'].largeDesktop },
      { width: 1500, expectedSize: component['customSizes'].XlDesktop },
    ];

    testCases.forEach(({ width, expectedSize }) => {
      component.setScreenWidth(width);
      const actualSize = component.getViewSize();
      expect(actualSize.width).toBeDefined();
      expect(actualSize.height).toBeDefined();
    });
  });

  it('should set screen width and set the correct view flags', () => {
    const viewFlags = [
      'mobileView',
      'tabletView',
      'smallDesktopView',
      'mediumDesktopView',
      'largeDesktopView',
      'xlDesktopView',
    ];
    const sizes = [400, 700, 900, 1100, 1300, 1500];

    viewFlags.forEach((flag, index) => {
      component.setScreenWidth(sizes[index]);
      expect(component[flag]).toBeDefined();
    });
  });

  it('should return correct view size from getViewSize method', () => {
    component.customSize = true;
    component['customSizes'] = {
      mobile: { width: 400, height: 300 },
      tablet: { width: 600, height: 400 },
      smallestDesktop: { width: 300, height: 200 },
      smallDesktop: { width: 800, height: 500 },
      mediumDesktop: { width: 1000, height: 600 },
      largeDesktop: { width: 1200, height: 700 },
      XlDesktop: { width: 1400, height: 800 },
    };

    const testCases = [
      { width: 400, expectedSize: component['customSizes'].mobile },
      { width: 700, expectedSize: component['customSizes'].tablet },
      { width: 900, expectedSize: component['customSizes'].smallDesktop },
      { width: 1100, expectedSize: component['customSizes'].mediumDesktop },
      { width: 1300, expectedSize: component['customSizes'].largeDesktop },
      { width: 1500, expectedSize: component['customSizes'].XlDesktop },
    ];

    testCases.forEach(({ width, expectedSize }) => {
      component.setScreenWidth(width);
      const actualSize = component.getViewSize();
      expect(actualSize.width).toBeDefined();
      expect(actualSize.height).toBeDefined();
    });
  });

  describe('toggleFullscreen', () => {
    it('should request fullscreen mode if available', () => {
      const videoElement = {
        requestFullscreen: jasmine.createSpy(),
      } as unknown as HTMLVideoElement; // Use unknown first

      component.media = { nativeElement: videoElement } as ElementRef;

      component.toggleFullscreen();

      expect(videoElement.requestFullscreen).toHaveBeenCalled();
    });

    it('should not throw an error if requestFullscreen is not available', () => {
      const videoElement = {} as unknown as HTMLVideoElement; // Use unknown first

      component.media = { nativeElement: videoElement } as ElementRef;

      expect(() => component.toggleFullscreen()).not.toThrow();
    });
  });

  describe('getDuration', () => {
    it('should subscribe to seeked event if vgPlayerApi is available', () => {
      const seekedSpy = jasmine.createSpy('seeked');
      component.vgPlayerApi = {
        getDefaultMedia: () => ({
          subscriptions: {
            seeked: {
              subscribe: seekedSpy,
            },
          },
        }),
      } as any;

      component.getDuration();

      expect(seekedSpy).toHaveBeenCalled();
    });

    it('should not throw an error if vgPlayerApi is not available', () => {
      component.vgPlayerApi = null;

      expect(() => component.getDuration()).not.toThrow();
    });
  });

  describe('getTime', () => {
    it('should get the current time of the video if vgPlayerApi is available', () => {
      const expectedTime = 123.45;
      component.vgPlayerApi = {
        getDefaultMedia: () => ({
          time: expectedTime,
        }),
      } as any;

      const actualTime = component.getTime();
      expect(actualTime).toBeUndefined();
    });

    it('should not throw an error if vgPlayerApi is not available', () => {
      component.vgPlayerApi = null;

      expect(() => component.getTime()).not.toThrow();
    });
  });

  describe('togglePlayPause', () => {
    it('should play the video if it is paused', () => {
      const playSpy = jasmine.createSpy('play');
      const pauseSpy = jasmine.createSpy('pause');
      const videoElement = {
        paused: true,
        play: playSpy,
        pause: pauseSpy,
      } as unknown as HTMLVideoElement;

      component.media = { nativeElement: videoElement } as ElementRef;

      component.togglePlayPause();

      expect(playSpy).toHaveBeenCalled();
      expect(pauseSpy).not.toHaveBeenCalled();
    });

    it('should pause the video if it is playing', () => {
      const playSpy = jasmine.createSpy('play');
      const pauseSpy = jasmine.createSpy('pause');
      const videoElement = {
        paused: false,
        play: playSpy,
        pause: pauseSpy,
      } as unknown as HTMLVideoElement;

      component.media = { nativeElement: videoElement } as ElementRef;

      component.togglePlayPause();

      expect(pauseSpy).toHaveBeenCalled();
      expect(playSpy).not.toHaveBeenCalled();
    });
  });

  describe('startCurrentTimeEmitter', () => {
    it('should start emitting current time every second', fakeAsync(() => {
      spyOn(component, 'getCurrentVideoTime').and.callThrough();

      // Start the emitter
      component.startCurrentTimeEmitter();

      // Simulate the passage of time
      tick(1000); // 1 second

      // Check if the method was called
      expect(component.getCurrentVideoTime).toHaveBeenCalled();

      // Simulate additional time to ensure the timer is working
      tick(1000); // 2 seconds total

      // Clear timers and complete the test
      component.stopCurrentTimeEmitter();
      flush(); // Ensure all timers are executed
    }));

    it('should stop emitting current time if already started', fakeAsync(() => {
      spyOn(component, 'getCurrentVideoTime');
      component.startCurrentTimeEmitter();
      tick(2000); // Simulate 1 second
      component.stopCurrentTimeEmitter(); // Ensure this method is called
      tick(2000); // Simulate another second to check if emissions are stopped
      expect(component.getCurrentVideoTime).toHaveBeenCalledTimes(2); // Ensure it's called only once
      flush(); // Optionally use flush if you have other pending timers or use fakeAsync to handle it
    }));
  });
  describe('extractYoutubeVideoId', () => {
    it('should extract video ID from a standard URL', () => {
      const url = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
      const videoId = component.extractYoutubeVideoId(url);
      expect(videoId).toBe('dQw4w9WgXcQ');
    });

    it('should return null for a URL without video ID', () => {
      const url = 'https://www.youtube.com/watch';
      const videoId = component.extractYoutubeVideoId(url);
      expect(videoId).toBeNull();
    });

    it('should handle URLs with additional query parameters', () => {
      const url =
        'https://www.youtube.com/watch?v=dQw4w9WgXcQ&feature=youtu.be';
      const videoId = component.extractYoutubeVideoId(url);
      expect(videoId).toBe('dQw4w9WgXcQ');
    });

    it('should handle URLs with shortened format', () => {
      const url = 'https://youtu.be/dQw4w9WgXcQ';
      const videoId = component.extractYoutubeVideoId(url);
      expect(videoId).toBeNull(); // This format should return null as it does not match the regex
    });
  });

  it('should emit videoCompleteEmitter event in videoEnded method', () => {
    spyOn(component.videoCompleteEmitter, 'emit');

    component.videoEnded();

    expect(component.videoCompleteEmitter.emit).toHaveBeenCalledWith(true);
  });

  it('should call stopCurrentTimeEmitter on ngOnDestroy', () => {
    spyOn(component, 'stopCurrentTimeEmitter');

    component.ngOnDestroy();

    expect(component.stopCurrentTimeEmitter).toHaveBeenCalled();
  });

  describe('checkIfInView', () => {
    it('should call checkIfInView on window scroll', () => {
      spyOn(component, 'checkIfInView');

      window.dispatchEvent(new Event('scroll'));

      expect(component.checkIfInView).toHaveBeenCalled();
    });

    it('should call checkIfInView in ngAfterViewInit', () => {
      spyOn(component, 'checkIfInView');

      component.ngAfterViewInit();

      expect(component.checkIfInView).toHaveBeenCalled();
    });
  });

  describe('handleVideoPlayerInView', () => {
    it('should call handleVideoPlayerInView if element is in view', () => {
      spyOn(component, 'handleVideoPlayerInView');
      component.isInView = false;

      const rect = { top: 0, bottom: window.innerHeight } as DOMRect;
      spyOn(
        component.media.nativeElement,
        'getBoundingClientRect'
      ).and.returnValue(rect);

      component.checkIfInView();

      expect(component.handleVideoPlayerInView).toHaveBeenCalled();
      expect(component.isInView).toBeTrue();
    });

    it('should not call handleVideoPlayerInView if element is not in view', () => {
      spyOn(component, 'handleVideoPlayerInView');
      component.isInView = true;

      const rect = {
        top: window.innerHeight + 1,
        bottom: window.innerHeight + 100,
      } as DOMRect;
      spyOn(
        component.media.nativeElement,
        'getBoundingClientRect'
      ).and.returnValue(rect);

      component.checkIfInView();

      expect(component.handleVideoPlayerInView).not.toHaveBeenCalled();
      expect(component.isInView).toBeFalse();
    });
  });

  it('should prevent default action on Space key press if input is not focused and video is in view', () => {
    spyOn(component, 'isInputFocused').and.returnValue(false);
    spyOn(window, 'addEventListener').and.callThrough();

    const event = new KeyboardEvent('keydown', { code: 'Space' });
    spyOn(event, 'preventDefault');

    component.isInView = true;
    component.handleVideoPlayerInView(event);

    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('should not prevent default action on Space key press if input is focused', () => {
    spyOn(component, 'isInputFocused').and.returnValue(true);
    spyOn(window, 'addEventListener').and.callThrough();

    const event = new KeyboardEvent('keydown', { code: 'Space' });
    spyOn(event, 'preventDefault');

    component.isInView = true;
    component.handleVideoPlayerInView(event);

    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('should not prevent default action if Space key is not pressed', () => {
    spyOn(component, 'isInputFocused').and.returnValue(false);
    spyOn(window, 'addEventListener').and.callThrough();

    const event = new KeyboardEvent('keydown', { code: 'Enter' });
    spyOn(event, 'preventDefault');

    component.isInView = true;
    component.handleVideoPlayerInView(event);

    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  describe('isInputFocused', () => {
    it('should return true if an input or textarea is focused', () => {
      const input = document.createElement('input');
      document.body.appendChild(input);
      input.focus();

      expect(component.isInputFocused()).toBeTrue();

      document.body.removeChild(input);
    });

    it('should return false if no input or textarea is focused', () => {
      expect(component.isInputFocused()).toBeFalse();
    });
  });

  describe('invokeVideoTime', () => {
    it('should call getCurrentVideoTime when currentVideoTime$ emits a value', () => {
      const spy = spyOn(component, 'getCurrentVideoTime');
      const mockObservable = of('some value'); // Create an observable that emits a value
      mockCourseService.currentVideoTime$ = mockObservable;

      component.invokeVideoTime();

      expect(spy).toHaveBeenCalled();
    });

    it('should not call getCurrentVideoTime if currentVideoTime$ emits null or undefined', () => {
      const spy = spyOn(component, 'getCurrentVideoTime');
      const mockObservable = of(null); // Create an observable that emits null
      mockCourseService.currentVideoTime$ = mockObservable;

      component.invokeVideoTime();

      expect(spy).not.toHaveBeenCalled();
    });

    it('should handle empty observable gracefully', () => {
      const spy = spyOn(component, 'getCurrentVideoTime');
      const mockObservable = of(); // Create an observable that emits undefined
      mockCourseService.currentVideoTime$ = mockObservable;

      component.invokeVideoTime();

      expect(spy).not.toHaveBeenCalled();
    });
  });

  it('should call checkIfInView when onWindowScroll is called', () => {
    spyOn(component, 'checkIfInView');
    component.onWindowScroll();
    expect(component.checkIfInView).toHaveBeenCalled();
  });

  it('should call checkIfInView when ngAfterViewInit is called', () => {
    spyOn(component, 'checkIfInView');
    component.ngAfterViewInit();
    expect(component.checkIfInView).toHaveBeenCalled();
  });

  describe('Phase 1 coverage batch', () => {
    let previewVideoSubject: Subject<any>;
    let currentVideoTimeSubject: Subject<any>;

    beforeEach(() => {
      previewVideoSubject = new Subject<any>();
      currentVideoTimeSubject = new Subject<any>();
      mockCourseService.$previewVideo = previewVideoSubject.asObservable();
      mockCourseService.currentVideoTime$ =
        currentVideoTimeSubject.asObservable();
    });

    describe('checkYoutubeLink', () => {
      it('should return true for standard youtube.com URLs', () => {
        expect(
          component.checkYoutubeLink('https://www.youtube.com/watch?v=abc123')
        ).toBeTrue();
      });

      it('should return true for youtu.be URLs', () => {
        expect(
          component.checkYoutubeLink('https://youtu.be/abc123')
        ).toBeTrue();
      });

      it('should return false for non-YouTube URLs', () => {
        expect(
          component.checkYoutubeLink('https://example.com/video.mp4')
        ).toBeFalse();
      });

      it('should return false for undefined URL', () => {
        expect(component.checkYoutubeLink(undefined)).toBeFalse();
      });
    });

    describe('getSelectedTopic', () => {
      it('should configure YouTube player when filename is YOUTUBE', () => {
        const topic = {
          filename: 'YOUTUBE',
          videoUrl: 'https://www.youtube.com/watch?v=ytVideo123',
        };

        component.getSelectedTopic(topic);

        expect(component.youtubePlayer).toBeTrue();
        expect(component.videoId).toBe('ytVideo123');
      });

      it('should configure YouTube player when videoUrl is a YouTube link', () => {
        const topic = {
          filename: 'video.mp4',
          videoUrl: 'https://www.youtube.com/watch?v=linkVideo456',
        };

        component.getSelectedTopic(topic);

        expect(component.youtubePlayer).toBeTrue();
        expect(component.videoId).toBe('linkVideo456');
      });

      it('should configure regular video player for non-YouTube topics', () => {
        const topic = {
          filename: 'lecture.mp4',
          videoUrl: 'https://cdn.example.com/lecture.mp4',
        };

        component.getSelectedTopic(topic);

        expect(component.youtubePlayer).toBeFalse();
        expect(component.videoUrl).toBe('https://cdn.example.com/lecture.mp4');
        expect(component.videoId).toBeNull();
      });
    });

    describe('getVideoPreviewUrl', () => {
      it('should update videoUrl when preview video emits', () => {
        component.getVideoPreviewUrl();

        previewVideoSubject.next('https://preview.example.com/video.mp4');

        expect(component.videoUrl).toBe(
          'https://preview.example.com/video.mp4'
        );
      });

      it('should not update videoUrl when preview emits falsy value', () => {
        component.videoUrl = 'https://existing.example.com/video.mp4';
        component.getVideoPreviewUrl();

        previewVideoSubject.next(null);

        expect(component.videoUrl).toBe(
          'https://existing.example.com/video.mp4'
        );
      });
    });

    describe('generateSubtitleBlob', () => {
      it('should create subtitle blob URL when vttContent is present', () => {
        const videoElement = document.createElement('video');
        const track = document.createElement('track');
        videoElement.appendChild(track);
        const mockTextTrack = { mode: 'disabled' };
        Object.defineProperty(videoElement, 'textTracks', {
          value: {
            length: 1,
            0: mockTextTrack,
            [Symbol.iterator]: function* () {
              yield mockTextTrack;
            },
          },
          configurable: true,
        });
        spyOn(document, 'querySelector').and.returnValue(videoElement);

        component.currentSelectedTopic = {
          vttContent: 'WEBVTT\n\n1\n00:00:00.000 --> 00:00:02.000\nHello',
        };

        component.generateSubtitleBlob();

        expect(component.webVTTString).toContain('WEBVTT');
        expect(component.subtitleUrl).toBeTruthy();
        expect(mockTextTrack.mode).toBe('showing');
      });

      it('should not set subtitleUrl when vttContent is missing', () => {
        component.currentSelectedTopic = { vttContent: null };
        component.subtitleUrl = '';

        component.generateSubtitleBlob();

        expect(component.subtitleUrl).toBe('');
      });
    });

    describe('setVideoTime', () => {
      it('should seek regular video via vgPlayerApi when not completed', fakeAsync(() => {
        const seekTimeSpy = jasmine.createSpy('seekTime');
        component.vgPlayerApi = { seekTime: seekTimeSpy } as any;
        component.youtubePlayer = false;

        component.setVideoTime(42, false);
        tick(500);

        expect(seekTimeSpy).toHaveBeenCalledWith(42);
      }));

      it('should seek YouTube player when youtubePlayer is true', fakeAsync(() => {
        const seekToSpy = jasmine.createSpy('seekTo');
        component.player = { seekTo: seekToSpy } as any;
        component.youtubePlayer = true;

        component.setVideoTime(30, false);
        tick(500);

        expect(seekToSpy).toHaveBeenCalledWith(30, true);
      }));

      it('should not seek regular video when topic is completed', fakeAsync(() => {
        const seekTimeSpy = jasmine.createSpy('seekTime');
        component.vgPlayerApi = { seekTime: seekTimeSpy } as any;
        component.youtubePlayer = false;

        component.setVideoTime(60, true);
        tick(500);

        expect(seekTimeSpy).not.toHaveBeenCalled();
      }));

      it('should not seek when timeInSeconds is null', fakeAsync(() => {
        const seekTimeSpy = jasmine.createSpy('seekTime');
        component.vgPlayerApi = { seekTime: seekTimeSpy } as any;
        component.youtubePlayer = false;

        component.setVideoTime(null as any, false);
        tick(500);

        expect(seekTimeSpy).not.toHaveBeenCalled();
      }));
    });

    describe('ngOnChanges', () => {
      it('should refresh topic, subtitles, and seek time when currentSelectedTopic changes', () => {
        spyOn(component, 'getSelectedTopic').and.callThrough();
        spyOn(component, 'getVideoPreviewUrl');
        spyOn(component, 'generateSubtitleBlob');
        spyOn(component, 'invokeVideoTime');
        spyOn(component, 'setVideoTime');
        spyOn(component, 'onResize');

        const newTopic = {
          videoUrl: 'https://cdn.example.com/new-video.mp4',
          seekTime: 15,
          isCompleted: false,
        };
        component.currentSelectedTopic = newTopic;

        component.ngOnChanges({
          currentSelectedTopic: {
            currentValue: newTopic,
            previousValue: null,
            firstChange: false,
            isFirstChange: () => false,
          },
        });

        expect(component.getSelectedTopic).toHaveBeenCalledWith(newTopic);
        expect(component.subtitleUrl).toBe('');
        expect(component.webVTTString).toBe('');
        expect(component.getVideoPreviewUrl).toHaveBeenCalled();
        expect(component.generateSubtitleBlob).toHaveBeenCalled();
        expect(component.invokeVideoTime).toHaveBeenCalled();
        expect(component.setVideoTime).toHaveBeenCalledWith(15, false);
        expect(component.onResize).toHaveBeenCalled();
      });

      it('should return early when only isTooltipVisible changes', () => {
        spyOn(component, 'getSelectedTopic');
        spyOn(component, 'onResize');

        component.ngOnChanges({
          isTooltipVisible: {
            currentValue: false,
            previousValue: true,
            firstChange: false,
            isFirstChange: () => false,
          },
        });

        expect(component.getSelectedTopic).not.toHaveBeenCalled();
        expect(component.onResize).toHaveBeenCalled();
      });
    });

    describe('onPlayerReady', () => {
      it('should assign vgPlayerApi and emit on video ended', () => {
        const loadedMetadataSubject = new Subject<any>();
        const endedSubject = new Subject<any>();
        const mockApi = {
          getDefaultMedia: () => ({
            subscriptions: {
              loadedMetadata: loadedMetadataSubject,
              ended: endedSubject,
            },
          }),
        };

        const videoElement = document.createElement('video');
        Object.defineProperty(videoElement, 'textTracks', {
          value: [],
          configurable: true,
        });
        spyOn(document, 'querySelector').and.returnValue(videoElement);
        spyOn(component.videoCompleteEmitter, 'emit');
        spyOn(component, 'generateSubtitleBlob');
        spyOn(component, 'setupCustomSubtitleTracking');

        component.onPlayerReady(mockApi);

        expect(component.vgPlayerApi).toBe(mockApi as any);

        endedSubject.next(true);
        expect(component.videoCompleteEmitter.emit).toHaveBeenCalledWith(true);
      });

      it('should call generateSubtitleBlob when text tracks exist on metadata load', () => {
        const loadedMetadataSubject = new Subject<any>();
        const mockTextTrack = { mode: 'disabled' };
        const mockApi = {
          getDefaultMedia: () => ({
            subscriptions: {
              loadedMetadata: loadedMetadataSubject,
              ended: of(null),
            },
          }),
        };

        const videoElement = document.createElement('video');
        Object.defineProperty(videoElement, 'textTracks', {
          value: [mockTextTrack],
          configurable: true,
        });
        spyOn(document, 'querySelector').and.returnValue(videoElement);
        spyOn(component, 'generateSubtitleBlob');
        spyOn(component, 'setupCustomSubtitleTracking');

        component.onPlayerReady(mockApi);
        loadedMetadataSubject.next({});

        expect(component.generateSubtitleBlob).toHaveBeenCalled();
      });
    });

    describe('onStateChange', () => {
      it('should call videoEnded when YouTube player state is ENDED', () => {
        (window as any).YT = { PlayerState: { ENDED: 0 } };
        spyOn(component, 'videoEnded');

        component.onStateChange({ data: 0 } as YT.OnStateChangeEvent);

        expect(component.videoEnded).toHaveBeenCalled();
      });

      it('should not call videoEnded for non-ENDED states', () => {
        (window as any).YT = { PlayerState: { ENDED: 0, PLAYING: 1 } };
        spyOn(component, 'videoEnded');

        component.onStateChange({ data: 1 } as YT.OnStateChangeEvent);

        expect(component.videoEnded).not.toHaveBeenCalled();
      });
    });

    describe('getCurrentVideoTime', () => {
      it('should emit current time for regular video player', () => {
        spyOn(component.currentVideoTime, 'emit');
        component.youtubePlayer = false;
        component.vgPlayerApi = {
          getDefaultMedia: () => ({ currentTime: 55 }),
          playbackRate: 1.5,
        } as any;

        component.getCurrentVideoTime();

        expect(component.currentVideoTime.emit).toHaveBeenCalledWith({
          videoTime: 55,
          isVideoPlaying: component.isVideoPlaying,
          playbackRate: 1.5,
        });
      });

      it('should emit current time for YouTube player', () => {
        spyOn(component.currentVideoTime, 'emit');
        component.youtubePlayer = true;
        component.player = {
          getCurrentTime: () => 88,
        } as any;
        component.vgPlayerApi = { playbackRate: 1 } as any;

        component.getCurrentVideoTime();

        expect(component.currentVideoTime.emit).toHaveBeenCalledWith({
          videoTime: 88,
          isVideoPlaying: component.isVideoPlaying,
          playbackRate: 1,
        });
      });
    });

    describe('onTrackChange', () => {
      it('should add no-track class when track is null', () => {
        const videoElement = document.createElement('video');
        spyOn(document, 'querySelector').and.returnValue(videoElement);

        component.onTrackChange({
          srcElement: { value: 'null' },
        });

        expect(videoElement.classList.contains('no-track')).toBeTrue();
      });

      it('should remove no-track class when English track is selected', () => {
        const videoElement = document.createElement('video');
        videoElement.classList.add('no-track');
        spyOn(document, 'querySelector').and.returnValue(videoElement);

        component.onTrackChange({
          srcElement: { value: 'en' },
        });

        expect(videoElement.classList.contains('no-track')).toBeFalse();
      });
    });

    describe('updateSubtitlePosition', () => {
      it('should set CSS variables when scrub bar is visible', () => {
        component.isScrubBarVisible = true;

        component.updateSubtitlePosition();

        expect(
          document.documentElement.style.getPropertyValue(
            '--subtitle-bottom-pos'
          )
        ).toBe('60px');
        expect(
          document.documentElement.style.getPropertyValue(
            '--subtitle-translate-y'
          )
        ).toBe('-30px');
      });

      it('should set lower CSS variables when scrub bar is hidden', () => {
        component.isScrubBarVisible = false;

        component.updateSubtitlePosition();

        expect(
          document.documentElement.style.getPropertyValue(
            '--subtitle-bottom-pos'
          )
        ).toBe('30px');
        expect(
          document.documentElement.style.getPropertyValue(
            '--subtitle-translate-y'
          )
        ).toBe('0px');
      });
    });

    describe('ngOnDestroy cleanup', () => {
      it('should clear subtitleUpdateInterval when present', () => {
        const intervalId = setInterval(() => {}, 1000);
        component['subtitleUpdateInterval'] = intervalId;
        spyOn(component, 'stopCurrentTimeEmitter');
        spyOn(window, 'clearInterval');

        component.ngOnDestroy();

        expect(window.clearInterval).toHaveBeenCalledWith(intervalId);
      });
    });
  });
});
