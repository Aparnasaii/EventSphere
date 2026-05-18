import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit, NgZone } from '@angular/core';
import { ActivatedRoute }   from '@angular/router';
import { EventService }     from '../../../services/event.service';
import { AuthService }      from '../../../services/auth.service';
import { TicketService }    from '../../../services/ticket.service';
import { Event }            from '../../../models/event.model';
import { forkJoin, of, Subscription } from 'rxjs';
import { catchError }       from 'rxjs/operators';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.css']
})
export class EventListComponent implements OnInit, AfterViewInit, OnDestroy {

  // â”€â”€ Data â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  events:   Event[] = [];
  filtered: Event[] = [];
  loading   = true;
  search    = '';
  category  = '';
  location  = '';
  toast     = '';
  toastType = 'info';

  // Mode flags (driven by query params)
  pastMode       = false;   // ?past=true
  registeredMode = false;   // ?registered=true  (attendee: booked upcoming events)
  allEventsMode  = false;   // ?view=all         (organizer: browse all events)

  private routeSub!: Subscription;

  // â”€â”€ Infinite Scroll â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  visibleCount  = 9;
  batchSize     = 6;
  loadingMore   = false;

  @ViewChild('scrollSentinel') sentinelRef!: ElementRef;
  private observer!: IntersectionObserver;

  categories = ['Music', 'Technology', 'Sports', 'Art', 'Food', 'Business'];

  categoryData = [
    { value: 'Music',      label: 'Music',    icon: 'music',     image: 'assets/images/event-1.jpg',      accent: '#f59e0b' },
    { value: 'Technology', label: 'Tech',      icon: 'cpu',       image: 'assets/images/cat-tech2.jpg',    accent: '#818cf8' },
    { value: 'Sports',     label: 'Sports',    icon: 'trophy',    image: 'assets/images/cat-sports.jpg',   accent: '#fb923c' },
    { value: 'Art',        label: 'Art',       icon: 'palette',   image: 'assets/images/cat-art.jpg',      accent: '#34d399' },
    { value: 'Food',       label: 'Food',      icon: 'utensils',  image: 'assets/images/cat-food2.jpg',    accent: '#f87171' },
    { value: 'Business',   label: 'Business',  icon: 'briefcase', image: 'assets/images/cat-business.jpg', accent: '#a5b4fc' }
  ];

  constructor(
    private route:      ActivatedRoute,
    private eventSvc:   EventService,
    public  auth:       AuthService,
    private ticketSvc:  TicketService,
    private zone:       NgZone
  ) {}

  ngOnInit(): void {
    // Subscribe to queryParamMap so re-navigation (same route, different params)
    // always triggers a fresh load â€” fixes the "Past Events won't reload" bug.
    this.routeSub = this.route.queryParamMap.subscribe(params => {
      const paymentStatus = params.get('payment');
      if (paymentStatus === 'cancelled') {
        this.showToast('Payment cancelled. Your ticket was not booked.', 'info');
      }

      this.pastMode       = params.get('past')       === 'true';
      this.registeredMode = params.get('registered') === 'true';
      this.allEventsMode  = params.get('view')       === 'all';
      this.category       = params.get('category')   ?? '';

      // Reset display state on every navigation
      this.loading      = true;
      this.events       = [];
      this.filtered     = [];
      this.visibleCount = this.batchSize + 3;

      this.loadEvents();
    });
  }

  private loadEvents(): void {
    const user = this.auth.getCurrentUser();
    const now  = new Date();

    if (this.auth.isOrganizer() && user?.userId && !this.allEventsMode) {
      // Organizer: show only their own events (unless ?view=all)
      this.eventSvc.getEventsByOrganizer(user.userId).pipe(catchError(() => of([]))).subscribe(evts => {
        this.applyModeAndFilter(evts as Event[], now);
      });

    } else if (this.auth.isAttendee() && (this.pastMode || this.registeredMode) && user?.userId) {
      // Attendee in past/registered mode: load all events + tickets, then filter to booked only
      forkJoin({
        all:  this.eventSvc.getAllEvents().pipe(catchError(() => of([]))),
        tkts: this.ticketSvc.getTicketsByUser(user.userId).pipe(catchError(() => of([])))
      }).subscribe(({ all, tkts }) => {
        // Only count tickets that are still active (CONFIRMED or PENDING_PAYMENT).
        // CANCELLED and FAILED tickets must NOT show up in "Registered" or "Past".
        const activeTickets = (tkts as any[]).filter(
          (t: any) => t.status === 'CONFIRMED' || t.status === 'PENDING_PAYMENT'
        );
        const bookedIds = new Set(activeTickets.map((t: any) => t.eventId));
        const mine = (all as Event[]).filter(e => bookedIds.has(e.eventId));
        this.applyModeAndFilter(mine, now);
      });

    } else {
      // Default: all events
      this.eventSvc.getAllEvents().pipe(catchError(() => of([]))).subscribe(evts => {
        this.applyModeAndFilter(evts as Event[], now);
      });
    }
  }

  private applyModeAndFilter(evts: Event[], now: Date): void {
    let list = evts;

    if (this.pastMode) {
      // Past mode: events that have already ended
      list = evts
        .filter(e => {
          const end = (e as any).endTime ? new Date((e as any).endTime) : new Date(e.startTime);
          return end < now;
        })
        .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());

    } else if (this.registeredMode) {
      // Registered mode (attendee): upcoming booked events only
      list = evts
        .filter(e => {
          const end = (e as any).endTime ? new Date((e as any).endTime) : new Date(e.startTime);
          return end > now;
        })
        .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());

    } else {
      // Normal / All Events mode: upcoming only
      list = evts
        .filter(e => {
          const end = (e as any).endTime ? new Date((e as any).endTime) : new Date(e.startTime);
          return end > now;
        })
        .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
    }

    this.events  = list;
    this.loading = false;
    this.applyFilter();
  }

  ngAfterViewInit(): void {
    this.setupIntersectionObserver();
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    if (this.observer) this.observer.disconnect();
  }

  // â”€â”€ Infinite Scroll setup â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  private setupIntersectionObserver(): void {
    this.zone.runOutsideAngular(() => {
      this.observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting && !this.loadingMore) {
          this.zone.run(() => this.loadMoreEvents());
        }
      }, { threshold: 0.1 });

      if (this.sentinelRef?.nativeElement) {
        this.observer.observe(this.sentinelRef.nativeElement);
      }
    });
  }

  loadMoreEvents(): void {
    if (this.visibleCount >= this.filtered.length) return;
    this.loadingMore = true;
    setTimeout(() => {
      this.visibleCount = Math.min(this.visibleCount + this.batchSize, this.filtered.length);
      this.loadingMore  = false;
    }, 400);
  }

  get visibleEvents(): Event[] {
    return this.filtered.slice(0, this.visibleCount);
  }

  get hasMore(): boolean {
    return this.visibleCount < this.filtered.length;
  }

  get pageTitle(): string {
    if (this.registeredMode)                              return 'My Registered Events';
    if (this.pastMode)                                    return 'Past Events';
    if (this.auth.isOrganizer() && this.allEventsMode)   return 'All Events';
    if (this.auth.isOrganizer())                         return 'My Events';
    return 'Discover Events';
  }

  get pageSubtitle(): string {
    if (this.registeredMode)                              return 'Events you have booked tickets for';
    if (this.pastMode && this.auth.isAttendee())          return 'Events you attended in the past';
    if (this.pastMode)                                    return 'Completed and archived events';
    if (this.auth.isOrganizer() && this.allEventsMode)   return 'Browse and discover all events on EventSphere';
    if (this.auth.isOrganizer())                         return 'Manage the events you have created';
    return 'Find and book extraordinary experiences near you';
  }

  // â”€â”€ Filters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  applyFilter(): void {
    const cat = this.category.toLowerCase();
    this.filtered = this.events.filter(e => {
      const matchSearch   = !this.search   || e.name.toLowerCase().includes(this.search.toLowerCase());
      const matchCategory = !this.category || (e.category ?? '').toLowerCase() === cat;
      const matchLocation = !this.location || (e.location ?? '').toLowerCase().includes(this.location.toLowerCase());
      return matchSearch && matchCategory && matchLocation;
    });
    this.visibleCount = this.batchSize + 3;
  }

  clearFilter(): void {
    this.search = ''; this.category = ''; this.location = '';
    this.filtered     = this.events;
    this.visibleCount = this.batchSize + 3;
  }

  // â”€â”€ Permission helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  canDeleteEvent(event: Event): boolean {
    if (this.auth.isAdmin()) return true;
    if (this.auth.isOrganizer()) {
      const userId = this.auth.getCurrentUser()?.userId;
      return (event as any).organizerId === userId;
    }
    return false;
  }

  canEditEvent(event: Event): boolean {
    return this.canDeleteEvent(event);
  }

  // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  getCategoryImage(cat: string): string {
    return this.categoryData.find(c => c.value === cat)?.image ?? 'assets/images/event-1.jpg';
  }

  getCategoryAccent(cat: string): string {
    return this.categoryData.find(c => c.value === cat)?.accent ?? '#6366f1';
  }

  getCategoryIcon(cat: string): string {
    const map: any = {
      Music: 'music', Technology: 'cpu', Sports: 'trophy',
      Art: 'palette', Food: 'utensils', Business: 'briefcase'
    };
    return map[cat] ?? 'calendar-days';
  }

  getCategoryLabel(cat: string): string {
    return this.categoryData.find(c => c.value === cat)?.label ?? cat;
  }

  isPast(event: Event): boolean {
    // An event is "past" once its endTime has passed (or startTime if no endTime)
    const end = (event as any).endTime ? new Date((event as any).endTime) : new Date(event.startTime);
    return end < new Date();
  }

  isEnded(event: Event): boolean {
    return this.isPast(event);
  }

  deleteEvent(id: number): void {
    if (!confirm('Delete this event? This action cannot be undone.')) return;
    this.eventSvc.deleteEvent(id).subscribe({
      next: () => {
        this.events   = this.events.filter(e => e.eventId !== id);
        this.filtered = this.filtered.filter(e => e.eventId !== id);
        this.showToast('Event deleted successfully.', 'success');
      },
      error: err => {
        const msg = err?.error?.message || err?.message || 'Failed to delete event.';
        this.showToast(msg, 'error');
      }
    });
  }

  showToast(msg: string, type: string): void {
    this.toast = msg; this.toastType = type;
    setTimeout(() => this.toast = '', 5000);
  }
}

