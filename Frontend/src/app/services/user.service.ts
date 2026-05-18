import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  private baseUrl = 'http://localhost:8081/users/api/users';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/all`);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  getPendingOrganizers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/admin/pending`);
  }

  approveOrganizer(id: number): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/admin/approve/${id}`, {});
  }

  rejectOrganizer(id: number): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/admin/reject/${id}`, {});
  }

  suspendUser(id: number): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/admin/suspend/${id}`, {});
  }

  reactivateUser(id: number): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/admin/reactivate/${id}`, {});
  }

  deleteUser(id: number): Observable<string> {
    // Try admin-namespaced endpoint first (matches backend pattern)
    return this.http.delete(`${this.baseUrl}/admin/${id}`, { responseType: 'text' });
  }
}
