import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { forkJoin, finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { BookingApi, Booking, Space } from './booking-api.service';
@Component({selector:'app-bookings',standalone:true,imports:[ReactiveFormsModule,DatePipe,ButtonModule],templateUrl:'./bookings.component.html'})
export class BookingsComponent implements OnInit {
 private readonly api=inject(BookingApi);private readonly fb=inject(FormBuilder);
 readonly spaces=signal<Space[]>([]);readonly bookings=signal<Booking[]>([]);
 readonly busy=signal(false);readonly loading=signal(true);readonly error=signal('');readonly message=signal('');
 readonly form=this.fb.nonNullable.group({spaceId:[1,Validators.required],startsAt:['',Validators.required],endsAt:['',Validators.required]});
 ngOnInit(){this.refresh();}
 refresh(){this.loading.set(true);this.error.set('');forkJoin({spaces:this.api.spaces(),bookings:this.api.own()}).pipe(finalize(()=>this.loading.set(false))).subscribe({next:r=>{this.spaces.set(r.spaces);this.bookings.set(r.bookings);},error:()=>this.error.set('No se pudo cargar la información. Puedes volver a intentar.')});}
 create(){if(this.form.invalid||this.busy())return;this.busy.set(true);this.error.set('');this.message.set('');const v=this.form.getRawValue();
 this.api.create({spaceId:v.spaceId,startsAt:this.offset(v.startsAt),endsAt:this.offset(v.endsAt)}).pipe(finalize(()=>this.busy.set(false))).subscribe({next:()=>{this.message.set('Reserva creada.');this.refresh();},error:(e:HttpErrorResponse)=>this.fail(e)});}
 cancel(id:number){if(this.busy())return;this.busy.set(true);this.error.set('');this.message.set('');this.api.cancel(id).pipe(finalize(()=>this.busy.set(false))).subscribe({next:()=>{this.message.set('Reserva cancelada. El horario vuelve a estar disponible.');this.refresh();},error:(e:HttpErrorResponse)=>this.fail(e)});}
 private offset(value:string){return value+(value.length===16?':00':'')+'-05:00';}
 private fail(error:HttpErrorResponse){this.error.set(error.status===409?'El espacio ya está reservado en ese horario.':error.status===400?'Revisa los campos: la fecha final debe ser posterior a la inicial.':error.status===401||error.status===403?'Acceso no válido. Cierra sesión y vuelve a entrar.':'No se pudo confirmar la operación. Actualiza la lista antes de reintentar.');}
}
