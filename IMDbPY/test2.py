from imdb import IMDb
ia = IMDb('http')
movie = ia.get_movie('0109830')
print movie.getAsXML('business')