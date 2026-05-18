import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Ticket, BookTicketRequest, TicketBookingResponse } from '../models/ticket.model';

@Injectable({ providedIn: 'root' })
export class TicketService {

  private baseUrl = 'http://localhost:8081/tickets/tickets';

  constructor(private http: HttpClient) {}

  bookTicket(req: BookTicketRequest): Observable<TicketBookingResponse> {
    return this.http.post<TicketBookingResponse>(`${this.baseUrl}/book`, req);
  }

  /** Backend returns HTTP 200 with empty body — use responseType:'text' to avoid
   *  JSON-parse error on empty string, then map to void. */
  cancelTicket(ticketId: number): Observable<void> {
    return this.http.post(
      `${this.baseUrl}/cancel/${ticketId}`, {},
      { responseType: 'text' }
    ).pipe(map(() => void 0));
  }

  getTicketsByUser(userId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.baseUrl}/user/${userId}`);
  }

  getTicketsByEvent(eventId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.baseUrl}/event/${eventId}`);
  }
}
