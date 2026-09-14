const fs=require('node:fs');
const semver=require('/workspace/frontend/node_modules/semver');
for(const line of fs.readFileSync(process.argv[2],'utf8').trim().split('\n')){
 const [a,b]=line.split('\t');
 console.log(semver.compare(a,b));
}
