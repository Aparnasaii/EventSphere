import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeedbackService } from '../../services/feedback.service';
import { EventService }    from '../../services/event.service';
import { AuthService }     from '../../services/auth.service';
import { TicketService }   from '../../services/ticket.service';
import { Event }           from '../../models/event.model';
import { TicketStatus }    from '../../models/ticket.model';
import { catchError }      from 'rxjs/operators';
import { of, forkJoin }    from 'rxjs';

@Component({
  selector: 'app-feedback',
  templateUrl: './feedback.component.html',
  styleUrls: ['./feedback.component.css']
})
export class FeedbackComponent implements OnInit {
  eventId!: number;
  event: Event | null = null;
  rating    = 0;
  comment   = '';
  loading   = false;
  submitted = false;
  toast     = '';
  toastType = 'success';

  averageRating: number | null = null;
  comments: string[] = [];

  /** feedbackId from the PENDING record the organizer created */
  feedbackId: number | null = null;

  /** True once event has fully ended */
  eventEnded = false;

  /** True while loading */
  feedbackLoading = true;

  /** True if the attendee has a CONFIRMED ticket for this event */
  hasTicket = false;

  /**
   * True when event ended + has ticket + loading done + no feedbackId found.
   * Means organizer hasn't triggered the feedback cycle yet.
   */
  feedbackNotReady = false;

  private pendingLoads = 0;

  constructor(
    private route:       ActivatedRoute,
    private feedbackSvc: FeedbackService,
    private eventSvc:    EventService,
    private ticketSvc:   TicketService,
    public  auth:        AuthService
  ) {}

  /** Tracks if we've already auto-triggered the feedback cycle (avoid loops) */
  private autoTriggered = false;

  private markLoaded(): void {
    this.pendingLoads--;
    if (this.pendingLoads <= 0) {
      this.feedbackLoading = false;

      // Admins don't submit feedback — only attendees do. Skip all the
      // trigger / not-ready logic for them.
      if (this.auth.isAdmin()) {
        this.feedbackNotReady = false;
        return;
      }

      const needsTrigger =
        this.hasTicket && this.eventEnded && !this.submitted && this.feedbackId === null;

      if (needsTrigger && !this.autoTriggered) {
        // Auto-initiate the feedback cycle silently in the background
        this.autoTriggered = true;
        this.autoInitiateFeedback();
      } else {
        this.feedbackNotReady = needsTrigger;
      }
    }
  }

  /**
   * Silently initiate the feedback cycle, then reload this user's feedback record.
   * Tries the direct feedback-service endpoint first (no role check), then falls
   * back to the event-service trigger endpoint. If both fail, shows "not ready".
   */
  private autoInitiateFeedback(): void {
    const eventName = (this.event as any)?.name || `Event #${this.eventId}`;

    this.feedbackSvc.initiateFeedback(this.eventId, eventName).subscribe({
      next: () => this.scheduleReload(),
      error: () => {
        // Fallback: try event-service trigger endpoint
        this.eventSvc.triggerFeedback(this.eventId).subscribe({
          next:  () => this.scheduleReload(),
          error: () => { this.feedbackNotReady = true; }
        });
      }
    });
  }

  private scheduleReload(): void {
    setTimeout(() => {
      this.pendingLoads    = 1;
      this.feedbackLoading = true;
      this.loadMyFeedback();
    }, 700);
  }

  ngOnInit(): void {
    this.eventId         = Number(this.route.snapshot.paramMap.get('eventId'));
    const user           = this.auth.getCurrentUser();
    this.feedbackLoading = true;
    this.pendingLoads    = 2;

    // (1) Load event details + ticket ownership
    forkJoin({
      event:   this.eventSvc.getEventById(this.eventId).pipe(catchError(() => of(null))),
      tickets: (this.auth.isAttendee() && user?.userId)
               ? this.ticketSvc.getTicketsByUser(user.userId).pipe(catchError(() => of([])))
               : of([])
    }).subscribe(({ event, tickets }) => {
      this.event = event as Event | null;
      if (event) {
        const end = (event as any).endTime
          ? new Date((event as any).endTime)
          : new Date((event as any).startTime);
        this.eventEnded = end < new Date();
      }
      if (this.auth.isAttendee()) {
        this.hasTicket = (tickets as any[]).some(
          (t: any) => Number(t.eventId) === this.eventId && t.status === TicketStatus.CONFIRMED
        );
      } else {
        this.hasTicket = true; // Admins / Organizers bypass ticket check
      }
      this.markLoaded();
    });

    // (2) Load this user's existing feedback record + public stats
    this.loadStats();
    this.loadMyFeedback();
  }

  loadStats(): void {
    this.feedbackSvc.getAverageRating(this.eventId).subscribe({
      next: avg => this.averageRating = avg,
      error: () => {}
    });
    this.feedbackSvc.getComments(this.eventId).subscribe({
      next: c => this.comments = c,
      error: () => {}
    });
  }

  loadMyFeedback(): void {
    const user = this.auth.getCurrentUser();
    if (!user?.userId) { this.markLoaded(); return; }

    // Strategy 1: GET by eventId + userId — returns 404 if no PENDING record exists yet
    this.feedbackSvc.getUserFeedback(this.eventId, user.userId).pipe(
      catchError(() => {
        // Strategy 2: GET all feedbacks for this user, find one matching this event
        return this.feedbackSvc.getFeedbacksByUser(user.userId).pipe(
          catchError(() => of([] as any[]))
        );
      })
    ).subscribe((result: any) => {
      let fb: any = null;
      if (Array.isArray(result)) {
        fb = result.find((f: any) =>
          Number(f.eventId) === this.eventId || Number(f.event?.eventId) === this.eventId
        ) ?? null;
      } else if (result && typeof result === 'object') {
        fb = result;
      }

      if (fb) {
        const fid = fb.feedbackId ?? fb.id ?? null;
        this.feedbackId = fid ? Number(fid) : null;
        this.rating     = fb.rating  ?? 0;
        this.comment    = fb.comment ?? '';

        const status = (fb.status ?? '').toUpperCase();
        this.submitted = ['COMPLETED', 'APPROVED', 'SUBMITTED'].includes(status);
      }

      this.markLoaded();
    });
  }

  setRating(r: number): void {
    if (!this.submitted) this.rating = r;
  }

  /** Manual "Try Again" button when auto-trigger failed */
  retryInitiate(): void {
    this.feedbackNotReady = false;
    this.autoTriggered    = false;
    this.feedbackLoading  = true;
    this.pendingLoads     = 1;
    this.loadMyFeedback();
  }

  submit(): void {
    if (this.rating === 0) {
      this.showToast('Please select a star rating before submitting.', 'error');
      return;
    }
    if (!this.feedbackId) {
      this.showToast('Feedback form is not ready yet. Ask the organizer to initiate feedback.', 'error');
      return;
    }
    const user = this.auth.getCurrentUser();
    if (!user?.userId) return;

    this.loading = true;
    this.feedbackSvc.submitFeedback(this.feedbackId, {
      rating:  this.rating,
      comment: this.comment
    }).subscribe({
      next:  () => this.onSuccess(),
      error: err => this.onError(err)
    });
  }

  private onSuccess(): void {
    this.loading   = false;
    this.submitted = true;
    this.feedbackNotReady = false;
    this.loadStats();
  }

  private onError(err: any): void {
    this.loading = false;
    const body = err?.error;
    let msg = 'Submission failed. Please try again.';

    if (typeof body === 'string' && body.trim()) {
      msg = body.trim();
    } else if (body && typeof body === 'object') {
      msg = body.message || body.error || body.detail || msg;
    } else if (err.status === 403) {
      msg = 'You do not have permission to submit feedback for this event.';
    } else if (err.status === 404) {
      msg = 'Feedback record not found. Please ask the organizer to initiate the feedback cycle.';
    } else if (err.status === 500) {
      msg = 'Server error. Please try again later.';
    }
    this.showToast(msg, 'error');
  }

  showToast(msg: string, type: string): void {
    this.toast = msg; this.toastType = type;
    setTimeout(() => this.toast = '', 4000);
  }
}
