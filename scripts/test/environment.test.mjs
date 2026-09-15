import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
const entry=fileURLToPath(new URL('../environment.mjs',import.meta.url));
function fixture(t,volume=false){
 const root=fs.mkdtempSync(path.join(os.tmpdir(),'booking-doc-ops-'));
 fs.mkdirSync(path.join(root,'scripts'));fs.copyFileSync(entry,path.join(root,'scripts/environment.mjs'));
 fs.writeFileSync(path.join(root,'.env.example'),'LOCAL_HTTP_PORT=8080\nLOCAL_ANGULAR_PORT=4200\n');
 const fake=path.join(root,'docker-stub.cjs');
 fs.writeFileSync(fake,`const cp=require('node:child_process');const original=cp.spawnSync;cp.spawnSync=(c,args,o)=>{
 if(c!=='docker')return original(c,args,o);
 let stdout='';
 if(args[0]==='info')stdout=JSON.stringify({OSType:'linux',Architecture:'amd64'});
 if(args[0]==='volume')stdout=${JSON.stringify(volume?'booking-local_oracle-data':'')};
 if(args.includes('--images'))stdout='booking-test/dev\\nbooking-test/oracle\\n';
 if(args[0]==='image')stdout=JSON.stringify([{Os:'linux',Architecture:'amd64'}]);
 if(args.includes('node')&&args.includes('-e')&&args.at(-1).includes("import fs"))return original(process.execPath,['--input-type=module','-e',args.at(-1)],{cwd:o.cwd,encoding:'utf8'});
 return {status:0,stdout,stderr:''};};require('node:module').syncBuiltinESMExports();`);
 const run=action=>spawnSync(process.execPath,['--require',fake,path.join(root,'scripts/environment.mjs'),action],{cwd:root,encoding:'utf8'});
 t.after(()=>{const resolved=fs.realpathSync(root);const parent=fs.realpathSync(os.tmpdir());assert.equal(path.dirname(resolved),parent);assert.ok(path.basename(resolved).startsWith('booking-doc-ops-'));fs.rmSync(resolved,{recursive:true});});
 return {root,run};
}
test('new bootstrap produces private separate credentials and refuses regeneration',t=>{
 const {root,run}=fixture(t);let r=run('prepare-new');assert.equal(r.status,0,r.stderr);
 const a=fs.readFileSync(path.join(root,'.local/secrets/app-password'),'utf8');
 const b=fs.readFileSync(path.join(root,'.local/secrets/oracle-password'),'utf8');
 assert.notEqual(a,b);assert.ok(a.length>=40);assert.ok(!r.stdout.includes(a));
 const overlay=fs.readFileSync(path.join(root,'.local/transfer/compose.images.yaml'),'utf8');
 assert.ok(overlay.startsWith('services:\n'));assert.ok(!overlay.includes('\\n'));
 r=run('prepare-new');assert.equal(r.status,1);assert.equal(fs.readFileSync(path.join(root,'.local/secrets/app-password'),'utf8'),a);
});
test('an existing Oracle volume blocks bootstrap before creating any local file',t=>{
 const {root,run}=fixture(t,true);const r=run('prepare-new');assert.equal(r.status,1);assert.match(r.stderr,/Existing booking-local/);assert.ok(!fs.existsSync(path.join(root,'.local')));
});
test('check and schema fail closed on an incomplete or restored installation',t=>{
 const {root,run}=fixture(t);assert.equal(run('check').status,1);
 assert.equal(run('prepare-new').status,0);assert.equal(run('check').status,0);
 fs.unlinkSync(path.join(root,'.local/new-environment'));
 const r=run('schema');assert.equal(r.status,1);assert.match(r.stderr,/never a restored database/);
});

test('first start creates valid Basic properties once without leaking or replacing them',t=>{
 const {root,run}=fixture(t);assert.equal(run('prepare-new').status,0);
 const first=run('start');assert.equal(first.status,0,first.stderr);
 const file=path.join(root,'.local/runtime/booking.properties');const value=fs.readFileSync(file,'utf8');
 const lines=value.trim().split('\n');assert.equal(lines.length,4);assert.match(lines[0],/^booking.auth.first.username=ana$/);
 const password=lines[1].split('=').slice(1).join('=');assert.ok(password.length>=32);assert.ok(!first.stdout.includes(password));
 assert.equal(run('start').status,0);assert.equal(fs.readFileSync(file,'utf8'),value);
});
