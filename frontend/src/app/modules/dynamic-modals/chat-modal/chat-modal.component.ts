import { Component, Input, OnInit } from '@angular/core';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseChat } from 'src/app/core/models/course-chat.model';
import { CourseService } from 'src/app/core/services/course.service';

@Component({
  selector: 'app-chat-modal',
  templateUrl: './chat-modal.component.html',
  styleUrls: ['./chat-modal.component.scss'],
})
export class ChatModalComponent implements OnInit {
  @Input() data?: any;
  @Input() title?: any;
  @Input() topicId?: any;
  @Input() topicTime?: any;
  @Input() profilePicture?: any;
  @Input() isFirstTime?: any;
  @Input() courseChatPresent?: any;
  @Input() selectedChatId?: any;
  @Input() chatTopicTime?: any;
  @Input() currentVideoTime?: any;

  _httpConstants: HttpConstants = new HttpConstants();
  courseChat: Array<CourseChat> = [];

  courseChatHistory: Array<any> = [];
  chatTopicId: any;
  askQuestion: any;
  chatSentTime: any;
  showSpinner: boolean = false;

  constructor(private _courseService: CourseService) {}

  ngOnInit(): void {
    this.getSectionTopicsAndChatQuestion();
  }

  getSectionTopicsAndChatQuestion() {
    this._courseService.getSectionAndTopicsChatQuestion(this.data).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseChat = response?.data;
          this.courseChat.map((course) => {
            course?.topics.map((topic) => {
              topic?.chatTopicHistory.map((chatTopicHistory) => {
                if (chatTopicHistory.chatId === this.selectedChatId) {
                  topic['active'] = true;
                }
              });
            });
          });

          this.courseChat.map((course) => {
            course?.topics.map((topic) => {
              if (topic.topicId === this.topicId) {
                topic?.chatTopicHistory.map((chatTopicHistory) => {
                  if (chatTopicHistory.time == this.chatSentTime) {
                    this.selectedChatId = chatTopicHistory?.chatId;
                  }
                  if (chatTopicHistory.chatId === this.selectedChatId) {
                    topic['active'] = true;
                  }
                });
              }
            });
          });

          this.getCourseChatHistory(
            this.selectedChatId,
            this.chatTopicTime,
            this.topicId
          );
        }
      },
      error: (error: any) => {
        this.courseChat = [];
        this.courseChatHistory = [];
        this.courseChatPresent = false;
      },
    });
  }

  getCourseChatHistory(chatId: any, chatTime: any, topicId: any) {
    this.chatTopicId = topicId;
    this.topicId = topicId;
    this.selectedChatId = chatId;
    this.chatTopicTime = chatTime;
    this._courseService?.getCourseChatHistory(chatId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseChatHistory = [];
          this.courseChatHistory = response?.data;
        }
      },
      error: (error: any) => {},
    });
  }

  sendMessage() {
    this.showSpinner = true;
    let chatPayLoad = {
      courseId: this.data,
      topicId: this.topicId,
      question: this.askQuestion,
      time: this.currentVideoTime ?? '02:56',
    };
    this.chatSentTime = this.currentVideoTime;
    this._courseService.sendMessageInChat(chatPayLoad).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.askQuestion = '';
          this.showSpinner = false;
          this.courseChatHistory.push(response?.data);

          this.getSectionTopicsAndChatQuestion();
        }
      },
      error: (error: any) => {},
    });
  }

  deleteChat(chat?: any){
    this._courseService.deleteChat(this.data, chat?.chatId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseChatHistory = [];
          this.getSectionTopicsAndChatQuestion();
        }
      },
      error: (error: any) => {
      },
    });
  }

}
