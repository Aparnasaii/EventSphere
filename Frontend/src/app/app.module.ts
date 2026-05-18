import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import {
  LucideAngularModule,
  LayoutDashboard, LayoutGrid, CalendarDays, CalendarOff, Ticket, CirclePlus,
  Users, Clock4, Sun, Moon, Menu, LogOut, LogIn, ChevronDown,
  CircleCheck, Zap, Search, Hourglass,
  Music, Cpu, Trophy, Palette, Utensils, Briefcase,
  MapPin, Mail, Lock, User, UserPlus, Phone, Eye, EyeOff,
  CircleAlert, TriangleAlert, Star, Globe, Shield, ShieldCheck,
  StarHalf, Trash2, Tag, X, ArrowRight,
  Sparkles, Rocket, Hand, Smile, Frown, Signal, ClipboardList, PartyPopper,
  Check, BadgeCheck, PhoneCall,
  Pencil, Clock,
  ChevronLeft, ChevronRight,
  CheckCircle2,
  History, Archive, Info, RefreshCw,
  AlertCircle, CalendarCheck, ListChecks, Bookmark,
  PauseCircle, PlayCircle, UserCheck, UserX, Ban
} from 'lucide-angular';

import { AppRoutingModule }          from './app-routing.module';
import { AppComponent }              from './app.component';
import { JwtInterceptor }            from './interceptors/jwt.interceptor';
import { ErrorInterceptor }          from './interceptors/error.interceptor';

/* Shared */
import { NavbarComponent }           from './components/shared/navbar/navbar.component';

/* Auth */
import { LoginComponent }            from './components/auth/login/login.component';
import { RegisterComponent }         from './components/auth/register/register.component';
import { ForgotPasswordComponent }   from './components/auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent }    from './components/auth/reset-password/reset-password.component';

/* Dashboard */
import { DashboardComponent }        from './components/dashboard/dashboard.component';

/* Events */
import { EventListComponent }        from './components/events/event-list/event-list.component';
import { EventDetailComponent }      from './components/events/event-detail/event-detail.component';
import { EventFormComponent }        from './components/events/event-form/event-form.component';

/* Admin */
import { ManageUsersComponent }      from './components/admin/manage-users/manage-users.component';
import { PendingOrganizersComponent} from './components/admin/pending-organizers/pending-organizers.component';

/* Tickets */
import { MyTicketsComponent }        from './components/tickets/my-tickets/my-tickets.component';

/* Feedback */
import { FeedbackComponent }         from './components/feedback/feedback.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    LoginComponent,
    RegisterComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    DashboardComponent,
    EventListComponent,
    EventDetailComponent,
    EventFormComponent,
    ManageUsersComponent,
    PendingOrganizersComponent,
    MyTicketsComponent,
    FeedbackComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    LucideAngularModule.pick({
      LayoutDashboard, LayoutGrid, CalendarDays, CalendarOff, Ticket, CirclePlus,
      Users, Clock4, Sun, Moon, Menu, LogOut, LogIn, ChevronDown,
      CircleCheck, Zap, Search, Hourglass,
      Music, Cpu, Trophy, Palette, Utensils, Briefcase,
      MapPin, Mail, Lock, User, UserPlus, Phone, Eye, EyeOff,
      CircleAlert, TriangleAlert, Star, Globe, Shield, ShieldCheck,
      StarHalf, Trash2, Tag, X, ArrowRight,
      Sparkles, Rocket, Hand, Smile, Frown, Signal, ClipboardList, PartyPopper,
      Check, BadgeCheck, PhoneCall,
      Pencil, Clock,
      ChevronLeft, ChevronRight,
      CheckCircle2,
      History, Archive, Info, RefreshCw,
      AlertCircle, CalendarCheck, ListChecks, Bookmark,
      PauseCircle, PlayCircle, UserCheck, UserX, Ban
    })
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor,  multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
