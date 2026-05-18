import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService }   from '../../../services/event.service';
import { TicketService }  from '../../../services/ticket.service';
import { AuthService }    from '../../../services/auth.service';
import { Event }          from '../../../models/event.model';

@Component({
  selector: 'app-event-detail',
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.css']
})
export class EventDetailComponent implements OnInit {
  event: Event | null = null;
  loading     = true;
  bookingModal = false;
  booking      = false;
  toast        = '';
  toastType    = 'success';
  quantity     = 1;

  constructor(
    private route:     ActivatedRoute,
    private router:    Router,
    private eventSvc:  EventService,
    private ticketSvc: TicketService,
    public  auth:      AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.eventSvc.getEventById(id).subscribe({
      next: e => { this.event = e; this.loading = false; },
      error: () => { this.loading = false; this.router.navigate(['/events']); }
    });
  }

  openBooking(): void  { this.bookingModal = true; this.quantity = 1; }
  closeBooking(): void { this.bookingModal = false; }

  get totalAmount(): number {
    return (this.event?.ticketPrice ?? 0) * this.quantity;
  }

  get maxQuantity(): number {
    return Math.min(10, this.event?.availableSeats ?? 1);
  }

  // Can the current user edit this event?
  get canEdit(): boolean {
    if (!this.event) return false;
    if (this.auth.isAdmin()) return true;
    if (this.auth.isOrganizer()) {
      return (this.event as any).organizerId === this.auth.getCurrentUser()?.userId;
    }
    return false;
  }

  // Can the current user delete this event?
  get canDelete(): boolean {
    return this.canEdit;
  }

  confirmBooking(): void {
    const user = this.auth.getCurrentUser();
    if (!user?.userId || !this.event) return;
    this.booking = true;

    this.ticketSvc.bookTicket({
      userId:   user.userId,
      eventId:  this.event.eventId,
      quantity: this.quantity
    }).subscribe({
      next: res => {
        this.booking      = false;
        this.bookingModal = false;

        if (res.checkoutUrl) {
          // Paid event â†’ redirect to Stripe
          this.showToast('Ticket reserved! Redirecting to payment...', 'success');
          setTimeout(() => window.open(res.checkoutUrl!, '_blank'), 1500);
        } else {
          // Free event â†’ confirmed immediately
          this.showToast(`${this.quantity} ticket${this.quantity > 1 ? 's' : ''} booked successfully!`, 'success');
          // Refresh the event to update seat count
          this.eventSvc.getEventById(this.event!.eventId).subscribe(e => this.event = e);
        }
      },
      error: err => {
        this.booking      = false;
        this.bookingModal = false;
        const body = err.error;
        let msg = 'Booking failed. Please try again.';
        if (typeof body === 'string' && body.trim()) {
          msg = body;
        } else if (body && typeof body === 'object') {
          msg = body.message || body.error || body.detail || msg;
        }
        this.showToast(msg, 'error');
      }
    });
  }

  deleteEvent(): void {
    if (!this.event || !confirm('Delete this event?')) return;
    this.eventSvc.deleteEvent(this.event.eventId).subscribe({
      next: () => this.router.navigate(['/events']),
      error: () => this.showToast('Failed to delete event', 'error')
    });
  }

  /** True once the event has fully ended (endTime has passed) */
  isEventEnded(): boolean {
    if (!this.event) return false;
    const end = (this.event as any).endTime
      ? new Date((this.event as any).endTime)
      : new Date(this.event.startTime);
    return end < new Date();
  }

  /** True once the event has started (startTime passed) â€” used to block booking */
  isPastEvent(): boolean {
    return this.event ? new Date(this.event.startTime) < new Date() : false;
  }

  getCategoryIcon(cat: string): string {
    const map: any = {
      Music:      'music',
      Technology: 'cpu',
      Sports:     'trophy',
      Art:        'palette',
      Food:       'utensils',
      Business:   'briefcase'
    };
    return map[cat] ?? 'calendar-days';
  }

  showToast(msg: string, type: string): void {
    this.toast = msg; this.toastType = type;
    setTimeout(() => this.toast = '', 4000);
  }
}

