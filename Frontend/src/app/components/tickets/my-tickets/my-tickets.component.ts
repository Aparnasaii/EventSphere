import { Component, OnInit } from '@angular/core';
import { ActivatedRoute }   from '@angular/router';
import { forkJoin, of }     from 'rxjs';
import { catchError, map }  from 'rxjs/operators';
import { TicketService }    from '../../../services/ticket.service';
import { EventService }     from '../../../services/event.service';
import { AuthService }      from '../../../services/auth.service';
import { Ticket, TicketStatus } from '../../../models/ticket.model';

interface TicketView extends Ticket {
  eventName?:      string;
  eventStartTime?: string;
  eventEndTime?:   string;
}

@Component({
  selector: 'app-my-tickets',
  templateUrl: './my-tickets.component.html',
  styleUrls: ['./my-tickets.component.css']
})
export class MyTicketsComponent implements OnInit {
  tickets: TicketView[] = [];
  loading   = true;
  toast     = '';
  toastType = 'success';

  constructor(
    private route:     ActivatedRoute,
    private ticketSvc: TicketService,
    private eventSvc:  EventService,
    public  auth:      AuthService
  ) {}

  ngOnInit(): void {
    // Check if we were redirected back from Stripe
    const paymentStatus = this.route.snapshot.queryParamMap.get('payment');
    if (paymentStatus === 'success') {
      this.showToast('Payment successful! Your ticket is confirmed.', 'success');
    } else if (paymentStatus === 'failed') {
      this.showToast('Payment could not be processed. Please try again.', 'error');
    }

    const user = this.auth.getCurrentUser();
    if (!user?.userId) { this.loading = false; return; }

    this.ticketSvc.getTicketsByUser(user.userId).subscribe({
      next: tickets => {
        if (tickets.length === 0) { this.tickets = []; this.loading = false; return; }

        // Fetch event details in parallel
        const eventCalls = tickets.map(t =>
          this.eventSvc.getEventById(t.eventId).pipe(
            catchError(() => of(null))
          )
        );

        forkJoin(eventCalls).subscribe((events: any[]) => {
          const all = tickets.map((t, i) => ({
            ...t,
            eventName:      events[i]?.name      ?? `Event #${t.eventId}`,
            eventStartTime: events[i]?.startTime ?? null,
            eventEndTime:   events[i]?.endTime   ?? null
          }));
          // Show all statuses â€” PENDING_PAYMENT included so users can track unpaid tickets
          this.tickets = all;
          this.loading = false;
        });
      },
      error: () => { this.loading = false; }
    });
  }

  cancel(id: number): void {
    if (!confirm('Cancel this ticket? This action cannot be undone.')) return;
    this.ticketSvc.cancelTicket(id).subscribe({
      next: () => {
        const t = this.tickets.find(x => x.ticketId === id);
        if (t) t.status = TicketStatus.CANCELLED;
        this.showToast('Ticket cancelled successfully', 'success');
      },
      error: err => {
        const body = err.error;
        let msg = 'Failed to cancel ticket';
        if (typeof body === 'string' && body.trim()) {
          msg = body;
        } else if (body && typeof body === 'object') {
          msg = body.message || body.error || msg;
        }
        this.showToast(msg, 'error');
      }
    });
  }

  isEventEnded(ticket: TicketView): boolean {
    if (!ticket.eventStartTime) return false;
    const end = ticket.eventEndTime
      ? new Date(ticket.eventEndTime)
      : new Date(ticket.eventStartTime);
    return end < new Date();
  }

  getStatusClass(status: string): string {
    if (status === 'CONFIRMED')       return 'chip-green';
    if (status === 'CANCELLED')       return 'chip-red';
    if (status === 'FAILED')          return 'chip-red';
    if (status === 'PENDING_PAYMENT') return 'chip-orange';
    return 'chip-blue';
  }

  showToast(msg: string, type: string): void {
    this.toast = msg; this.toastType = type;
    setTimeout(() => this.toast = '', 3500);
  }
}

