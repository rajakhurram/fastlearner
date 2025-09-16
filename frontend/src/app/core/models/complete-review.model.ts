export interface CompleteReview {
    rating1?:          number;
    rating2?:          number;
    rating3?:          number;
    rating4?:          number;
    rating5?:          number;
    totalReview?:      number;
    totalUsers?:       number;
    feedbackComments?: FeedbackComment[];
}

export interface FeedbackComment {
    reviewId?:  number;
    comment?:   string;
    userName?:  string;
    rating?:    number;
    createdAt?: Date;
    likes?:     number;
    dislikes?:  number;
    profileImage?: string;
}
