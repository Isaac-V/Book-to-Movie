#!/usr/bin/env python

import imdb
ia = imdb.IMDb()
tm = ia.search_movie('The Woman in White')
for item in tm:
  strTitle = str(item['title'])
  print "SEARCH RESULT:", strTitle
  
  if item.get('kind') != None:
    print "KIND:", item['kind']
  print ""
  #ia.update(item, 'akas')
  """try:
    alternateTitles = item['akas']
    altTitle = False
    for alt in alternateTitles:
        if alt == "Hopelessly Lost":
            print "Alternate title!"
            altTitle = True;
            break;
    if altTitle is False:
        print "No match"
  except KeyError:
    print "No alternate found"""