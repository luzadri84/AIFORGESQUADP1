import test from 'node:test';
import assert from 'node:assert/strict';
import { isOwnApi } from '../src/app/acceso/api-path.ts';
test('credentials only reach relative application API', () => {
  for (const url of ['/api/me','/api/bookings','/api/bookings/12']) assert.equal(isOwnApi(url),true);
  for (const url of ['https://other.example/api/me','//other.example/api/me','http://localhost:8080/api/me','/api-foreign','data:text/plain,test','/assets/icon.svg']) assert.equal(isOwnApi(url),false,url);
});
