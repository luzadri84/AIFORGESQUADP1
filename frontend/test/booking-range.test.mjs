import '@angular/compiler';
import test from 'node:test';
import assert from 'node:assert/strict';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { bookingRangeValidator } from '../src/app/reservas/booking-range.validator.ts';

for (const [name,start,end,valid] of [
 ['same minute (reported case)','2026-09-17T17:08','2026-09-17T17:08',false],
 ['end before start','2026-09-17T17:08','2026-09-17T16:08',false],
 ['one minute later','2026-09-17T17:08','2026-09-17T17:09',true],
 ['across midnight','2026-09-17T23:59','2026-09-18T00:01',true],
 ['missing start','','2026-09-17T18:08',false],
 ['missing end','2026-09-17T17:08','',false],
 ['malformed value','not-a-date','2026-09-17T18:08',false]
]) test(name,()=>{
 const form=new FormGroup({startsAt:new FormControl(start,Validators.required),endsAt:new FormControl(end,Validators.required)},{validators:bookingRangeValidator});
 assert.equal(form.valid,valid);
 form.controls.startsAt.setValue('2026-09-17T17:08');
 form.controls.endsAt.setValue('2026-09-17T18:08');
 assert.equal(form.valid,true,'correcting the dates must release the form');
});
