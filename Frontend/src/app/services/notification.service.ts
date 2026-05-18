import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface NotificationItem {
  id: number;
  userId: number;
  subject: string;
  message: string;
  type: string;
  status: string;        // SENT | FAILED
  sentAt: string;
  templateName?: string; // optional metadata
}

@Injectable({ providedIn: 'root' })
export class NotificationService {

  // Gateway: /notifications/** → strips "notifications" → service gets /api/v1/notifications/...
  private baseUrl = 'http://localhost:8081/notifications/api/v1/notifications';

  constructor(private http: HttpClient) {}

  getHistory(userId: number): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(`${this.baseUrl}/user/${userId}`);
  }

  getUnread(userId: number): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(`${this.baseUrl}/user/${userId}/unread`);
  }

  markAsRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/read`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
