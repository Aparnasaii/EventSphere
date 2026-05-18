import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pw  = group.get('newPassword')?.value;
  const cpw = group.get('confirm')?.value;
  return pw && cpw && pw !== cpw ? { mismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit, OnDestroy {

  form: FormGroup;
  token        = '';
  loading      = false;
  done         = false;
  error        = '';
  showPw       = false;
  showCpw      = false;
  invalidToken = false;

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
    private fb:     FormBuilder,
    private route:  ActivatedRoute,
    private router: Router,
    private auth:   AuthService
  ) {
    this.form = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirm:     ['', Validators.required]
    }, { validators: passwordsMatch });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.invalidToken = true;
      this.error = 'This reset link is invalid or has already been used. Please request a new one.';
    }

    this.timer = setInterval(() => {
      this.activeSlide = (this.activeSlide + 1) % this.slides.length;
    }, 5000);
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  get pw()  { return this.form.get('newPassword')!; }
  get cpw() { return this.form.get('confirm')!; }

  goToSlide(i: number): void { this.activeSlide = i; }

  submit(): void {
    if (this.form.invalid || !this.token) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error   = '';

    this.auth.resetPassword({ token: this.token, newPassword: this.pw.value }).subscribe({
      next: () => {
        this.loading = false;
        this.done    = true;
        setTimeout(() => this.router.navigate(['/auth/login']), 3000);
      },
      error: err => {
        this.loading = false;
        const body = err.error;
        if (typeof body === 'string' && body.trim()) this.error = body;
        else if (body?.message) this.error = body.message;
        else this.error = 'Password reset failed. The link may have expired.';
      }
    });
  }

  goLogin():    void { this.router.navigate(['/auth/login']); }
  requestNew(): void { this.router.navigate(['/auth/forgot-password']); }
}

