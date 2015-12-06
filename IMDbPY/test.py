#!/usr/bin/env python

import imdb
ia = imdb.IMDb()
tm = ia.search_movie('Peter Pan 2013')
for item in tm:
  print item['title'], "(", item['year'], ")"