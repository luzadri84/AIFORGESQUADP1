import type { ValidatorFn } from '@angular/forms';

// datetime-local is entered in Bogotá, independently of the browser's timezone.
export const bookingRangeValidator: ValidatorFn = control => {
  const start = control.get('startsAt')?.value;
  const end = control.get('endsAt')?.value;
  if (!start || !end) return null; // Required validators own missing fields.
  const startsAt = Date.parse(start + '-05:00');
  const endsAt = Date.parse(end + '-05:00');
  return Number.isFinite(startsAt) && Number.isFinite(endsAt) && startsAt < endsAt
    ? null : { bookingRange: true };
};
