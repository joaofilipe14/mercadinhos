import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

export interface UserResponse {
  email: string;
  role: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/auth';

  // Estado reativo global do utilizador usando Signals
  currentUser = signal<{ email:string; role: string } | null>(null);

  constructor() {
    const email = localStorage.getItem('email');
    const role = localStorage.getItem('role');
    const token = localStorage.getItem('token');
    if (token && role && email) {
      this.currentUser.set({ email, role });
    }
  }

  login(email: string, password: string) {
    return this.http.post<UserResponse>(`${this.API_URL}/login`, { email, password })
      .pipe(
        tap(res => {
          localStorage.setItem('email', res.email);
          localStorage.setItem('role', res.role);
          localStorage.setItem('token', res.token);
          this.currentUser.set({ email: res.email, role: res.role });
        })
      );
  }

  logout() {
    localStorage.clear();
    this.currentUser.set(null);
  }

  hasRole(role: string): boolean {
    return this.currentUser()?.role === role;
  }
}
