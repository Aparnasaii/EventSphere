export interface Feedback {
  feedbackId:  number;
  userId:      number;
  eventId:     number;
  rating:      number;
  comment:     string;
  submittedAt: string;   
  status?:     string;   
  eventName?:  string;   
}

export interface FeedbackRequest {
  rating:  number;
  comment: string;
}
