"""Offline OSV comparison. Requires complete public catalogs filtered locally.
No network calls. Matches Maven ComparableVersion and npm semver intervals;
reachability/applicability must still be reviewed manually.
"""
import json,subprocess
from pathlib import Path
p=Path('.local/audit')
data=json.loads((p/'catalog-matches.json').read_text(encoding='utf-8'))
coords={e:json.loads((p/(('maven' if e=='Maven' else 'npm')+'-coordinates.json')).read_text(encoding='utf-8')) for e in ['Maven','npm']}
pairs={e:set() for e in coords}
for r in data['records']:
 for c in coords[r['ecosystem']]:
  if c['name']!=r['affected']['package']['name']:continue
  for rg in r['affected'].get('ranges',[]):
   for ev in rg.get('events',[]):
    for boundary in ev.values():
     if boundary!='0':pairs[r['ecosystem']].add((c['version'],boundary))
cmp={}
base=['docker','compose','-f','compose.yaml','-f','.local/transfer/compose.images.yaml','exec','-T','dev','bash','-c']
for eco in pairs:
 ordered=sorted(pairs[eco]);file=('maven' if eco=='Maven' else 'npm')+'-pairs.tsv'
 (p/file).write_text(''.join(a+'\t'+b+'\n' for a,b in ordered),encoding='utf-8')
 command=('java -cp /home/node/.m2/wrapper/dists/apache-maven-3.9.9/*/lib/maven-artifact-3.9.9.jar scripts/audit/CompareVersions.java' if eco=='Maven' else 'node scripts/audit/compare-semver.cjs')+' .local/audit/'+file
 output=subprocess.check_output(base+[command],text=True)
 values=[int(x) for x in output.splitlines()];assert len(values)==len(ordered)
 cmp[eco]=dict(zip(ordered,values))
from range_match import is_affected
hits={}
for r in data['records']:
 for c in coords[r['ecosystem']]:
  if c['name']==r['affected']['package']['name'] and is_affected(r['affected'],c['version'],cmp[r['ecosystem']]):
   d=data['details'][r['id']];key=(r['ecosystem'],c['name'],c['version'],r['id'])
   hits[key]={'ecosystem':r['ecosystem'],**c,'id':r['id'],'aliases':d.get('aliases',[]),'summary':d.get('summary',''),'severity':d.get('database_specific',{}).get('severity'),'ranges':r['affected'].get('ranges',[]),'references':d.get('references',[])}
result={'stats':data['stats'],'hits':list(hits.values()),'limits':'Version-range matches, not exploitability. No outbound package queries. Public catalog records may be incomplete.'}
(p/'dependency-findings.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print('Matches',len(hits))
for h in hits.values():print(h['ecosystem'],h['name'],h['version'],h['scope'],h['id'],h['aliases'],h['severity'],h['summary'])
