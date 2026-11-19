import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginRequest {
  email?: string | null;
  password?: string | null;
}

export interface SignupRequest {
  id?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  password?: string | null;
  roles?: string[] | null;
}

export interface AuthResponse {
  token: string;
  id: string;
  email: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8081/api/auth';
  private readonly TOKEN_KEY = 'tsb_jwt_token';

  constructor(private http: HttpClient, private router: Router) {}

  login(data: LoginRequest): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
    tap(response => {
      if (response && response.token) {
        this.setToken(response.token);

        // ✅ Stocker l'ID utilisateur
        if (response.id) {
          localStorage.setItem('userId', response.id);
        }

        // ✅ Stocker les rôles
        localStorage.setItem('roles', JSON.stringify(response.roles));
      }
    })
  );
}

  signup(data: SignupRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/signup`, data);
  }

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  clearToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getUserId(): string | null {
    return localStorage.getItem('userId');
  }

  getRoles(): string[] {
    const rolesStr = localStorage.getItem('roles');
    if (rolesStr) {
      try {
        return JSON.parse(rolesStr);
      } catch {
        return [rolesStr];
      }
    }
    return [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  logout(): void {
  localStorage.removeItem('tsb_jwt_token');
  localStorage.removeItem('roles');
  localStorage.removeItem('userId');
  this.router.navigate(['/login']);
  }
}