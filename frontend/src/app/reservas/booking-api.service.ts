import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
export interface Space {id:number;name:string;type:string;capacity:number;site:string;}
export interface Booking {id:number;spaceId:number;spaceName:string;startsAt:string;endsAt:string;status:string;}
export interface BookingRequest {spaceId:number;startsAt:string;endsAt:string;}
@Injectable({providedIn:'root'})
export class BookingApi {
 private readonly http=inject(HttpClient);
 spaces(){return this.http.get<Space[]>('/api/spaces');}
 own(){return this.http.get<Booking[]>('/api/bookings');}
 create(body:BookingRequest){return this.http.post<Booking>('/api/bookings',body);}
 cancel(id:number){return this.http.delete<void>('/api/bookings/'+id);}
}
