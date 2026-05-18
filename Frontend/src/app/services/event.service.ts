import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, EventSearchParams } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventService {

  private baseUrl = 'http://localhost:8081/events/events';

  constructor(private http: HttpClient) {}

  getAllEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(this.baseUrl);
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.baseUrl}/${id}`);
  }

  createEvent(event: Partial<Event>): Observable<Event> {
    return this.http.post<Event>(this.baseUrl, event);
  }

  updateEvent(id: number, event: Partial<Event>): Observable<Event> {
    return this.http.put<Event>(`${this.baseUrl}/${id}`, event);
  }

  deleteEvent(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  searchEvents(params: EventSearchParams): Observable<Event[]> {
    let httpParams = new HttpParams();
    if (params.category) httpParams = httpParams.set('category', params.category);
    if (params.location) httpParams = httpParams.set('location', params.location);
    if (params.date)     httpParams = httpParams.set('date', params.date);
    return this.http.get<Event[]>(`${this.baseUrl}/search`, { params: httpParams });
  }

  getEventsByCategory(category: string): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.baseUrl}/category/${category}`);
  }

  getEventsByOrganizer(organizerId: number): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.baseUrl}/organizer/${organizerId}`);
  }

  getEventsByLocation(location: string): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.baseUrl}/location/${location}`);
  }

  getEventsByTimeRange(start: string, end: string): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.baseUrl}/range`, {
      params: { start, end }
    });
  }

  triggerFeedback(eventId: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/${eventId}/feedback`, {},
      { responseType: 'text' });
  }
}
