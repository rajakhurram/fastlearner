import { compileDeclareDirectiveFromMetadata } from '@angular/compiler';
import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import {
  Answer,
  Article,
  CreateCourse,
  Document,
  Question,
  Quiz,
  Section,
  Topic,
  Video,
} from 'src/app/core/models/create-course.model';
import { CourseService } from 'src/app/core/services/course.service';
import { InstructorService } from 'src/app/core/services/instructor.service';
import { MessageService } from 'src/app/core/services/message.service';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CommunicationService } from 'src/app/core/services/communication.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CompletionModalComponent } from 'src/app/modules/dynamic-modals/completion-modal/completion-modal.component';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';

@Component({
  selector: 'app-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.scss'],
})
export class PreviewComponent {
  _httpConstants: HttpConstants = new HttpConstants();
  @ViewChild('videoPlayerElement', { static: false }) videoPlayerElement: any;
  isTooltipVisible: boolean = true;
  @Output() currentStep = new EventEmitter<string>();
  @Input() courseInformationData: FormGroup;
  @Input() sectionsData: any;
  @Input() courseId: any;
  @Input() selectedContentType: any;
  description?: any;
  previewVideo?: any = {
    videoUrl: '',
    vttContent: null,
  };
  thumbnail?: any = '';
  typeVideo?: string = 'Video';
  typeArticle?: string = 'Article';
  typeQuiz?: string = 'Quiz';
  topicTypes?: Array<any> = [];

  course?: CreateCourse = {};
  section?: Section = {};

  tags?: Array<any> = [];
  imageSrc = '../../../../assets/images/add_image.svg';
  videoSrc = '../../../../../assets/images/add_video.svg';
  courseContentType = CourseContentType;

  constructor(
    private fb: FormBuilder,
    private _instructorService: InstructorService,
    private _courseService: CourseService,
    private _messageService: MessageService,
    private _router: Router,
    private _communicationService: CommunicationService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef
  ) {}

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.checkTooltipVisibility();
  }

  ngOnInit(): void {
    this._communicationService?.closeCompletionData$?.subscribe(() => {
      this.closeCourseCompletionModal();
    });

    if (this.courseId) {
      this.getCourseTags();
    }
    this.getTopicTypes();
    this.description = this.courseInformationData?.get('description')?.value;
    this.course.certificateEnabled =
      this.courseInformationData?.get('certificateEnabled')?.value;
    this.previewVideo.videoUrl =
      this.courseInformationData?.get('previewPath')?.value;
    this.previewVideo.vttContent = this.courseInformationData?.get(
      'previewVideoVttContent'
    )?.value;
  }

  checkTooltipVisibility() {
    const rect =
      this.videoPlayerElement?.media?.nativeElement.getBoundingClientRect();
    const tooltipHeight = rect?.height;

    // Calculate the visible height of the tooltip
    const visibleTop = Math.max(rect?.top, 0);
    const visibleBottom = Math.min(rect?.bottom, window.innerHeight);
    const visibleHeight = visibleBottom - visibleTop;

    // Check if at least half of the tooltip is visible
    this.isTooltipVisible = visibleHeight >= tooltipHeight / 2.3;
  }

  getTopicTypes() {
    this._instructorService?.getTopicTypes()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.topicTypes = response?.data;
        }
      },
      error: (error: any) => {
        console.log(error);
      },
    });
  }

  get courseTagArray(): FormArray {
    return this.courseInformationData?.get('tagsArray') as FormArray;
  }

  steps(step?: any) {
    this.currentStep.emit(step);
  }

  publishCourse() {
    this._courseService
      .createCourseDto(
        this.courseInformationData,
        this.sectionsData,
        this.courseId,
        true,
        this.course?.certificateEnabled,
        this.selectedContentType
      )
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this._communicationService.updateInstructorCourse();
            this.opencourseCompletionModal();
          }
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  getCourseTags() {
    this._courseService?.getTagsByCourseId(this.courseId)?.subscribe({
      next: (response: any) => {
        this.tags = response?.data;
      },
      error: (error: any) => {
        console.log(error);
      },
    });
  }

  modifyCourseTags(tagsArray?: any) {
    this.tags.forEach((obj1: any) => {
      if (!tagsArray.find((obj2: any) => obj1.id == obj2.id)) {
        obj1.active = false;
      }
    });

    tagsArray.forEach((tag: any) => {
      if (tag.id == null) {
        this.tags.push(tag);
      }
    });

    return this.tags;
  }

  createCourse() {
    this._courseService.createCourse(this.course).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.opencourseCompletionModal();
        }
      },
      error: (error: any) => {
        console.log(error);
      },
    });
  }

  routeToDashboard() {
    this._router.navigate(['instructor/instructor-dashboard']);
  }

  createSections() {
    const sections: Section[] = [];

    this.sectionsData.forEach((section: any, index: any) => {
      const sec: Section = {
        id: section.sectionId,
        delete: section.delete,
        title: section.name,
        isFree: section.switchValue,
        level: index + 1,
        topics: this.createTopics(section),
      };
      sections.push(sec);
    });

    return sections;
  }

  createTopics(section: any) {
    const topics: Topic[] = [];

    section.topics.forEach((topic: any, index: any) => {
      if (topic.id || topic.validate) {
        let video = null;
        let quiz = null;
        let article = null;

        if (topic.selectedContentType == this.typeVideo) {
          video = this.getVideoData(topic.video);
          topic.article.articleId == ''
            ? (article = null)
            : (article = this.getArticleData(topic.article));
          topic.quiz.quizId == ''
            ? (quiz = null)
            : (quiz = this.getQuizData(topic.quiz));
          if (
            topic.article &&
            topic.article.documents &&
            topic.article.documents.length > 0
          ) {
            topic.article.documents[0].delete = true;
          }
        } else if (topic.selectedContentType == this.typeQuiz) {
          quiz = this.getQuizData(topic.quiz);
          topic.article.articleId == ''
            ? (article = null)
            : (article = this.getArticleData(topic.article));
          topic.video.videoData.videoId == ''
            ? (video = null)
            : (video = this.getVideoData(topic.video));
          if (
            topic.article &&
            topic.article.documents &&
            topic.article.documents.length > 0
          ) {
            topic.article.documents[0].delete = true;
          }
        } else if (topic.selectedContentType == this.typeArticle) {
          article = this.getArticleData(topic.article);
          topic.video.videoData.videoId == ''
            ? (video = null)
            : (video = this.getVideoData(topic.video));
          topic.quiz.quizId == ''
            ? (quiz = null)
            : (quiz = this.getQuizData(topic.quiz));
        }

        const top: Topic = {
          id: topic.topicId,
          delete: topic.delete,
          title: topic.name,
          level: index + 1,
          topicTypeId: this.fetchTopicTypeId(topic.selectedContentType),
          duration: topic.topicDuration,
          video: video,
          quiz: quiz,
          article: article,
        };
        topics.push(top);
      }
    });

    return topics;
  }

  getVideoData(video?: any) {
    const vid: Video = {
      id: video.videoData.videoId,
      filename: video.videoData.videoFileName,
      delete: video.videoData.delete,
      videoURL: video.videoData.videoUrl,
      summary: video.videoData.videoSummary,
      transcribe: video.videoData.videoTranscript,
      vttContent: video.videoData.videoSubtitles,
      documents: this.getDocuments(video.documentData?.documents),
    };
    return vid;
  }

  getDocuments(documents?: any) {
    const documentArr: Document[] = [];
    documents?.forEach((document: any) => {
      const doc: Document = {
        id: document.id,
        delete: document.delete,
        summary: document.summary,
        docName: document.documentFileName,
        docUrl: document.documentUrl,
      };
      documentArr.push(doc);
    });

    return documentArr;
  }

  getQuizData(quiz?: any) {
    const qui: Quiz = {
      id: quiz.quizId,
      delete: quiz.delete,
      title: quiz.title,
      questions: this.getQuizQuestions(quiz.questions),
    };
    return qui;
  }

  getQuizQuestions(questions?: any) {
    const quizQuestions: Question[] = [];
    questions.forEach((question: any) => {
      const ques: Question = {
        id: question.questionId,
        delete: question.delete,
        questionText: question.ques,
        answers: this.getQuestionAnswers(
          question.answers,
          question.correctAnswer
        ),
      };
      quizQuestions.push(ques);
    });
    return quizQuestions;
  }

  getQuestionAnswers(answers?: any, correctAnswer?: any) {
    const questionAnswers: Answer[] = [];
    answers.forEach((answer: any) => {
      const ans: Answer = {
        id: answer.answerId,
        delete: answer.delete,
        answerText: answer.ans,
        isCorrectAnswer: answer.ans == correctAnswer.ans ? true : false,
      };
      questionAnswers.push(ans);
    });
    return questionAnswers;
  }

  getArticleData(article?: any) {
    const art: Article = {
      id: article.articleId,
      delete: article.delete,
      article: article.content,
      documents: this.getArticleDocument(article),
    };
    return art;
  }

  getArticleDocument(article?: any) {
    const documentArr: Document[] = [];
    const document: Document = {
      id: article.articleDocumnetId,
      delete: article.delete,
      docName: article.articleFileName,
      docUrl: article.articleDocumnetUrl,
      summary: article.articleSummary,
    };
    documentArr.push(document);
    return documentArr;
  }

  fetchTopicTypeId(selectedContentType?: any) {
    const topicType = this.topicTypes.find(
      (topicType) => topicType.name === selectedContentType
    );
    return topicType?.id;
  }

  fetchCourseSummariesInfo(courseSummaries?: any) {
    const courseSummariesInfo = [];
    courseSummaries.forEach((courseSummary: any) => {
      if (courseSummary.courseSummaryInfo) {
        courseSummariesInfo.push(courseSummary.courseSummaryInfo);
      }
    });
    return courseSummariesInfo;
  }

  fetchPrerequisite(prerequisite?: any) {
    const prerequisites = [];
    prerequisites.push(prerequisite);
    return prerequisites;
  }

  opencourseCompletionModal() {
    const modal = this._modal.create({
      nzContent: CompletionModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: '40%',
    });
    modal?.afterClose?.subscribe((event) => {
      this.closeCourseCompletionModal();
    });
  }

  closeCourseCompletionModal() {
    // this._modal.closeAll();
    this.routeToDashboard();
  }
}
