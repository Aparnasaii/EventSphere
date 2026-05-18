export interface Feedback {
  feedbackId:  number;
  userId:      number;
  eventId:     number;
  rating:      number;
  comment:     string;
  submittedAt: string;   // backend field — was incorrectly 'createdAt'
  status?:     string;   // e.g. 'PENDING' | 'APPROVED'
  eventName?:  string;   // joined from event service (not stored in entity)
}

export interface FeedbackRequest {
  rating:  number;
  comment: string;
}
