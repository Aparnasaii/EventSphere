import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

interface Slide {
  image:    string;
  tag:      string;
  headline: string;
  em:       string;
  desc:     string;
  accent:   string;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {

  // â”€â”€ Slideshow â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  activeSlide = 0;
  private timer: any;
  private readonly INTERVAL = 4800;

  slides: Slide[] = [
    {
      image:    'assets/images/event-1.jpg',
      tag:      'Live Music',
      headline: 'Feel the',
      em:       'Energy',
      desc:     'Lose yourself in the rhythm of live performances. From indie bands to stadium headliners — every beat creates a memory.',
      accent:   '#F59E0B'
    },
    {
      image:    'assets/images/event-2.jpg',
      tag:      'Nightlife',
      headline: 'Dance',
      em:       'Until Dawn',
      desc:     'The night is alive. Curated parties, legendary DJs and electric atmospheres that pulse through your soul.',
      accent:   '#A78BFA'
    },
    {
      image:    'assets/images/event-3.jpg',
      tag:      'Live Shows',
      headline: 'Epic',
      em:       'Moments',
      desc:     'Stand in the crowd as beams of light cut through the dark. These are the nights that define who you are.',
      accent:   '#7ECEFD'
    },
    {
      image:    'assets/images/event-4.jpg',
      tag:      'Festivals',
      headline: 'Your Story,',
      em:       'Captured',
      desc:     'Every festival is a universe of colour and sound. Discover, attend and share experiences that define a generation.',
      accent:   '#FB923C'
    },
    {
      image:    'assets/images/event-5.jpg',
      tag:      'Art & Culture',
      headline: 'Beyond the',
      em:       'Ordinary',
      desc:     'Art exhibitions, cultural showcases and immersive installations — experience creativity in its purest form.',
      accent:   '#34D399'
    }
  ];

  // â”€â”€ Modal â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  modalOpen               = false;
  modalMode: 'login' | 'register' = 'login';

  // â”€â”€ Login form â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  loginForm:    FormGroup;
  loginLoading  = false;
  loginError    = '';
  showPass      = false;

  // â”€â”€ Register form â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  regForm:     FormGroup;
  regLoading   = false;
  regError     = '';
  regSuccess   = '';
  showRegPass  = false;

  roles = [
    { value: 'ROLE_ATTENDEE',  label: 'Attend Events',   desc: 'Discover and book experiences' },
    { value: 'ROLE_ORGANIZER', label: 'Organise Events', desc: 'Create and manage tickets'      }
  ];

  knowMoreOpen = false;

  services = [
    { icon: 'ticket',           accent: '#fbbf24', title: 'Easy Ticket Booking', desc: 'Book tickets for any event in seconds with our seamless checkout — free or paid.' },
    { icon: 'search',           accent: '#60a5fa', title: 'Event Discovery',     desc: 'Browse thousands of events by category, city, and date. Never miss what matters to you.' },
    { icon: 'layout-dashboard', accent: '#a78bfa', title: 'Organizer Dashboard', desc: 'Create events, manage seats, track bookings and get insights — all from one place.' },
    { icon: 'shield-check',     accent: '#34d399', title: 'Secure Payments',     desc: 'Stripe-powered checkout with instant confirmation. Your money is always safe.' },
    { icon: 'star',             accent: '#f87171', title: 'Ratings & Reviews',   desc: 'Genuine feedback from verified attendees helps you pick the best experiences.' },
    { icon: 'zap',              accent: '#22d3ee', title: 'Real-time Updates',   desc: 'Instant notifications for bookings, reminders and event changes — always in the loop.' },
  ];

  testimonials = [
    { name: 'Priya Sharma',    role: 'Attendee',    avatar: 'P', stars: 5,
      text: 'EventSphere changed how I discover events! Booked 3 concerts this month and every experience was seamless.' },
    { name: 'Rahul Mehta',     role: 'Organizer',   avatar: 'R', stars: 5,
      text: 'Managing 500+ attendees was effortless. The dashboard gives me everything I need — real-time, no fuss.' },
    { name: 'Ananya Krishnan', role: 'Attendee',    avatar: 'A', stars: 5,
      text: 'Found the most amazing food festival! The category filters made it so easy to find exactly what I wanted.' },
    { name: 'Vikram Singh',    role: 'Organizer',   avatar: 'V', stars: 4,
      text: 'Ticket sales for my tech conference doubled compared to last year. The platform just works.' },
    { name: 'Deepika Patel',   role: 'Attendee',    avatar: 'D', stars: 5,
      text: 'I love seeing all upcoming events at a glance. The seat availability badges keep me from missing out!' },
    { name: 'Arjun Nair',      role: 'Organizer',   avatar: 'A', stars: 5,
      text: 'Best event platform in India. Approval process is quick and support is incredibly responsive.' },
  ];

  scrollTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  }

  constructor(
    private fb:     FormBuilder,
    private auth:   AuthService,
    private router: Router,
    private route:  ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.regForm = this.fb.group({
      fullName:      ['', [Validators.required, Validators.minLength(2)]],
      email:         ['', [Validators.required, Validators.email]],
      password:      ['', [Validators.required, Validators.minLength(6)]],
      contactNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      role:          ['ROLE_ATTENDEE', Validators.required]
    });
  }

  ngOnInit(): void {
    const mode = this.route.snapshot.queryParamMap.get('mode');
    if (mode === 'register') { this.openModal('register'); }
    this.startAutoScroll();
  }

  ngOnDestroy(): void {
    this.stopAutoScroll();
    document.body.style.overflow = '';
  }

  // â”€â”€ Slideshow â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  startAutoScroll(): void {
    this.stopAutoScroll();
    this.timer = setInterval(() => this.nextSlide(), this.INTERVAL);
  }

  stopAutoScroll(): void {
    if (this.timer) { clearInterval(this.timer); this.timer = null; }
  }

  nextSlide(): void {
    this.activeSlide = (this.activeSlide + 1) % this.slides.length;
  }

  goToSlide(i: number): void {
    this.activeSlide = i;
    this.startAutoScroll();   // reset timer so next auto-advance is from now
  }

  advanceSlide(): void {
    this.nextSlide();
    this.startAutoScroll();
  }

  // â”€â”€ Modal â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  openModal(mode: 'login' | 'register'): void {
    this.modalMode = mode;
    this.modalOpen = true;
    this.stopAutoScroll();
    document.body.style.overflow = 'hidden';
  }

  closeModal(): void {
    this.modalOpen  = false;
    this.loginError = '';
    this.regError   = '';
    this.regSuccess = '';
    document.body.style.overflow = '';
    this.startAutoScroll();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void { if (this.modalOpen) this.closeModal(); }

  // â”€â”€ Login submit â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  submitLogin(): void {
    if (this.loginForm.invalid) { this.loginForm.markAllAsTouched(); return; }
    this.loginLoading = true;
    this.loginError   = '';

    this.auth.login(this.loginForm.value).subscribe({
      next: () => {
        document.body.style.overflow = '';
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.loginLoading = false;
        const body = err.error;
        const status = err.status;

        if (status === 401) {
          this.loginError = 'Invalid email or password.';
        } else if (status === 403) {
          // Could be pending approval or rejected account
          const msg = (typeof body === 'string' ? body : body?.message) || '';
          this.loginError = msg.toLowerCase().includes('pending')
            ? 'Your organizer account is awaiting admin approval.'
            : msg.toLowerCase().includes('suspend') || msg.toLowerCase().includes('inactive')
              ? 'Your account has been suspended. Please contact support.'
              : 'Access denied. Please contact support.';
        } else if (status === 500) {
          // Backend internal error — could be DB issue, wrong credentials format, etc.
          // Try to show the backend message if it's useful, else generic
          const serverMsg = typeof body === 'string'
            ? body.trim()
            : body?.message || '';
          this.loginError = (serverMsg && !serverMsg.toLowerCase().includes('internal server error'))
            ? serverMsg
            : 'Server error. Please try again in a moment.';
        } else if (status === 0) {
          this.loginError = 'Cannot reach the server. Check your connection and try again.';
        } else if (typeof body === 'string' && body.trim()) {
          this.loginError = body.trim();
        } else if (body && typeof body === 'object') {
          this.loginError = body.message || body.error || 'Sign in failed. Please try again.';
        } else {
          this.loginError = 'Sign in failed. Please try again.';
        }
      }
    });
  }

  // â”€â”€ Register submit â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  submitRegister(): void {
    if (this.regForm.invalid) { this.regForm.markAllAsTouched(); return; }
    this.regLoading = true;
    this.regError   = '';

    this.auth.register(this.regForm.value).subscribe({
      next: () => {
        this.regSuccess = 'Account created! Switching to login…';
        this.regLoading = false;
        setTimeout(() => {
          this.regSuccess = '';
          this.regForm.reset({ role: 'ROLE_ATTENDEE' });
          this.modalMode  = 'login';
        }, 1800);
      },
      error: err => {
        this.regLoading = false;
        const body = err.error;
        const status = err.status;
        if (status === 409 || (typeof body === 'string' && body.toLowerCase().includes('already'))) {
          this.regError = 'This email is already registered. Please sign in instead.';
        } else if (status === 500) {
          this.regError = 'Server error. Please try again in a moment.';
        } else if (status === 0) {
          this.regError = 'Cannot reach the server. Check your connection.';
        } else if (typeof body === 'string' && body.trim()) {
          this.regError = body.trim();
        } else if (body && typeof body === 'object') {
          this.regError = body.message || body.error || 'Registration failed. Please try again.';
        } else {
          this.regError = 'Registration failed. Please try again.';
        }
      }
    });
  }

  get lf() { return this.loginForm.controls; }
  get rf() { return this.regForm.controls; }
}

