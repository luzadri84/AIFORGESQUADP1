import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { switchMap, tap, catchError, throwError } from 'rxjs';
@Injectable({providedIn: 'root'})
export class AuthService {
  private readonly http=inject(HttpClient);
  readonly username=signal('');
  private authorization=''; private csrf='';
  headers(): Record<string,string> { return this.authorization ? {'Authorization':this.authorization,...(this.csrf ? {'X-CSRF-TOKEN':this.csrf}: {})} : {}; }
  login(username:string,password:string) {
    this.logout();
    this.authorization='Basic '+btoa(String.fromCharCode(...new TextEncoder().encode(username+':'+password)));
    return this.http.get<{username:string}>('/api/me').pipe(switchMap(me =>
      this.http.get<{token:string}>('/api/csrf').pipe(tap(token => {this.csrf=token.token;this.username.set(me.username);}))),
      catchError((error:unknown) => {this.logout();return throwError(() => error);}));
  }
  logout() { this.authorization='';this.csrf='';this.username.set(''); }
}
