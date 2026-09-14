"""Build local inventories and filter complete OSV catalogs; never sends dependency names.
Run from repository root after dependency:tree and npm ls. Downloads only public
whole-ecosystem catalogs when --download is supplied; cached catalogs otherwise.
"""
import argparse,hashlib,json,re,urllib.request,zipfile
from pathlib import Path
p=Path('.local/audit');p.mkdir(parents=True,exist_ok=True)
args=argparse.ArgumentParser();args.add_argument('--download',action='store_true');opts=args.parse_args()
meta=[]
for eco in ['Maven','npm']:
 file=p/(eco+'-advisories.zip')
 if opts.download:
  urllib.request.urlretrieve('https://storage.googleapis.com/osv-vulnerabilities/'+eco+'/all.zip',file)
 meta.append({'ecosystem':eco,'bytes':file.stat().st_size,'sha256':hashlib.file_digest(file.open('rb'),'sha256').hexdigest()})
(p/'catalogs.json').write_text(json.dumps(meta,indent=2),encoding='utf-8')
coords=[]
for line in (p/'maven-tree.txt').read_text(encoding='utf-8').splitlines():
 m=re.search(r'([\w.-]+):([\w.-]+):jar:([^: ]+):(compile|runtime|provided|test)',line)
 if m:coords.append(dict(name=m[1]+':'+m[2],version=m[3],scope=m[4]))
coords=list({(c['name'],c['version'],c['scope']):c for c in coords}.values())
(p/'maven-coordinates.json').write_text(json.dumps(coords,indent=2),encoding='utf-8')
lock=json.loads(Path('frontend/package-lock.json').read_text(encoding='utf-8'))
npm=[dict(name=v.get('name',k.split('node_modules/')[-1]),version=v['version'],scope='dev' if v.get('dev') else 'production',path=k) for k,v in lock['packages'].items() if k and 'version' in v]
(p/'npm-coordinates.json').write_text(json.dumps(npm,indent=2),encoding='utf-8')
names={'Maven':{c['name'] for c in coords},'npm':{c['name'] for c in npm}}
records=[];details={};stats={}
for eco in names:
 count=0
 with zipfile.ZipFile(p/(eco+'-advisories.zip')) as archive:
  for file in archive.namelist():
   if not file.endswith('.json'):continue
   d=json.loads(archive.read(file));count+=1
   if d.get('withdrawn'):continue
   for a in d.get('affected',[]):
    pkg=a.get('package',{})
    if pkg.get('ecosystem')==eco and pkg.get('name') in names[eco]:
     records.append({'ecosystem':eco,'id':d['id'],'affected':a});details[d['id']]=d
 stats[eco]={'catalogRecords':count,'inventoryEntries':len(coords if eco=='Maven' else npm),'packageNames':len(names[eco])}
(p/'catalog-matches.json').write_text(json.dumps({'records':records,'details':details,'stats':stats}),encoding='utf-8')
print(json.dumps(stats,indent=2))
