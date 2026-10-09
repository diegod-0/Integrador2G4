import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginResponse, UserSession } from '../models/auth.model';

const TOKEN_KEY = 'rescuelink_jwt_token';
const USER_KEY = 'rescuelink_user_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly _session = signal<UserSession | null>(this.readInitialSession());
  readonly session = this._session.asReadonly();

  get token(): string | null {
    return this._session()?.accessToken ?? localStorage.getItem(TOKEN_KEY);
  }

  get isAuthenticated(): boolean {
    return !!this._session();
  }

  get currentUser(): UserSession | null {
    return this._session();
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { email, password }).pipe(
      tap(response => {
        if (response && response.success && response.data) {
          this.setSession(response.data);
        }
      })
    );
  }

  loginAsDemoAdmin(): Observable<LoginResponse> {
    return this.login('admin@rescuelink.org', 'password123');
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._session.set(null);
  }

  private setSession(session: UserSession): void {
    localStorage.setItem(TOKEN_KEY, session.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(session));
    this._session.set(session);
  }

  private readInitialSession(): UserSession | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
