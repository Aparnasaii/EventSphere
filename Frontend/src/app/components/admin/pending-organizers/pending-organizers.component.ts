import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-pending-organizers',
  templateUrl: './pending-organizers.component.html',
  styleUrls: ['./pending-organizers.component.css']
})
export class PendingOrganizersComponent implements OnInit {
  users: User[] = [];
  loading   = true;
  toast     = '';
  toastType = 'success';

  constructor(private userSvc: UserService) {}

  ngOnInit(): void {
    this.userSvc.getPendingOrganizers().subscribe({
      next: u => { this.users = u; this.loading = false; },
      error: () => this.loading = false
    });
  }

  approve(id: number): void {
    this.userSvc.approveOrganizer(id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.userId !== id);
        this.showToast('Organizer approved successfully!', 'success');
      },
      error: () => this.showToast('Failed to approve organizer', 'error')
    });
  }

  reject(id: number): void {
    if (!confirm('Reject this organizer request?')) return;
    this.userSvc.rejectOrganizer(id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.userId !== id);
        this.showToast('Organizer request rejected.', 'info');
      },
      error: () => this.showToast('Failed to reject organizer', 'error')
    });
  }

  showToast(msg: string, type: string): void {
    this.toast     = msg;
    this.toastType = type;
    setTimeout(() => this.toast = '', 3500);
  }
}

