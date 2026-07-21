import { HttpClient } from '@angular/common/http'; // Make sure HttpClientModule is imported in your app.module.ts
import { CourseService } from 'src/app/core/services/course.service';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Component, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { finalize } from 'rxjs/operators';


@Component({
  selector: 'app-co-pilot',
  templateUrl: './co-pilot.component.html',
  styleUrls: ['./co-pilot.component.scss']
})
export class CoPilotComponent implements AfterViewChecked {
  message?: any = '';
  chatHistory: { sender: 'user' | 'bot', text: string, entities?: any[] }[] = [];
  showSpinner: boolean = false;
  _httpConstants: HttpConstants = new HttpConstants();
  @ViewChild('chatContainer') public chatContainer!: ElementRef;
shouldScroll: boolean = false;
  private sessionId: string | null = null;
  constructor(
    private http: HttpClient,
    private _router: Router,
    private _courseService: CourseService

  ) {}

    ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  sendMessage() {
    if (!this.message.trim()) return;
  if (this.showSpinner) {
    return; 
  }
    this.showSpinner = true;
    this.chatHistory.push({ sender: 'user', text: this.message });
    this.shouldScroll = true;

    const userMessage = this.message;
    this.message = '';
  
    const body = {
      question: userMessage,
      session_id: this.sessionId ?? undefined,
    };
  
    this._courseService.sendCoPilotMessage(body).pipe(
      finalize(() => {
        this.showSpinner = false;
        this.shouldScroll = true;
      })
    ).subscribe({
      next: (response: any) => {
        // Backward compatible parsing:
        // - New API: { answer: string, session_id: string }
        // - Old proxy: { answer: { answer: string, entities: [] } }
        const answerText =
          response?.answer?.answer ??
          (typeof response?.answer === 'string' ? response.answer : null) ??
          'No answer found.';

        const rawEntities = response?.answer?.entities || [];
        this.sessionId = response?.session_id ?? this.sessionId;
const groupedEntities = this.processEntities(rawEntities);

this.chatHistory.push({ sender: 'bot', text: answerText, entities: groupedEntities });
      },
      error: (err) => {
        this.chatHistory.push({
          sender: 'bot',
          text: 'Sorry, something went wrong. Please check your connection and try again.',
        });
        console.error(err);
      },
    });
  }
  
  
  routeToInstructorProfile(profileUrl?: any) {
    const queryParam = profileUrl ? `?url=${encodeURIComponent(profileUrl)}` : '';
    const fullUrl = this._router.serializeUrl(
      this._router.createUrlTree(['user/profile'], { queryParams: { url: profileUrl } })
    );
    const baseUrl = window.location.origin;
    window.open(baseUrl + '/' + fullUrl, '_blank');
  }
  
  routeToCourseDetails(courseUrl: any) {
    const fullUrl = this._router.serializeUrl(
      this._router.createUrlTree(['student/course-details', courseUrl])
    );
    const baseUrl = window.location.origin;
    window.open(baseUrl + '/' + fullUrl, '_blank');
  }
  
   private scrollToBottom(): void {
  try {
    if (this.chatContainer) {
      this.chatContainer.nativeElement.scrollTop =
        this.chatContainer.nativeElement.scrollHeight;
    }
  } catch (err) {
    console.error('Scroll failed', err);
  }
}


  routeToCourseContent(event: any, course: any, sectionId: any, topicId?: any) {
  const urlTree = this._router.createUrlTree(
    ['student/course-content', course.url],
    { queryParams: { sectionId: sectionId, topicId: topicId } }
  );
  const fullUrl = this._router.serializeUrl(urlTree);
  const baseUrl = window.location.origin;

  window.open(baseUrl + '/' + fullUrl, '_blank');

  event.stopPropagation();
}


  
  private processEntities(entities: any[]) {
  const groupedCourses = [];
  const courses = entities.filter(e => e.type === 'course');

  for (const course of courses) {
    const courseId = course.course_id;

    const sections = entities.filter(e => e.type === 'section' && e.course_id === courseId);
    const topics = entities.filter(e => e.type === 'topic' && e.course_id === courseId);

    groupedCourses.push({
      ...course,
      sections: sections,
      topics: topics
    });
  }

  const instructors = entities
    .filter(e => e.type === 'instructor')
    .map(({ contact, ...instructor }) => instructor);

  return [...groupedCourses, ...instructors];
}

formatChatText(text: string): string {
  if (!text) return '';

  
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');

  
  const formatted = escaped
    .replace(/\n/g, '<br>')               
    .replace(/(\d+\.\s+)/g, '<br>$1')    
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>'); 

  return formatted;
}

isValidValue(value: any): boolean {
  if (!value) return false;

  const invalidValues = ['not specified', 'not available', 'n/a'];
  return !invalidValues.includes(value.toString().trim().toLowerCase());
}


}
