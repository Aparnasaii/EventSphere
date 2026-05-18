import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

import { LoginComponent }             from './components/auth/login/login.component';
import { RegisterComponent }          from './components/auth/register/register.component';
import { ForgotPasswordComponent }    from './components/auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent }     from './components/auth/reset-password/reset-password.component';
import { DashboardComponent }         from './components/dashboard/dashboard.component';
import { EventListComponent }         from './components/events/event-list/event-list.component';
import { EventDetailComponent }       from './components/events/event-detail/event-detail.component';
import { EventFormComponent }         from './components/events/event-form/event-form.component';
import { ManageUsersComponent }       from './components/admin/manage-users/manage-users.component';
import { PendingOrganizersComponent } from './components/admin/pending-organizers/pending-organizers.component';
import { MyTicketsComponent }         from './components/tickets/my-tickets/my-tickets.component';
import { FeedbackComponent }          from './components/feedback/feedback.component';

const routes: Routes = [
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },

  /* Auth */
  { path: 'auth/login',           component: LoginComponent },
  { path: 'auth/register',        component: RegisterComponent },
  { path: 'auth/forgot-password', component: ForgotPasswordComponent },
  { path: 'auth/reset-password',  component: ResetPasswordComponent },

  /* Main */
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },

  /* Events */
  { path: 'events',            component: EventListComponent,   canActivate: [AuthGuard] },
  { path: 'events/create',     component: EventFormComponent,   canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ORGANIZER', 'ROLE_ADMIN'] } },
  { path: 'events/edit/:id',   component: EventFormComponent,   canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ORGANIZER', 'ROLE_ADMIN'] } },
  { path: 'events/:id',        component: EventDetailComponent, canActivate: [AuthGuard] },

  /* Tickets */
  { path: 'tickets', component: MyTicketsComponent, canActivate: [AuthGuard] },

  /* Feedback */
  { path: 'feedback/:eventId', component: FeedbackComponent, canActivate: [AuthGuard] },

  /* Admin */
  {
    path: 'admin/users',
    component: ManageUsersComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },
  {
    path: 'admin/pending',
    component: PendingOrganizersComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },

  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
