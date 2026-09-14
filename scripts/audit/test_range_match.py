import unittest
from range_match import is_affected
class RangeTest(unittest.TestCase):
 def check(self,version,events):
  comparison={(version,b):(int(version)>int(b))-(int(version)<int(b)) for e in events for b in e.values() if b!='0'}
  return is_affected({'ranges':[{'type':'ECOSYSTEM','events':events}]},version,comparison)
 def test_union_and_exclusive_fixed(self):
  events=[{'introduced':'0'},{'fixed':'2'},{'introduced':'3'},{'fixed':'5'}]
  for version,expected in [('1',True),('2',False),('3',True),('4',True),('5',False)]:
   with self.subTest(version=version):self.assertEqual(self.check(version,events),expected)
 def test_last_affected_and_open_interval(self):
  self.assertTrue(self.check('2',[{'introduced':'1'},{'last_affected':'2'}]))
  self.assertFalse(self.check('3',[{'introduced':'1'},{'last_affected':'2'}]))
  self.assertTrue(self.check('4',[{'introduced':'3'}]))
 def test_limit_and_explicit_versions(self):
  self.assertFalse(self.check('2',[{'introduced':'0'},{'limit':'2'}]))
  self.assertTrue(is_affected({'versions':['special']},'special',{}))
if __name__=='__main__':unittest.main()
