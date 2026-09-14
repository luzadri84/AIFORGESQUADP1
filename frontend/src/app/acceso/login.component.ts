import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { finalize } from 'rxjs';
import { AuthService } from './auth.service';
@Component({selector:'app-login',standalone:true,imports:[ReactiveFormsModule,ButtonModule,InputTextModule],template:`
<section class="login-panel panel"><span class="eyebrow">TU ESPACIO DE TRABAJO</span><h1>Un lugar para<br>cada encuentro.</h1>
<p class="muted">Accede para reservar salas, auditorios y espacios de colaboración.</p>
<form [formGroup]="form" (ngSubmit)="submit()" class="grid gap-4 mt-6">
<label for="username">Usuario</label><input pInputText id="username" formControlName="username" autocomplete="username">
<label for="password">Contraseña</label><input pInputText id="password" type="password" formControlName="password" autocomplete="current-password">
@if(error()){<p role="alert" class="error">{{error()}}</p>}
<p-button type="submit" label="Entrar" [loading]="busy()" [disabled]="form.invalid || busy()" />
</form><p class="footnote">Acceso local · Usa las credenciales configuradas para este entorno.</p></section>`})
export class LoginComponent {
 private readonly auth=inject(AuthService);private readonly fb=inject(FormBuilder);
 readonly form=this.fb.nonNullable.group({username:['',Validators.required],password:['',Validators.required]});
 readonly busy=signal(false);readonly error=signal('');
 submit(){if(this.form.invalid || this.busy())return;this.busy.set(true);this.error.set('');const v=this.form.getRawValue();
 this.auth.login(v.username,v.password).pipe(finalize(()=>{this.busy.set(false);this.form.controls.password.reset();})).subscribe({error:()=>this.error.set('No fue posible acceder. Revisa tus credenciales y la conexión.')});}
}
