"""Match the union of OSV intervals with an ecosystem-specific comparator map."""
def is_affected(a,v,compare):
 if v in a.get('versions',[]):return True
 for rg in a.get('ranges',[]):
  lower=None
  for ev in rg.get('events',[]):
   if 'introduced' in ev: lower=ev['introduced']
   for end,inclusive in [('fixed',False),('last_affected',True),('limit',False)]:
    if end in ev:
     above=lower is not None and (lower=='0' or compare[(v,lower)]>=0)
     below=compare[(v,ev[end])] <= (0 if inclusive else -1)
     if above and below:return True
     lower=None
  if lower is not None and (lower=='0' or compare[(v,lower)]>=0):return True
 return False
