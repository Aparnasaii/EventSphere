import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { AuthService }  from '../../../services/auth.service';

@Component({
  selector: 'app-event-form',
  templateUrl: './event-form.component.html',
  styleUrls: ['./event-form.component.css']
})
export class EventFormComponent implements OnInit {

  form!: FormGroup;
  isEdit    = false;
  eventId!: number;
  loading   = false;
  fetching  = false;
  toast     = '';
  toastType = 'success';

  /** Preserved on edit so we don't accidentally reset remaining seats */
  private originalAvailableSeats = 0;

  categories = ['Music', 'Technology', 'Sports', 'Art', 'Food', 'Business'];

  /** Minimum allowed datetime (now, in local ISO format for the input) */
  get minDateTime(): string {
    return this.toLocalISO(new Date().toISOString());
  }

  /** Validator: startTime must be in the future */
  private futureTimeValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const selected = new Date(control.value);
    const now      = new Date();
    return selected <= now ? { pastDate: true } : null;
  }

  /** Validator: endTime must be after startTime AND event must be at least 1 hour long */
  private endAfterStartValidator(group: AbstractControl): ValidationErrors | null {
    const start = group.get('startTime')?.value;
    const end   = group.get('endTime')?.value;
    if (!start || !end) return null;
    const startMs = new Date(start).getTime();
    const endMs   = new Date(end).getTime();
    if (endMs <= startMs) return { endBeforeStart: true };
    // Minimum 1 hour (3,600,000 ms)
    if (endMs - startMs < 60 * 60 * 1000) return { tooShort: true };
    return null;
  }

  constructor(
    private fb:       FormBuilder,
    private route:    ActivatedRoute,
    private router:   Router,
    private eventSvc: EventService,
    public  auth:     AuthService
  ) {}

  ngOnInit(): void {
    // Form uses startTime / endTime matching the backend field names exactly
    this.form = this.fb.group({
      name:          ['', [Validators.required, Validators.minLength(3)]],
      description:   ['', [Validators.required, Validators.minLength(10)]],
      location:      ['', Validators.required],
      category:      ['Technology', Validators.required],
      startTime:     ['', [Validators.required, this.futureTimeValidator.bind(this)]],
      endTime:       ['', Validators.required],
      totalCapacity: [100, [Validators.required, Validators.min(1)]],
      ticketPrice:   [0,   [Validators.required, Validators.min(0)]]
    }, { validators: this.endAfterStartValidator.bind(this) });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit  = true;
      this.eventId = Number(id);
      this.fetching = true;
      this.eventSvc.getEventById(this.eventId).subscribe({
        next: evt => {
          this.originalAvailableSeats = evt.availableSeats;
          this.form.patchValue({
            name:          evt.name,
            description:   evt.description,
            location:      evt.location,
            category:      evt.category,
            startTime:     this.toLocalISO(evt.startTime),
            endTime:       this.toLocalISO(evt.endTime),
            totalCapacity: evt.totalCapacity,
            ticketPrice:   evt.ticketPrice
          });
          this.fetching = false;
        },
        error: () => {
          this.fetching = false;
          this.showToast('Failed to load event', 'error');
        }
      });
    }
  }

  /** Strip seconds so <input type="datetime-local"> accepts the value */
  toLocalISO(dt: string): string {
    if (!dt) return '';
    const d = new Date(dt);
    const offset = d.getTimezoneOffset() * 60000;
    return new Date(d.getTime() - offset).toISOString().slice(0, 16);
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;

    const raw      = this.form.value;
    const user     = this.auth.getCurrentUser();

    // Derive eventDate (LocalDate) from the start datetime
    const eventDate = raw.startTime ? raw.startTime.split('T')[0] : '';

    // Build payload matching backend Event entity exactly
    const payload: any = {
      name:           raw.name,
      description:    raw.description,
      location:       raw.location,
      category:       raw.category,
      startTime:      raw.startTime,   // "yyyy-MM-ddTHH:mm"  â†’ stored as LocalDateTime
      endTime:        raw.endTime,
      eventDate:      eventDate,       // "yyyy-MM-dd"        â†’ stored as LocalDate
      totalCapacity:  raw.totalCapacity,
      // On create â†’ all seats are available; on edit â†’ keep the existing remaining count
      availableSeats: this.isEdit ? this.originalAvailableSeats : raw.totalCapacity,
      ticketPrice:    raw.ticketPrice,
      status:         'ACTIVE',        // default status on create; kept on edit
      organizerId:    user?.userId ?? 0
    };

    const obs = this.isEdit
      ? this.eventSvc.updateEvent(this.eventId, payload)
      : this.eventSvc.createEvent(payload);

    obs.subscribe({
      next: () => {
        this.loading = false;
        this.showToast(this.isEdit ? 'Event updated!' : 'Event created!', 'success');
        setTimeout(() => this.router.navigate(['/events']), 1500);
      },
      error: err => {
        this.loading = false;
        this.showToast(err.error?.message || 'Operation failed', 'error');
      }
    });
  }

  showToast(msg: string, type: string): void {
    this.toast = msg; this.toastType = type;
    setTimeout(() => this.toast = '', 4000);
  }

  get f() { return this.form.controls; }
}

