import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { timeout } from 'rxjs';
export interface Space {id:number;name:string;type:string;capacity:number;site:string;}
export interface Booking {id:number;spaceId:number;spaceName:string;startsAt:string;endsAt:string;status:string;}
export interface BookingRequest {spaceId:number;startsAt:string;endsAt:string;occurrences?:number;}
export interface BookingResult {created:Booking[];rejected:{startsAt:string;endsAt:string;reason:string}[];}
@Injectable({providedIn:'root'})
export class BookingApi {
 private readonly http=inject(HttpClient);
 // A timed-out write may have reached Oracle: never retry it automatically.
 spaces(){return this.http.get<Space[]>('/api/spaces').pipe(timeout(15000));}
 own(){return this.http.get<Booking[]>('/api/bookings').pipe(timeout(15000));}
 create(body:BookingRequest){return this.http.post<BookingResult>('/api/bookings',body).pipe(timeout(15000));}
 cancel(id:number){return this.http.delete<void>('/api/bookings/'+id).pipe(timeout(15000));}
}
