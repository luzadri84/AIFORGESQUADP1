import { Component, inject } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { AuthService } from './acceso/auth.service';
import { LoginComponent } from './acceso/login.component';
import { BookingsComponent } from './reservas/bookings.component';
@Component({selector:'app-root',standalone:true,imports:[ButtonModule,LoginComponent,BookingsComponent],template:`
<header><a class="brand" href="/">▦ <strong>Booking</strong><span>Espacios compartidos</span></a>@if(auth.username()){<div class="user"><span>{{auth.username()}}</span><p-button label="Salir" [text]="true" (onClick)="auth.logout()" /></div>}</header>
<main>@if(auth.username()){<app-bookings/>}@else{<app-login/>}</main><footer>Booking de espacios · Entorno local</footer>`})
export class AppComponent {readonly auth=inject(AuthService);}
