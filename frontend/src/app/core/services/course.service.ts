import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  BehaviorSubject,
  Observable,
  Subject,
  catchError,
  of,
  switchMap,
} from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { AuthService } from './auth.service';
import { FormGroup } from '@angular/forms';
import { HttpConstants } from '../constants/http.constants';
import { InstructorService } from './instructor.service';
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
import { CourseType } from '../enums/course-status';

@Injectable({
  providedIn: 'root',
})
export class CourseService {
  private _httpBackend: HttpClient;

  _httpConstants: HttpConstants = new HttpConstants();
  tags?: Array<any> = [];
  topicTypes?: Array<any> = [];
  filteredCourses = [];
  course?: CreateCourse = {};
  typeVideo?: string = 'Video';
  typeArticle?: string = 'Article';
  typeQuiz?: string = 'Quiz';

  constructor(
    private _http: HttpClient,
    private _authService: AuthService,
    handler: HttpBackend,
    private _instructorService: InstructorService
  ) {
    this._httpBackend = new HttpClient(handler);
  }

  private favoriteCourses = new Subject<any>();
  $favoriteCourses = this.favoriteCourses.asObservable();

  passFavoriteCoursesFromLandingPageToNavbar(courseList: any) {
    this.favoriteCourses.next(courseList);
  }

  private searchResultsSubject = new BehaviorSubject<any[]>([]);
  public searchResults$: Observable<any[]> =
    this.searchResultsSubject.asObservable();

  setSearchResults(courses: any[], nlpCourses: any[], fromShowMore?): void {
    if (fromShowMore) {
      courses?.map((element) => {
        this.filteredCourses.push(element);
      });
      nlpCourses?.map((element) => {
        this.filteredCourses.push(element);
      });
      this.searchResultsSubject.next(this.filteredCourses);
    } else {
      this.filteredCourses = [...(courses || []), ...(nlpCourses || [])];
      this.searchResultsSubject.next([
        { courses: courses },
        { nlpCourses: nlpCourses },
      ]);
    }
  }
  

  private searchSuggestionsIds = new BehaviorSubject<any>([]);
  public $searchSuggestionsIds: Observable<any> =
    this.searchSuggestionsIds.asObservable();

  setSearchKeyword(courseIds: any): void {
    this.searchSuggestionsIds.next(courseIds);
  }

  private selectedTopic = new Subject<any>();
  $selectedTopic = this.selectedTopic.asObservable();

  passTopicToVideoPlayer(topic: any) {
    this.selectedTopic.next(topic);
  }

  private previewVideo = new Subject<any>();
  $previewVideo = this.previewVideo.asObservable();

  passPreviewVideoToVideoPlayer(topic: any) {
    this.previewVideo.next(topic);
  }

  private currentVideoTime = new Subject<any>();
  public currentVideoTime$: Observable<any> =
    this.currentVideoTime.asObservable();

  getTime(): void {
    this.currentVideoTime.next(true);
  }

  public getCourseCategory(): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.get(`${environment.baseUrl}course-category/`);
    } else {
      return this._httpBackend.get(`${environment.baseUrl}course-category/`);
    }
  }

  public getCoursesByCategory(body: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.post(
        `${environment.baseUrl}course/course-by-category`,
        body
      );
    } else {
      return this._httpBackend.post(
        `${environment.baseUrl}course/course-by-category`,
        body
      );
    }
  }
  public getNewCourses(body: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.get(
        `${environment.baseUrl}home-page/new-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    } else {
      return this._http.get(
        `${environment.baseUrl}home-page/new-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    }
  }
  public getInstructors(body: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}home-page/top-instructor?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
    );
  }
  public getTrendingCourses(body: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.get(
        `${environment.baseUrl}home-page/trending-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    } else {
      return this._http.get(
        `${environment.baseUrl}home-page/trending-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    }
  }
  public getAllCourses(body: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.post(
        `${environment.baseUrl}home-page/view-all`, body
      );
    } else {
      return this._http.post(
        `${environment.baseUrl}home-page/view-all`, body
      );
    }
  }
  public getFreeCourses(body: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.get(
        `${environment.baseUrl}home-page/free-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    } else {
      return this._httpBackend.get(
        `${environment.baseUrl}home-page/free-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    }
  }
  public getPremiumCourses(body: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.get(
        `${environment.baseUrl}home-page/premium-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    } else {
      return this._httpBackend.get(
        `${environment.baseUrl}home-page/premium-courses?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    }
  }

  public getCourseDetails(courseId: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.get(`${environment.baseUrl}course/get/${courseId}`);
    } else {
      return this._httpBackend.get(
        `${environment.baseUrl}course/get/${courseId}`
      );
    }
  }

  public getRelatedCourses(body: any): Observable<any> {
    if (this._authService.isLoggedIn()) {
      return this._http.post(
        `${environment.baseUrl}course/get-related-courses`,
        body
      );
    } else {
      return this._httpBackend.post(
        `${environment.baseUrl}course/get-related-courses`,
        body
      );
    }
  }

  public getFavoriteCourses(body: any): Observable<any> {
    if (body?.title) {
      return this._http.get(
        `${environment.baseUrl}favourite-course/?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}&title=${body?.title}`
      );
    } else {
      return this._http.get(
        `${environment.baseUrl}favourite-course/?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    }
  }

  public getMyCourses(body: any): Observable<any> {
    if (body?.title) {
      return this._http.get(
        `${environment.baseUrl}enrollment/?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}&title=${body?.title}&sortBy=${body?.sortBy}`
      );
    } else {
      return this._http.get(
        `${environment.baseUrl}enrollment/?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}&sortBy=${body?.sortBy}`
      );
    }
  }

  public enrolledInCourse(courseId: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}enrollment/?courseId=${courseId}`,
      null
    );
  }

  public addOrRemoveCourseToFavorite(courseId: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}favourite-course/?courseId=${courseId}`,
      null
    );
  }

  youtubeVideoUrlUpload(videoId: string): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}youtube-video/duration?videoId=${videoId}`
    );
  }

  public getCourseSections(courseId: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}section/${courseId}`);
  }

  public getSectionTopics(courseId: any, sectionId: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}topic/course/${courseId}/section/${sectionId}`
    );
  }

  public rateAndReviewCourse(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}course-review/`, body);
  }

  public getCourseRatingAndReview(courseId: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}course-review/${courseId}`);
  }

  public rateAndReviewSection(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}section-review/`, body);
  }
  public likeAndDislikeReviewSection(body: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}course-review/like/${body?.reviewId}/${body.action}`,
      {}
    );
  }

  public getSectionRatingAndReview(sectionId: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}section-review/${sectionId}`);
  }

  public getTopicSummary(topicId: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}topic/summary/${topicId}`);
  }

  public getQuestions(body): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}question/?courseId=${body?.courseId}&pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
    );
  }

  public getQuestionsReplies(body): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}answer/?courseId=${body?.courseId}&questionId=${body?.questionId}&pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
    );
  }

  public createQuestion(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}question/`, body);
  }

  public replyQuestion(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}answer/`, body);
  }

  public getCourseRatingReviewAndFeedback(body: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}course-review/?courseId=${body?.courseId}&pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
    );
  }

  public getSectionAndTopicsChatQuestion(courseId: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}chat/?courseId=${courseId}`);
  }

  public getCourseChatHistory(chatId: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}chat-history/${chatId}`);
  }

  public sendMessageInChat(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}chat/`, body);
  }

  public createTopicNote(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}topic-notes/`, body);
  }

  public getComments(courseId: number, currentPage: number, pageSize: number): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}course/get/feedback/${courseId}?pageNo=${currentPage}&pageSize=${pageSize}`
    );
  }
  
  public sendCoPilotMessage(body: any):Observable<any> {
    return this._http.post(`${environment.basePath}copilot/chat/`,body);
  }

  public getTopicNotes(body: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}topic-notes/?courseId=${body?.courseId}&pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
    );
  }

  public deleteTopicNote(body: any): Observable<any> {
    return this._http.delete(
      `${environment.baseUrl}topic-notes/?courseId=${body?.courseId}&topicId=${body?.topicId}&topicNoteId=${body?.topicNoteId}`
    );
  }

  public validateQuizAnswer(body: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}quiz/validate-answer?questionId=${body?.questionId}&answerId=${body?.answerId}`,
      null
    );
  }

  public submitQuizAnswers(answers: Array<{ questionId: number; answerId: number }>) {
    return this._http.post(`${environment.baseUrl}quiz/validate-answering`, answers);
  }
  

  public markTopicComplete(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}user-course-progress/`, body);
  }

  public manageWatchTime(courseId?: any, watchTime?: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}watch-time/?courseId=${courseId}&watchTime=${watchTime}`, null);
  }

  public searchCourse(body: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}course/course-search?query=${body?.searchValue}&pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
    );
  }

  public getAlternateInstructorSections(
    courseId: any,
    sectionId?: any
  ): Observable<any> {
    let url = `${environment.baseUrl}alternate-section/`;

    const queryParams = [];
    queryParams.push(`courseId=${encodeURIComponent(courseId)}`);
    queryParams.push(`sectionId=${encodeURIComponent(sectionId)}`);
    queryParams.push(`pageNo=${encodeURIComponent('0')}`);
    queryParams.push(`pageSize=${encodeURIComponent('100')}`);

    if (queryParams.length > 0) {
      url += `?${queryParams.join('&')}`;
    }

    return this._http.get(url);
  }

  public pinAlternateInstructor(
    toCourseId,
    toSectionId,
    fromCourseId,
    fromSectionId
  ): Observable<any> {
    let url = `${environment.baseUrl}alternate-section/`;

    const queryParams = [];
    queryParams.push(`courseId=${encodeURIComponent(toCourseId)}`);
    queryParams.push(`sectionId=${encodeURIComponent(toSectionId)}`);
    queryParams.push(`fromCourseId=${encodeURIComponent(fromCourseId)}`);
    queryParams.push(`fromSectionId=${encodeURIComponent(fromSectionId)}`);

    if (queryParams.length > 0) {
      url += `?${queryParams.join('&')}`;
    }

    return this._http.post(`${url}`, null);
  }

  public unPinAlternateInstructor(courseId, sectionId): Observable<any> {
    let url = `${environment.baseUrl}alternate-section/`;

    const queryParams = [];
    queryParams.push(`courseId=${encodeURIComponent(courseId)}`);
    queryParams.push(`sectionId=${encodeURIComponent(sectionId)}`);

    if (queryParams.length > 0) {
      url += `?${queryParams.join('&')}`;
    }

    return this._http.delete(`${url}`);
  }

  public getSuggestions(input: string): Observable<any> {
    return this._http.post(`${environment.baseUrl}course/autocomplete`, {
      input: input.trim(),
    });
  }

  public courseProgress(courseId: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}user-course-progress/${courseId}`
    );
  }

  public getInstructorPublicProfile(profileUrl: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}user-profile/?profileUrl=${profileUrl}`
    );
  }

  public getInstructorProfile(): Observable<any> {
    return this._http.get(`${environment.baseUrl}user-profile/`);
  }

  public getInstructorCourses(body: any): Observable<any> {
    if (body?.instructorId) {
      return this._http.get(
        `${environment.baseUrl}course/course-by-teacher-for-profile?instructorId=${body?.instructorId}&pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    } else {
      return this._http.get(
        `${environment.baseUrl}course/course-by-teacher-for-profile?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`
      );
    }
  }

  public getCourseLevels(): Observable<any> {
    return this._http.get(`${environment.baseUrl}course-level/`);
  }

  public getCourseFirstStepDetail(courseId?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}course/course-detail-for-update-first-step/${courseId}`
    );
  }

  public getSectionByCourseId(courseId?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}section/section-for-update/${courseId}`
    );
  }

  public getTopicsBySectionId(sectionId?: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}topic/section/${sectionId}`);
  }

  public getTagsByCourseId(courseId?: any): Observable<any> {
    return this._http.get(`${environment.baseUrl}tag/${courseId}`);
  }

  public createCourse(body: any): Observable<any> {
    return this._http.post(`${environment.baseUrl}course/create`, body);
  }

  public createCourseDto(
    courseInformationData?: FormGroup,
    sectionsData?: any,
    courseId?: any,
    isActive?: any,
    certificateEnabled?: any,
    selectedContentType?: any
  ): Observable<any> {
    if (courseId) {
      return this.getCourseTags(courseId)?.pipe(
        switchMap(() =>
          this.getTopicTypes(
            courseInformationData,
            sectionsData,
            courseId,
            isActive,
            certificateEnabled,
            selectedContentType
          )
        ),
        catchError((error) => {
          console.log(error);
          return of(null); // Handle the error appropriately
        })
      );
    } else {
      return this.getTopicTypes(
        courseInformationData,
        sectionsData,
        courseId,
        isActive,
        certificateEnabled,
        selectedContentType
      );
    }
  }

  courseTitleExist(title: any, courseId?: any) {
    return this._http.post(`${environment.baseUrl}course/unique-course-title`, {
      courseId: courseId,
      courseTitle: title,
    });
  }

  courseUrlExist(url?: any, courseId?: any) {
    return this._http.get(
      `${environment.baseUrl}course/unique-course-url?url=${url}&courseId=${courseId}`
    );
  }

  getCourseByUrl(url: any) {
    return this._http.post(`${environment.baseUrl}course/course-url`, {
      courseUrl: url,
    });
  }

  calculateCourseProgress() {
    let totalCourseProgress = 0;
    totalCourseProgress += this.calculateCourseInformationProgress();
    totalCourseProgress += this.calculateSectionProgress();
    return totalCourseProgress.toFixed(2);
  }

  calculateCourseInformationProgress() {
    let nullValues = [];
    let progressPerField = 3.3;
    let numberOfFields = 10;
    let totalProgress = 0;
    let tags = this.course?.tags?.filter((tag: any) => tag.active == true);
    if (this.course?.title == '') {
      nullValues.push(false);
    }
    if (this.course?.description == '') {
      nullValues.push(false);
    }
    if (this.course?.categoryId == null) {
      nullValues.push(false);
    }
    if (this.course?.courseLevelId == null) {
      nullValues.push(false);
    }
    if (this.course?.about == '') {
      nullValues.push(false);
    }
    if (this.course?.thumbnailUrl == '') {
      nullValues.push(false);
    }
    if (this.course?.previewVideoURL == '') {
      nullValues.push(false);
    }
    if (tags?.length == 0) {
      nullValues.push(false);
    }
    if (this.course?.prerequisite[0] == '') {
      nullValues.push(false);
    }
    if (this.course?.courseOutcomes?.length == 0) {
      nullValues.push(false);
    }

    totalProgress =
      progressPerField * numberOfFields - progressPerField * nullValues.length;
    return totalProgress;
  }

  calculateSectionProgress() {
    let totalProgress = 0;
    if (this.course?.sections?.length > 0) {
      const validSection = this.course.sections.find((section: any) => {
        return (
          !section.delete &&
          section.topics?.some((topic: any) => topic.validate)
        );
      });

      if (validSection) {
        if (validSection.title != '') {
          totalProgress += 4;
        }
        if (validSection.topics.length > 0) {
          totalProgress += 30;
        }
      }
    }

    return totalProgress;
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

  fetchTopicTypeId(selectedContentType?: any) {
    const topicType = this.topicTypes.find(
      (topicType) => topicType.name === selectedContentType
    );
    return topicType?.id;
  }

  createTopics(section: any) {
    const topics: Topic[] = [];

    section.topics?.forEach((topic: any, index: any) => {
      if (topic.topicId || topic.validate) {
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
          validate: topic.validate,
          video: video,
          quiz: quiz,
          article: article,
        };
        topics.push(top);
      }
    });

    return topics;
  }

  getArticleData(article?: any) {
    const art: Article = {
      id: article.articleId,
      delete: article.delete,
      article: article.content,
      documents:
        article.articleDocumnetUrl != ''
          ? this.getArticleDocument(article)
          : null,
    };
    return art;
  }

  getArticleDocument(article?: any) {
    const documentArr: Document[] = [];
    const document: Document = {
      id: article.articleDocumnetId,
      delete: article.delete,
      docName: article.articleDocumnetUrl != '' ? article.articleFileName : '',
      docUrl: article.articleDocumnetUrl,
      summary: article.articleSummary,
    };
    documentArr.push(document);
    return documentArr;
  }

  getQuizData(quiz?: any) {
    const qui: Quiz = {
      id: quiz?.quizId,
      delete: quiz?.delete,
      title: quiz?.title,
      durationInMinutes: quiz?.durationInMinutes,
      passingCriteria: quiz?.passingCriteria,
      randomQuestion: quiz?.randomQuestion,
      questions: this.getQuizQuestions(quiz.questions),
    };
    return qui;
  }

  getQuizQuestions(questions?: any) {
    const quizQuestions: Question[] = [];
    questions?.forEach((question: any) => {
      if (this.validateQuestion(question)) {
        const ques: Question = {
          id: question.questionId,
          delete: question.delete,
          questionText: question.ques,
          questionType: question?.questionType?.key,
          explanation: question?.explanation,
          answers: this.getQuestionAnswers(
            question.answers,
            question.correctAnswer
          ),
        };
        quizQuestions.push(ques);
      }
    });
    return quizQuestions;
  }

  validateQuestion(question?: any): boolean {
    let valid = true;
    if (question.questionId) {
      valid = true;
    } else if (
      !question.ques ||
      question?.answers.filter((answer: any) => answer.isCorrectAnswer)
            ?.length == 0 ||
      question.answers.length == 1
    ) {
      valid = false;
    }
    return valid;
  }

  getQuestionAnswers(answers?: any, correctAnswer?: any) {
    const questionAnswers: Answer[] = [];
    answers?.forEach((answer: any) => {
      if(!answer?.answerId && answer?.delete){
        return;
      }
        const ans: Answer = {
          id: answer.answerId,
          delete: answer.delete,
          answerText: answer.ans,
          // isCorrectAnswer: answer.ans == correctAnswer.ans ? true : false,
          isCorrectAnswer: answer?.isCorrectAnswer
        };
        questionAnswers.push(ans);
    });
    return questionAnswers;
  }

  createSections(sectionsData?: any, courseType?) {
    const sections: Section[] = [];

    sectionsData?.forEach((section: any, index: any) => {
      if (section.sectionId != '' || section.delete == false) {
        const sec: Section = {
          id: section.sectionId,
          delete: section.delete,
          title: section.name,
          isFree:
            courseType === CourseType.PREMIUM ? false : section.switchValue,
          level: index + 1,
          topics: this.createTopics(section),
        };
        sections.push(sec);
      }
    });

    return sections;
  }

  getCourseTags(courseId?: any): Observable<any> {
    return this.getTagsByCourseId(courseId)?.pipe(
      switchMap((response: any) => {
        this.tags = response?.data;
        return of(response); // Return an observable with the response
      }),
      catchError((error) => {
        this.course.tags = [];
        this.tags = [];
        console.log(error);
        return of(null); // Handle the error appropriately
      })
    );
  }

  getTopicTypes(
    courseInformationData?: FormGroup,
    sectionsData?: any,
    courseId?: any,
    isActive?: any,
    certificateEnabled?: any,
    selectedContentType?: any
  ): Observable<any> {
    return this._instructorService?.getTopicTypes()?.pipe(
      switchMap((response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.topicTypes = response?.data;

          this.course.isActive = isActive;
          this.course.courseId = courseId;
          this.course.title = !courseInformationData?.get('titleExist')?.value
            ? courseInformationData?.get('courseTitle')?.value
            : null;
          this.course.courseUrl = !courseInformationData?.get('urlExist')?.value
            ? courseInformationData?.get('courseUrl')?.value
            : null;
          this.course.description =
            courseInformationData?.get('description').value;
          this.course.courseType =
            courseInformationData?.get('courseType').value;
          this.course.price =
            this.course?.courseType == CourseType.PREMIUM
              ? courseInformationData?.get('price').value
              : null;
          this.course.categoryId =
            courseInformationData?.get('courseCategory').value?.id;

          this.course.courseLevelId =
            courseInformationData?.get('courseLevel').value?.id;
          this.course.about =
            courseInformationData?.get('courseHeadline').value;
          this.course.thumbnailUrl =
            courseInformationData?.get('thumbnailPath').value;
          this.course.previewVideoURL =
            courseInformationData?.get('previewPath').value;
          this.course.previewVideoVttContent = courseInformationData?.get(
            'previewVideoVttContent'
          ).value;
          this.course.tags = courseId
            ? this.modifyCourseTags(
                courseInformationData?.get('tagsArray').value
              )
            : courseInformationData?.get('tagsArray').value;
          this.course.prerequisite = this.fetchPrerequisite(
            courseInformationData?.get('prerequisite').value
          );
          this.course.courseOutcomes = this.fetchCourseSummariesInfo(
            courseInformationData?.get('courseSummaries').value
          );
          this.course.contentType = selectedContentType.toUpperCase();
          this.course.sections = this.createSections(
            sectionsData,
            this.course?.courseType
          );
          // this.course.courseProgress = isActive
          //   ? 100
          //   : this.calculateCourseProgress();
          this.course.certificateEnabled =
            certificateEnabled == null ? false : certificateEnabled;
          const course = this.course;

          return this._http.post(`${environment.baseUrl}course/create`, course);
        } else {
          return of(null);
        }
      }),
      catchError((error: any) => {
        console.log(error);
        return of(null);
      })
    );
  }

  modifyCourseTags(tagsArray?: any) {
    this.tags?.forEach((obj1: any) => {
      if (!tagsArray.find((obj2: any) => obj1.id == obj2.id)) {
        obj1.active = false;
      }
    });

    tagsArray?.forEach((tag: any) => {
      if (tag.id == null) {
        this.tags.push(tag);
      }
    });

    return this.tags;
  }

  fetchPrerequisite(prerequisite?: any) {
    const prerequisites = [];
    prerequisites.push(prerequisite);
    return prerequisites;
  }

  fetchCourseSummariesInfo(courseSummaries?: any) {
    const courseSummariesInfo = [];
    courseSummaries?.forEach((courseSummary: any) => {
      if (courseSummary.courseSummaryInfo) {
        courseSummariesInfo.push(courseSummary.courseSummaryInfo);
      }
    });
    return courseSummariesInfo;
  }

  premiumCourseAvailable() {
    return this._http.get(
      `${environment.baseUrl}course/premium-course-available`
    );
  }

  public deleteChat(courseId?: any, chatId?: any): Observable<any> {
    return this._http.delete(
      `${environment.baseUrl}chat/delete?courseId=${courseId}&chatId=${chatId}`
    );
  }

}
