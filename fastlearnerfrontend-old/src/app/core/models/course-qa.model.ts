export interface QuestionAnswers {
    questionDetails?: QuestionDetail[];
    pageNo?:          number;
    pageSize?:        number;
    totalElements?:   number;
    totalPages?:      number;
    replyText?: string;
}

export interface QuestionDetail {
    questionId?:   number;
    questionText?: string;
    topicId?:      number;
    topicName?:    string;
    courseId?:     number;
    userName?:     string;
    totalReplies?: number;
    showQuestionAnswers?: boolean;
    answerDetail?: AnswerDetail[];
}

export interface AnswerDetail {
    answerId?:         number;
    answerText?:       string;
    answerDetailList?: null;
    userName?:         string;
    totalReplies?:     number;
}
