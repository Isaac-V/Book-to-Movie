#!/usr/bin/env python

import imdb
ia = imdb.IMDb()
#avatar = ia.get_movie("0109830")
#ia.update(avatar, 'business') # get the business information

#business = avatar.get('business') or {} # may be empty
#gross_list = business.get('gross') # to see what else is available: print
#for item in gross_list:
#  if 'worldwide' in item or 'Worldwide' in item:
#    print item
#budget = business.get('budget')
#for item in budget:
#  print item
#ia.update(avatar, 'awards')
#print avatar.get('awards')
tm = ia.search_movie('Alien Abduction (2012) (V)') # The Matrix
for item in tm:
  print item['title'], item['year']
#ia.update(tm, 'awards')
#print tm['awards']