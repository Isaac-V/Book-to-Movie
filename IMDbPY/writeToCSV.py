import csv, codecs, cStringIO
from getBoxOffice import IMDbMovie
import getBoxOffice
import re # Regular Expression
import imdb # IMDBpy package

##-----------##
##  GLOBALS  ##
##-----------##

""" FIELDS_TO_KEEP specifies table headers from original file that will be preserved in output file.
    All other headers in file will be ignored.
"""
FIELDS_TO_KEEP = ['const', 'Title', 'IMDb Rating', 'Year']

""" FIELDS_TO_ADD specifies new table headers that will be added to the output file.
    These define new columns of data that will be filled.
"""
FIELDS_TO_ADD = getBoxOffice.KEY_NAMES

""" Specify file names of input and output files (to easily change) """
IN_FILE_NAME = 'SmallTestList.csv'
OUT_FILE_NAME = 'SmallOutputList.csv'

imdbConnect = imdb.IMDb()

##----------------------------------------------------------------------

##-------------##
##  FUNCTIONS  ##
##-------------##

def connectToIMDb():
    imdbConnect = imdb.IMDb()

""" Takes CSV files exported from IMDb list and modifies to only include desired columns.
    Removes unwanted columns (anything not in FIELDS_TO_KEEP). Adds and populates new
    columns as specified in FIELDS_TO_ADD.
"""
def readWriteCSVasDict():
    with open(IN_FILE_NAME, 'rb') as inputFile, open(OUT_FILE_NAME, 'wb') as resultFile:
        reader = csv.DictReader(inputFile)
        # Define writer. Headers specified by joined list of FIELDS_TO_KEEP and FIELDS_TO_ADD
        # Any column whose key is not in fields parameter is excluded from the output file
        writer = csv.DictWriter(resultFile, FIELDS_TO_KEEP + FIELDS_TO_ADD, extrasaction="ignore")
        
        # Write keys as headers
        writer.writeheader()
        # Iterate through each row to add new columns
        for row in reader:
            # Define new row
            new_row = row
            
            ##TODO: Filter out rows with incomplete data
            # Fill box office information
            updateBoxOffice(new_row)
            
            ## TODO: Add nominations column
            movieURL = new_row.get("URL")
            print "Movie's URL:", movieURL
            
            # If data complete, write modified row to output file
            # Otherwise, ignore
            if isComplete(new_row, FIELDS_TO_ADD):
                writer.writerow(new_row)
            print "\n----------\n"

    # Close files
    inputFile.close()
    resultFile.close()
    
"""
    Update row to include box office information.
    Return true if successfully updated, or false if row not updated
    (e.g. due to incomplete data)
"""
def updateBoxOffice(row):
    # Get movie's IMDb id from file
    movieID = re.sub('\D', '', row['const'])
    print 'Movie ID is:', movieID
    
    movieObject = IMDbMovie(movieID, imdbConnect)
    
    # Retrieve box office info from IMDbPY API
    #boxOffice = getBoxOffice.get_as_dict(movieID)
    boxOffice = movieObject.get_as_dict()
    print boxOffice
    
    # Update row and return true
    row.update(boxOffice)
    return True

"""
    Check if row contains values for all specified keys, and if all key-values are non-empty.
    Return false if data is unavailable or incomplete. Return true if all key-value pairs are filled.
"""
def isComplete(row, fields):
    for key in fields:
        value = row.get(key)
        if value == None or value == "":
            return False
    return True
    
### --------------------------------------------------------------

### TODO: Create Binary Output file.

### --------------------------------------------------------------

###---------------------------------------------------------------
### UNUSED CODE (KEPT FOR SAFETY)
###---------------------------------------------------------------

"""
    CURRENTLY NOT USED
    Read and write output file as a list rather than a dict object
"""    
"""def writeCSVasList():
    with open(IN_FILE_NAME, 'rb') as inputFile, open(OUT_FILE_NAME, 'wb') as resultFile:
        reader = csv.reader(inputFile)
        # Define writer. Headers specified by joined list of FIELDS_TO_KEEP and FIELDS_TO_ADD
        writer = csv.writer(resultFile)
        
        for row in reader:
            new_row = row

            if rownum == 0:
                # Keep track of headers
                header = row
                
                # Get desired columns
                new_row.extend(getBoxOffice.KEY_NAMES)
            else:
                new_row = row
                movieID = re.sub('\D', '', new_row[header.index('const')])
                print 'Movie ID is:', movieID
                boxOffice = getBoxOffice.get_as_list(movieID)
                new_row.extend(boxOffice)
                writer.writerow(new_row)
                rownum += 1

    # Close files
    inputFile.close()
    resultFile.close()
"""

if __name__ == "__main__":
    readWriteCSVasDict()