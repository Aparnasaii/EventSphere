import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { User } from '../../../models/user.model';

type FilterKey = 'all' | 'admin' | 'organizer' | 'attendee' | 'pending' | 'suspended';

@Component({
  selector: 'app-manage-users',
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.css']
})
export class ManageUsersComponent implements OnInit {

  users:    User[] = [];
  loading   = true;
  search    = '';
  toast     = '';
  toastType = 'success';
  activeFilter: FilterKey = 'all';

  constructor(private userSvc: UserService) {}

  ngOnInit(): void {
    this.userSvc.getAllUsers().subscribe({
      next: u => { this.users = u; this.loading = false; },
      error: err => {
        this.loading = false;
        this.showToast(err?.error?.message || 'Failed to load users', 'error');
      }
    });
  }

  // ── Stat counts ──────────────────────────────────
  get totalCount():     number { return this.users.length; }
  get adminCount():     number { return this.users.filter(u => u.role === 'ROLE_ADMIN').length; }
  get organizerCount(): number { return this.users.filter(u => u.role === 'ROLE_ORGANIZER').length; }
  get attendeeCount():  number { return this.users.filter(u => u.role === 'ROLE_ATTENDEE').length; }
  get pendingCount():   number { return this.users.filter(u => u.status === 'PENDING').length; }
  get suspendedCount(): number { return this.users.filter(u => u.status === 'SUSPENDED').length; }

  setFilter(f: FilterKey): void { this.activeFilter = f; }

  get filtered(): User[] {
    const q = this.search.toLowerCase().trim();
    return this.users.filter(u => {
      // Filter chip
      if (this.activeFilter === 'admin'     && u.role   !== 'ROLE_ADMIN')     return false;
      if (this.activeFilter === 'organizer' && u.role   !== 'ROLE_ORGANIZER') return false;
      if (this.activeFilter === 'attendee'  && u.role   !== 'ROLE_ATTENDEE')  return false;
      if (this.activeFilter === 'pending'   && u.status !== 'PENDING')        return false;
      if (this.activeFilter === 'suspended' && u.status !== 'SUSPENDED')      return false;
      // Search
      if (!q) return true;
      return (u.fullName || '').toLowerCase().includes(q) ||
             (u.email    || '').toLowerCase().includes(q);
    });
  }

  approve(id: number): void {
    this.userSvc.approveOrganizer(id).subscribe({
      next: updated => {
        this.patchUser(id, updated);
        this.showToast('Organizer approved successfully!', 'success');
      },
      error: err => this.showToast(err?.error?.message || 'Failed to approve organizer', 'error')
    });
  }

  suspend(user: User): void {
    if (!confirm(`Suspend ${user.fullName}? They won't be able to log in.`)) return;
    this.userSvc.suspendUser(user.userId).subscribe({
      next: updated => {
        this.patchUser(user.userId, updated);
        this.showToast(`${user.fullName} has been suspended.`, 'success');
      },
      error: err => this.handleActionError(err, 'suspend')
    });
  }

  reactivate(user: User): void {
    this.userSvc.reactivateUser(user.userId).subscribe({
      next: updated => {
        this.patchUser(user.userId, updated);
        this.showToast(`${user.fullName} has been reactivated.`, 'success');
      },
      error: err => this.handleActionError(err, 'reactivate')
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Permanently delete ${user.fullName}? This cannot be undone.`)) return;
    this.userSvc.deleteUser(user.userId).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.userId !== user.userId);
        this.showToast(`${user.fullName} has been deleted.`, 'success');
      },
      error: err => this.handleActionError(err, 'delete')
    });
  }

  /** Extract a useful message from any backend error format and log it for diagnostics */
  private handleActionError(err: any, action: string): void {
    console.error(`[ManageUsers] ${action} failed:`, err);
    let msg = `Failed to ${action} user.`;
    if (err?.status === 404) {
      msg = `This endpoint isn't available on the server. Please restart the user-service so the new ${action} endpoint is loaded.`;
    } else if (err?.status === 403) {
      msg = 'You do not have permission for this action.';
    } else if (err?.status === 0) {
      msg = 'Could not reach the server. Is the user-service running?';
    } else if (typeof err?.error === 'string' && err.error.trim()) {
      msg = err.error.trim();
    } else if (err?.error?.message) {
      msg = err.error.message;
    } else if (err?.message) {
      msg = err.message;
    }
    this.showToast(msg, 'error');
  }

  private patchUser(id: number, updated: User): void {
    const idx = this.users.findIndex(u => u.userId === id);
    if (idx > -1) this.users[idx] = updated;
  }

  showToast(msg: string, type: string): void {
    this.toast = msg; this.toastType = type;
    setTimeout(() => this.toast = '', 4000);
  }

  getRoleBadge(role: string): string {
    if (role === 'ROLE_ADMIN')     return 'Admin';
    if (role === 'ROLE_ORGANIZER') return 'Organizer';
    return 'Attendee';
  }

  getRoleClass(role: string): string {
    if (role === 'ROLE_ADMIN')     return 'admin';
    if (role === 'ROLE_ORGANIZER') return 'organizer';
    return 'attendee';
  }

  getRoleIcon(role: string): string {
    if (role === 'ROLE_ADMIN')     return 'shield-check';
    if (role === 'ROLE_ORGANIZER') return 'calendar-days';
    return 'ticket';
  }

  getStatusClass(status: string): string {
    return (status || '').toLowerCase();
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }
}
