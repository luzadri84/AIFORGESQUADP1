import { bootstrapApplication } from '@angular/platform-browser';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';
import { AppComponent } from './app/app.component';
import { authInterceptor } from './app/acceso/auth.interceptor';
bootstrapApplication(AppComponent, {providers: [provideZonelessChangeDetection(), provideAnimationsAsync(),
  provideHttpClient(withInterceptors([authInterceptor])), providePrimeNG({theme: {preset: Aura, options: {darkModeSelector: false}}})]
}).catch(() => { document.body.textContent='No fue posible iniciar Booking. Recargue la página.'; });
