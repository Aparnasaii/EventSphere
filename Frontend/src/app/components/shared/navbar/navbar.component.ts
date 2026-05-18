import { Component, OnInit } from '@angular/core';
import { AuthService }         from '../../../services/auth.service';
import { UserService }         from '../../../services/user.service';
import { NotificationService } from '../../../services/notification.service';
import { User }                from '../../../models/user.model';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  userMenuOpen = false;
  sidebarOpen  = true;   // controls both desktop collapse & mobile slide
  isDark       = false;
  pendingCount = 0;
  unreadCount  = 0;

  constructor(
    public  auth:     AuthService,
    private userSvc:  UserService,
    private notifSvc: NotificationService
  ) {}

  ngOnInit(): void {
    this.auth.currentUser().subscribe(u => {
      this.currentUser = u;
      if (u?.userId) {
        this.notifSvc.getUnread(u.userId).subscribe({
          next: list => this.unreadCount = list.length,
          error: () => {}
        });
      }
    });
    this.isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    if (this.auth.isAdmin()) {
      this.userSvc.getPendingOrganizers().subscribe({
        next: list => this.pendingCount = list.length,
        error: () => {}
      });
    }
    // Push content right to clear sidebar
    document.body.classList.remove('sidebar-collapsed');
  }

  /** Toggle sidebar â€” logo click, hamburger click */
  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
    document.body.classList.toggle('sidebar-collapsed', !this.sidebarOpen);
    if (!this.sidebarOpen) this.userMenuOpen = false;
  }

  /** Nav-link click: only close sidebar on mobile */
  closeMobileMenu(): void {
    if (window.innerWidth <= 900) {
      this.sidebarOpen = false;
      document.body.classList.add('sidebar-collapsed');
    }
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : '');
  }

  logout(): void { this.auth.logout(); }

  getRoleBadge(): string {
    const r = this.auth.getRole();
    if (r === 'ROLE_ADMIN')     return 'Admin';
    if (r === 'ROLE_ORGANIZER') return 'Organizer';
    return 'Attendee';
  }

  getRoleClass(): string {
    const r = this.auth.getRole();
    if (r === 'ROLE_ADMIN')     return 'admin';
    if (r === 'ROLE_ORGANIZER') return 'organizer';
    return 'attendee';
  }
}

