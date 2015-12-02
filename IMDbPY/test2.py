import csv
from imdb import IMDb

ia = IMDb('http')
movie = ia.get_movie('0109830')
movieXML = movie.asXML()

print movieXML
    