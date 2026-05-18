import { Component, OnInit, HostListener } from '@angular/core';
import { AuthService }     from '../../services/auth.service';
import { EventService }    from '../../services/event.service';
import { TicketService }   from '../../services/ticket.service';
import { FeedbackService } from '../../services/feedback.service';
import { forkJoin, of }    from 'rxjs';
import { catchError }      from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  totalEvents    = 0;
  myTickets      = 0;
  loading        = true;

  /** Upcoming events (startTime >= now) â€” shown for all roles */
  upcomingEvents: any[] = [];
  /** Past events (startTime < now) â€” shown for all roles */
  pastEvents:     any[] = [];
  /** Events the attendee has booked tickets for */
  myBookedEvents: any[] = [];
  /** My organised events (organizer role) */
  myOrgEvents:    any[] = [];
  /** Events happening TODAY â€” used for the reminder banner */
  todayEvents:    any[] = [];
  reminderDismissed = false;

  /** Past events the attendee booked but hasn't given feedback for yet */
  pendingFeedbackEvents: any[] = [];

  /* Parallax offsets */
  p1 = 0; p2 = 0; p3 = 0; p4 = 0;

  constructor(
    public  auth:     AuthService,
    private events:   EventService,
    private tickets:  TicketService,
    private feedback: FeedbackService
  ) {}

  @HostListener('window:scroll', [])
  onScroll(): void {
    const y = window.scrollY;
    this.p1 = y * 0.12; this.p2 = y * 0.25;
    this.p3 = y * 0.40; this.p4 = y * 0.55;
  }

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    const now  = new Date();

    const todayStr = now.toISOString().slice(0, 10); // "YYYY-MM-DD"

    if (this.auth.isOrganizer() && user?.userId) {
      // â”€â”€ ORGANIZER â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
      forkJoin({
        all:  this.events.getAllEvents().pipe(catchError(() => of([]))),
        mine: this.events.getEventsByOrganizer(user.userId).pipe(catchError(() => of([])))
      }).subscribe(({ all, mine }) => {
        this.totalEvents  = (all as any[]).length;
        const isEndedOrg = (e: any) => {
          const end = e.endTime ? new Date(e.endTime) : new Date(e.startTime);
          return end < now;
        };

        this.myOrgEvents  = (mine as any[]).sort((a: any, b: any) =>
          new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        );
        this.upcomingEvents = (mine as any[])
          .filter((e: any) => !isEndedOrg(e))
          .sort((a: any, b: any) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
        this.pastEvents = (mine as any[])
          .filter((e: any) => isEndedOrg(e))
          .sort((a: any, b: any) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());
        // Today's events reminder for organizer â€” only if event hasn't ended yet
        this.todayEvents = (mine as any[]).filter((e: any) => {
          if (!e.startTime || e.startTime.slice(0, 10) !== todayStr) return false;
          const finish = e.endTime ? new Date(e.endTime) : new Date(e.startTime);
          return finish > now;   // exclude already-completed events
        });
        this.loading = false;
      });

    } else if (this.auth.isAttendee() && user?.userId) {
      // â”€â”€ ATTENDEE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
      forkJoin({
        all:    this.events.getAllEvents().pipe(catchError(() => of([]))),
        myTkts: this.tickets.getTicketsByUser(user.userId).pipe(catchError(() => of([])))
      }).subscribe(({ all, myTkts }) => {
        this.totalEvents = (all as any[]).length;
        this.myTickets   = (myTkts as any[]).length;

        // Only count active tickets — cancelled / failed tickets must NOT appear in
        // "Registered Events", "Past Events", or "Today's reminder".
        const activeTkts = (myTkts as any[]).filter(
          (t: any) => t.status === 'CONFIRMED' || t.status === 'PENDING_PAYMENT'
        );
        const bookedIds = new Set(activeTkts.map(t => t.eventId));

        // Helper: check if event has fully ended using endTime
        const isEnded = (e: any) => {
          const end = e.endTime ? new Date(e.endTime) : new Date(e.startTime);
          return end < now;
        };
        const isUpcoming = (e: any) => !isEnded(e);

        // Registered events = upcoming booked events only
        this.myBookedEvents = (all as any[])
          .filter((e: any) => bookedIds.has(e.eventId) && isUpcoming(e))
          .sort((a: any, b: any) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());

        // Upcoming events = all upcoming (not ended) for discovery
        this.upcomingEvents = (all as any[])
          .filter((e: any) => isUpcoming(e))
          .sort((a: any, b: any) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())
          .slice(0, 6);

        // Past events = only events the attendee booked that have now ended
        this.pastEvents = (all as any[])
          .filter((e: any) => isEnded(e) && bookedIds.has(e.eventId))
          .sort((a: any, b: any) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());

        // Today's events reminder for attendee â€” only booked events that haven't ended yet
        this.todayEvents = (all as any[]).filter((e: any) => {
          if (!bookedIds.has(e.eventId)) return false;
          if (!e.startTime || e.startTime.slice(0, 10) !== todayStr) return false;
          const finish = e.endTime ? new Date(e.endTime) : new Date(e.startTime);
          return finish > now;   // exclude already-completed events
        });

        this.loading = false;

        // â”€â”€ Compute pending-feedback reminders for past events â”€â”€
        // Only CONFIRMED ticket events that have ended qualify. Then we check
        // the feedback record's status â€” if it's not COMPLETED, show a reminder.
        this.computePendingFeedback(user.userId);
      });

    } else {
      // â”€â”€ ADMIN â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
      this.events.getAllEvents().pipe(catchError(() => of([]))).subscribe((all: any[]) => {
        this.totalEvents    = all.length;
        const isEndedAdmin = (e: any) => {
          const end = e.endTime ? new Date(e.endTime) : new Date(e.startTime);
          return end < now;
        };

        this.upcomingEvents = all
          .filter(e => !isEndedAdmin(e))
          .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())
          .slice(0, 6);
        this.pastEvents = all
          .filter(e => isEndedAdmin(e))
          .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime())
          .slice(0, 6);
        // Admin reminder â€” only events today that haven't ended yet
        this.todayEvents = all.filter(e => {
          if (!e.startTime || e.startTime.slice(0, 10) !== todayStr) return false;
          const finish = e.endTime ? new Date(e.endTime) : new Date(e.startTime);
          return finish > now;
        });
        this.loading = false;
      });

      if (user?.userId) {
        this.tickets.getTicketsByUser(user.userId).pipe(catchError(() => of([]))).subscribe(
          (t: any[]) => this.myTickets = t.length
        );
      }
    }
  }

  categories = [
    { value: 'Music',      label: 'Music',      icon: 'music',     image: 'assets/images/event-1.jpg',      accent: '#f59e0b' },
    { value: 'Technology', label: 'Tech',        icon: 'cpu',       image: 'assets/images/cat-tech2.jpg',    accent: '#818cf8' },
    { value: 'Sports',     label: 'Sports',      icon: 'trophy',    image: 'assets/images/cat-sports.jpg',   accent: '#fb923c' },
    { value: 'Art',        label: 'Art',         icon: 'palette',   image: 'assets/images/cat-art.jpg',      accent: '#34d399' },
    { value: 'Food',       label: 'Food',        icon: 'utensils',  image: 'assets/images/cat-food2.jpg',    accent: '#f87171' },
    { value: 'Business',   label: 'Business',    icon: 'briefcase', image: 'assets/images/cat-business.jpg', accent: '#a5b4fc' }
  ];

  getGreeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good Morning';
    if (h < 17) return 'Good Afternoon';
    return 'Good Evening';
  }

  getCategoryIcon(cat: string): string {
    const map: any = {
      Music: 'music', Technology: 'cpu', Sports: 'trophy',
      Art: 'palette', Food: 'utensils', Business: 'briefcase'
    };
    return map[cat] ?? 'calendar-days';
  }

  getCategoryImage(cat: string): string {
    return this.categories.find(c =>
      c.value.toLowerCase() === cat?.toLowerCase()
    )?.image ?? 'assets/images/event-1.jpg';
  }

  getCategoryAccent(cat: string): string {
    return this.categories.find(c =>
      c.value.toLowerCase() === cat?.toLowerCase()
    )?.accent ?? '#6366f1';
  }

  isPastEvent(event: any): boolean {
    return new Date(event.startTime) < new Date();
  }

  /**
   * Build the pending-feedback reminder list. For each past event the attendee
   * booked, check if they've already submitted feedback. Only show events where
   * the user has NOT submitted feedback (status not COMPLETED).
   *
   * Strategy:
   *   1) Try the bulk endpoint GET /feedback/user/{userId}.
   *   2) If that fails (e.g. backend not restarted), fall back to checking
   *      each past event individually via GET /feedback/event/{eId}/user/{uId}.
   */
  private computePendingFeedback(userId: number): void {
    if (this.pastEvents.length === 0) {
      this.pendingFeedbackEvents = [];
      return;
    }

    this.feedback.getFeedbacksByUser(userId).subscribe({
      next: (feedbacks: any[]) => this.applyFeedbackStatuses(feedbacks || []),
      error: () => this.computePendingFeedbackPerEvent(userId)
    });
  }

  /** Build the status map and filter past events */
  private applyFeedbackStatuses(feedbacks: any[]): void {
    const statusByEvent = new Map<number, string>();
    feedbacks.forEach((f: any) => {
      const eid = Number(f.eventId ?? f.event?.eventId);
      const st  = (f.status ?? '').toUpperCase();
      if (eid) statusByEvent.set(eid, st);
    });
    this.pendingFeedbackEvents = this.pastEvents
      .filter((e: any) => {
        const st = statusByEvent.get(Number(e.eventId));
        return !st || st !== 'COMPLETED';
      })
      .slice(0, 4);
  }

  /** Fallback: check each past event individually (slower but works on old backend) */
  private computePendingFeedbackPerEvent(userId: number): void {
    const checks = this.pastEvents.map((e: any) =>
      this.feedback.getUserFeedback(e.eventId, userId).pipe(
        catchError(() => of({ eventId: e.eventId, status: null }))
      )
    );
    forkJoin(checks).subscribe((results: any[]) => {
      this.applyFeedbackStatuses(results);
    });
  }

  get rolePillClass(): string {
    const r = this.auth.getRole() ?? '';
    if (r.includes('ADMIN'))     return 'pill-admin';
    if (r.includes('ORGANIZER')) return 'pill-organizer';
    return 'pill-attendee';
  }
}

