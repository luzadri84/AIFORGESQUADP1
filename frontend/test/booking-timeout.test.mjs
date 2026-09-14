import '@angular/compiler';
import test from 'node:test';
import assert from 'node:assert/strict';
import { Injector, runInInjectionContext } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NEVER, TimeoutError, defer, finalize, of, throwError } from 'rxjs';
import { TestScheduler } from 'rxjs/testing';
import { BookingApi } from '../dist/out-tsc/app/reservas/booking-api.service.js';

const request={spaceId:1,startsAt:'2026-09-17T17:08:00-05:00',endsAt:'2026-09-17T18:08:00-05:00'};
function apiWith(source){
 const http={get:()=>source,post:()=>source,delete:()=>source};
 return runInInjectionContext(Injector.create({providers:[{provide:HttpClient,useValue:http}]}),()=>new BookingApi());
}
for(const action of ['spaces','own','create','cancel'])test(action+' stops waiting at 15 seconds without retry',()=>{
 const scheduler=new TestScheduler(assert.deepEqual);
 scheduler.run(({flush})=>{
  let calls=0,finalized=false,error;
  const api=apiWith(defer(()=>{calls++;return NEVER;}));
  api[action](action==='create'?request:1).pipe(finalize(()=>{finalized=true;})).subscribe({error:e=>{error=e;}});
  flush();
  assert.ok(error instanceof TimeoutError);
  assert.equal(scheduler.frame,15000);
  assert.equal(calls,1);
  assert.equal(finalized,true,'the component finalizer must run after timeout');
 });
});
test('HTTP 400 and successful result are preserved and finalize',()=>{
 const failure=new HttpErrorResponse({status:400});
 let error,finalized=false;
 apiWith(throwError(()=>failure)).create(request).pipe(finalize(()=>{finalized=true;})).subscribe({error:e=>{error=e;}});
 assert.equal(error,failure);assert.equal(finalized,true);
 const expected={created:[],rejected:[]};let result;
 apiWith(of(expected)).create(request).subscribe(value=>{result=value;});
 assert.equal(result,expected);
});
