#!/usr/bin/env node
// Host entry point: Node >=18 and Docker Compose v2+, no host Java/npm install or pwsh.
import {spawnSync} from 'node:child_process';
import {randomBytes} from 'node:crypto';
import {existsSync, mkdirSync, readFileSync, writeFileSync, statSync} from 'node:fs';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const files=['--project-name','booking-local','-f','compose.yaml','-f','.local/transfer/compose.images.yaml'];
const oracle='gvenzl/oracle-free:23.26.3-slim@sha256:6d61d267a3b978c24c5ac1790e62e927416a0aec446bd86e4b3a1527562757bd';
function run(command,args,{capture=false,input}={}) {
  const r=spawnSync(command,args,{cwd:root,encoding:'utf8',input,stdio:capture||input!==undefined?['pipe','pipe','pipe']:'inherit'});
  if(r.error)throw new Error(`${command} unavailable: ${r.error.message}`);
  if(r.status!==0){const codes=(r.stdout??'').match(/(?:ORA|SP2)-[0-9]+/g)??[];throw new Error(`${command} failed (${r.status}). ${codes.join(', ')} ${capture?'Inspect Docker/tool availability.':''}`);}
  return r.stdout?.trim()??'';
}
const docker=(...args)=>run('docker',args);
const compose=(...args)=>docker('compose',...files,...args);
const dev=command=>compose('exec','-T','dev','bash','-c',command);
function engine(){
  const info=JSON.parse(run('docker',['info','--format','{{json .}}'],{capture:true}));
  if(info.OSType!=='linux'||!['amd64','x86_64'].includes(info.Architecture))throw new Error('Requires Linux Docker engine amd64/x86_64.');
}
function check(){
  engine();
  for(const f of ['.local/transfer/compose.images.yaml','.local/secrets/app-password','.local/secrets/oracle-password'])
    if(!existsSync(path.join(root,f))||statSync(path.join(root,f)).size===0)throw new Error(`Missing ${f}. Complete bootstrap or restore first; no secrets generated.`);
  compose('-f','.devcontainer/compose.restored.yaml','config','--quiet');
  const images=run('docker',['compose',...files,'config','--images'],{capture:true}).split(/\r?\n/);
  for(const name of images){
    const [image]=JSON.parse(run('docker',['image','inspect',name],{capture:true}));
    if(image.Os!=='linux'||image.Architecture!=='amd64')throw new Error('Imported/built images must be Linux amd64.');
  }
  console.log('Existing configuration and images valid; no credentials changed.');
}
function prepare(){
  engine();
  if(existsSync(path.join(root,'.local')))throw new Error('Existing .local: bootstrap refuses to replace this installation. Use check/start.');
  const volumes=run('docker',['volume','ls','--format','{{.Name}}'],{capture:true}).split(/\r?\n/);
  const containers=run('docker',['ps','-aq','--filter','label=com.docker.compose.project=booking-local'],{capture:true});
  if(volumes.some(v=>v.startsWith('booking-local_'))||containers)throw new Error('Existing booking-local project/volumes: no new credentials created.');
  if(process.platform==='linux'&&process.getuid()!==1000)throw new Error('New Linux checkout requires owner UID1000 (container node user); see README before provisioning.');
  for(const dir of ['.local/secrets','.local/runtime','.local/transfer'])mkdirSync(path.join(root,dir),{recursive:true});
  for(const name of ['oracle-password','app-password'])writeFileSync(path.join(root,'.local/secrets',name),'Bkg9'+randomBytes(24).toString('hex'),{flag:'wx',mode:0o600});
  if(!existsSync(path.join(root,'.env')))writeFileSync(path.join(root,'.env'),readFileSync(path.join(root,'.env.example')),{flag:'wx',mode:0o600});
  writeFileSync(path.join(root,'.local/transfer/compose.images.yaml'),`services:\n  dev:\n    image: booking-local/dev:checkout\n    pull_policy: never\n  oracle:\n    image: ${oracle}\n    pull_policy: never\n`,{flag:'wx'});
  writeFileSync(path.join(root,'.local/new-environment'), 'Prepared new empty environment; not a restored backup.\n',{flag:'wx'});
  console.log('New Oracle credentials generated privately. Next: images, up, schema, seed, verify.');
}
function images(){
  if(!existsSync(path.join(root,'.local/new-environment')))throw new Error('images is only for an explicitly prepared NEW environment, not restored images.');
  docker('build','--platform','linux/amd64','-f','.devcontainer/Dockerfile','-t','booking-local/dev:checkout','.');
  docker('pull','--platform','linux/amd64',oracle);
}
function up(){check();compose('up','-d','--no-build','--wait','--wait-timeout','600');}
function credentialsAndDependencies(){
  dev('test -w /workspace');
  compose('exec','-T','dev','node','--input-type=module','-e',String.raw`import fs from 'node:fs';import crypto from 'node:crypto';const file='.local/runtime/booking.properties';if(!fs.existsSync(file)){fs.mkdirSync('.local/runtime',{recursive:true});fs.writeFileSync(file,['booking.auth.first.username=ana','booking.auth.first.password='+crypto.randomBytes(24).toString('base64'),'booking.auth.second.username=bruno','booking.auth.second.password='+crypto.randomBytes(24).toString('base64'),''].join('\n'),{flag:'wx',mode:0o600});}`);
  dev('test -f frontend/node_modules/@angular/build/package.json || (cd frontend && npm ci --no-audit --no-fund)');
}
function waitFor(url){
  // Only fixed internal URLs; never prints the CSRF body or cookies.
  const code=`const end=Date.now()+120000;let ok=false;while(Date.now()<end){try{const r=await fetch('${url}',{signal:AbortSignal.timeout(5000)});if(r.ok){ok=true;break}}catch{}await new Promise(r=>setTimeout(r,1000));}if(!ok)process.exit(1);`;
  compose('exec','-T','dev','node','--input-type=module','-e',code);
}
function startProcesses(){
  dev('bash scripts/app-process.sh backend');waitFor('http://localhost:8080/api/csrf');
  dev('bash scripts/app-process.sh frontend');waitFor('http://localhost:4200/');
  console.log('Booking running. Default host URLs: http://localhost:4200/ and http://localhost:8080/swagger-ui/index.html (see .env for overrides).');
}
function database(action){
  check();
  const names={schema:'V001__booking_schema.sql',seed:'R__development_spaces.sql'};
  if(action==='schema'&&!existsSync(path.join(root,'.local/new-environment')))throw new Error('schema is only for a NEW environment, never a restored database.');
  const sql=action==='status'?"SELECT USER, SYS_CONTEXT('USERENV','CON_NAME') AS PDB FROM dual;\nSELECT COUNT(*) AS BOOKING_COUNT FROM BKG_BOOKING;":readFileSync(path.join(root,'backend/src/main/resources/db/oracle',names[action]),'utf8');
  const command=String.raw`set -euo pipefail; { printf 'WHENEVER OSERROR EXIT FAILURE\nWHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK\nSET ECHO OFF VERIFY OFF\nCONNECT BOOKING/"%s"@//localhost:1521/FREEPDB1\n' "$(cat /run/secrets/app_password)"; cat; } | sqlplus -s /nolog`;
  const output=run('docker',['compose',...files,'exec','-T','oracle','bash','-c',command],{input:sql+'\nCOMMIT;\nEXIT;\n'});
  if(action==='status')console.log(output);
  console.log(`${action} succeeded. DDL is not transactional: inspect any partial failure before retrying.`);
}
async function main(){
  const action=process.argv[2]??'help';
  switch(action){
    case 'prepare-new':prepare();break;
    case 'images':images();break;
    case 'check':check();break;
    case 'up':up();break;
    case 'schema':case 'seed':database(action);break;
    case 'db-status':database('status');break;
    case 'start':up();credentialsAndDependencies();dev('if ! bash scripts/backend-artifact.sh current; then bash scripts/app-process.sh stop-backend; bash scripts/backend-artifact.sh build; fi');startProcesses();break;
    case 'verify':up();credentialsAndDependencies();dev('bash scripts/app-process.sh stop && bash scripts/backend-artifact.sh verify');dev('cd frontend && npm run build && npm test');dev('bash scripts/verify-local.sh');startProcesses();break;
    case 'stop':{
      const running=run('docker',['compose',...files,'ps','--status','running','--services'],{capture:true}).split(/\r?\n/);
      if(running.includes('dev'))dev('bash scripts/app-process.sh stop');compose('stop');break;
    }
    case 'status':compose('ps');break;
    case 'logs':dev('tail -n 60 .local/runtime/backend.log .local/runtime/frontend.log');break;
    case 'help':console.log('node scripts/environment.mjs prepare-new|images|check|up|schema|seed|start|verify|stop|status|db-status|logs');break;
    default:throw new Error('Unknown action; use help.');
  }
}
main().catch(error=>{console.error(error.message);process.exitCode=1;});
