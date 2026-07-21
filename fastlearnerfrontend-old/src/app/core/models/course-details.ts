import { CourseType } from '../enums/course-status';

export interface CourseDetails {
  courseId: number;
  categoryName: string;
  title: string;
  metaDescription?: string;
  metaHeading?: string;
  metaTitle?: string;
  price?: number;
  courseType?: CourseType;
  about: string;
  courseDescription: string;
  level: string;
  userId: number;
  creatorName: string;
  profilePicture: string;
  userProfileUrl: string;
  headline: string;
  aboutMe: string;
  totalCourses: number;
  totalEnrolled: number;
  totalStudents: number;
  lastUpdate: null;
  courseDuration: string;
  noOfTopics: number;
  prerequisite: string[];
  courseOutcome: string[];
  review: number;
  noOfReviewers: number;
  courseThumbnailUrl: string;
  previewVideoUrl: string;
  previewVideoVttContent?: string;
  isFavourite: boolean;
  isEnrolled: boolean;
  isAlreadyBought?: boolean;
  hasCertificate: boolean;
  sectionDetails: SectionDetail[];
  courseFeedback: CourseFeedback;
  contentType: any;
}

export interface CourseFeedback {
  rating1: number;
  rating2: number;
  rating3: number;
  rating4: number;
  rating5: number;
  feedbackComments: FeedbackComment[];
}

export interface FeedbackComment {
  reviewId: number;
  comment: string;
  userName: string;
  rating: number;
  createdAt: Date;
  likes: number;
  dislikes: number;
  profileImage?: string;
}

export interface SectionDetail {
  panelOpen: boolean;
  sectionId: number;
  sectionName: string;
  sectionLevel: number;
  sectionDuration: number;
  isFree: boolean;
  topicDetails: TopicDetail[];
  totalLectures: number;
  totalTime: number;
}

export interface TopicDetail {
  sectionId: number;
  topicId: number;
  topicName: string;
  topicType: string;
  topicLevel: number;
  topicDuration: number;
  videoId: number;
  videoFileName: string;
  videoUrl: string;
  quizId: null;
  quizTitle: string;
  quizQuestionId: null;
  quizQuestionText: string;
  quizQuestionAnwserList: QuizQuestionAnwserList[] | null;
}

export interface QuizQuestionAnwserList {
  id: number;
  answerText: string;
}
