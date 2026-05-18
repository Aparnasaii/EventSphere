import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Feedback, FeedbackRequest } from '../models/feedback.model';

@Injectable({ providedIn: 'root' })
export class FeedbackService {

  // Backend context-path = /feedbacks → controller at /feedback
  // Full URL: http://localhost:8081/feedbacks/feedback/...
  private baseUrl = 'http://localhost:8081/feedbacks/feedback';

  constructor(private http: HttpClient) {}

  /**
   * Submit (update) a trigger-created feedback record via PUT.
   * The backend trigger creates a PENDING record when a ticket is CONFIRMED.
   */
  submitFeedback(feedbackId: number, req: FeedbackRequest): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/submit/${feedbackId}`,
      req,
      { responseType: 'text' }
    );
  }

  /**
   * Initiate the feedback cycle directly via the feedback service.
   * POST /feedbacks/feedback/initiate — creates PENDING records for all confirmed attendees.
   * No role restriction on backend.
   */
  initiateFeedback(eventId: number, eventName: string): Observable<string> {
    return this.http.post(
      `${this.baseUrl}/initiate`,
      { eventId, eventName },
      { responseType: 'text' }
    );
  }

  /** Average rating for an event */
  getAverageRating(eventId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/average/${eventId}`);
  }

  /** All submitted comments for an event */
  getComments(eventId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/comments/${eventId}`);
  }

  /**
   * Fetch THIS user's feedback record for a specific event.
   * Returns Feedback object (with feedbackId) or throws 404 if none exists.
   */
  getUserFeedback(eventId: number, userId: number): Observable<Feedback> {
    return this.http.get<Feedback>(`${this.baseUrl}/event/${eventId}/user/${userId}`);
  }

  /**
   * Fallback: get ALL feedback records for a user.
   * Used when getUserFeedback returns 404 but a record might still exist.
   */
  getFeedbacksByUser(userId: number): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.baseUrl}/user/${userId}`);
  }
}
