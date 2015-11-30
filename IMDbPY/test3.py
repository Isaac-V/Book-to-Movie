import csv
fields = ['const', 'Title', 'IMDb Rating', 'Year', 'URL']
newFields = ['Budget', 'Cost']

data_list = []
def readwrite():
    with open("SmallTestList.csv") as infile, open("out.csv", "wb") as outfile:
        #           in Python 2, use open("out.csv", "wb") as outfile:
        r = csv.DictReader(infile)
        w = csv.DictWriter(outfile, fields + newFields, extrasaction="ignore")
        w.writeheader()
        rownum = 0
        for row in r:
            row.update({newFields[0]: '5', newFields[1]: '5'})
            w.writerow(row)
            rownum += 1

__readWrite()