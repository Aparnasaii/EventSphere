import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit, OnDestroy {

  form: FormGroup;
  loading = false;
  sent    = false;
  error   = '';
  sentTo  = '';

  /* â”€â”€ Slideshow (same images as login) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  slides = [
    { image: '/assets/images/event-1.jpg', tag: 'Live Music',       accent: '#f59e0b' },
    { image: '/assets/images/event-2.jpg', tag: 'Tech Conferences', accent: '#06b6d4' },
    { image: '/assets/images/event-3.jpg', tag: 'Art & Culture',    accent: '#ec4899' },
    { image: '/assets/images/event-4.jpg', tag: 'Sports Events',    accent: '#10b981' },
    { image: '/assets/images/event-5.jpg', tag: 'Food & Lifestyle', accent: '#f97316' }
  ];

  activeSlide = 0;
  private timer: any;

  constructor(
    private fb:   FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.timer = setInterval(() => {
      this.activeSlide = (this.activeSlide + 1) % this.slides.length;
    }, 5000);
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  get emailCtrl() { return this.form.get('email')!; }

  goToSlide(i: number): void { this.activeSlide = i; }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.error   = '';
    this.sentTo  = this.emailCtrl.value;

    this.auth.forgotPassword({ email: this.sentTo }).subscribe({
      next: () => { this.loading = false; this.sent = true; },
      error: err => {
        this.loading = false;
        const body = err.error;
        if (typeof body === 'string' && body.trim()) this.error = body;
        else if (body?.message) this.error = body.message;
        else this.error = 'Could not send reset link. Please try again.';
      }
    });
  }

  resend(): void { this.sent = false; this.error = ''; }
  goLogin(): void { this.router.navigate(['/auth/login']); }
}

