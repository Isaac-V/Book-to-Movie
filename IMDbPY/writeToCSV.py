## CMPSCI 383 (Artificial Intelligence)
## Mary Moser (29154085), Isaac Vawter (28277700)

##-------------##
##-- IMPORTS --##
##-------------##

import csv, codecs, cStringIO
from imdbInfo import IMDbMovie
import imdbInfo
import re # Regular Expression
import imdb # IMDBpy package
import sys
from operator import itemgetter
import time

##---------------##
##--  GLOBALS  --##
##---------------##

""" FIELDS_TO_KEEP specifies table headers from original file that will be preserved in output file.
    All other headers in file will be ignored.
"""
FIELDS_TO_KEEP = ['const', 'Title', 'IMDb Rating', 'Year']

""" FIELDS_TO_ADD specifies new table headers that will be added to the output file.
    These define new columns of data that will be filled.
"""
FIELDS_TO_ADD = imdbInfo.KEY_NAMES

HEADERS = HEADERS = ['Book', 'Movie', 'IMDb ID', 'IMDb Rating'] + FIELDS_TO_ADD

""" Specify file names of input and output files (to easily change) """
IN_FILE_NAME = 'SmallTestList.csv'
OUT_FILE_NAME = 'SmallOutputList.csv'

""" Access to IMDb """
ia = imdbInfo.ia

""" List of incomplete rows. To be printed at end of run """
INCOMPLETE = []

##----------------------------------------------------------------------

##--------------##
##  CONNECTING  ##
##--------------##

"""
    Establish a connection to IMDb
"""
def connectToIMDb():
    #ia = imdb.IMDb()
    imdbInfo.connectToIMDb()
    
##----------------------------------------------------------------------

##---------------------##
##-- DATA STRUCTURES --##
##---------------------##
    
"""
    Returns a list of unique elements, removing any duplicates
    
    @param list: A list of elements
    @returns: A new list identical to original, except every unique element appears only once
"""
def delRepeats(list):
    setList = []
    for item in list:
        if item not in setList:
            setList.append(item)
    return setList

"""
    Converts list of titles to list of imdb movies
    
    @param titleList: list of strings to be used to search IMDb
    @param firstIfNone: If True, any movie titles that don't yield an exact match will instead return the first search result.
    If False, movie will be discarded from list (i.e. returned list may be smaller than original)
    @returns: List of imdb movies, with basic information
"""
def titlesToMovies(titleList, firstIfNone):
    movieList = []
    for title in titleList:
        # NOTE: By default, tries to match exact result.
        # Simply return empty string if not resolved
        movie = imdbInfo.getClosestMatch(title, firstIfNone) or ""
        if movie != "":
            movieList.append(movie)
    return movieList
    
"""
    Converts list of movies to dictionary type
"""
def moviesToDict(movieList, encodeTitles):
    dictList = []
    for movie in movieList:
        movieObj = IMDbMovie(imdbInfo.getMovieID(movie), ia)
        dictList.append(movieObj.convertToDict(encodeTitles))
    return dictList

""" Sort dictionary list in descending order of key value """
def descendSortByKey(dictList, key):
    newList = sorted(dictList, key=itemgetter(key), reverse=True)
    print "\n====MOVIES AS DICT (sorted)\n"
    for dict in dictList:
        print dict
    return newList

##----------------------------------------------------------------------

##------------##
## READ/WRITE ##
##------------##    

"""
    Reads in a CSV of movie data. Writes only entries with complete data to the output file.
"""
def filterIncomplete(inFile, outFile):
    with open(inFile, 'rb') as inputFile, open(outFile, 'wb') as resultFile:
        # Read input file, delimiting by semicolns
        reader = csv.DictReader(inputFile, delimiter=';', fieldnames=HEADERS)
        
        # Define writer with desired headers
        writer = csv.DictWriter(resultFile, delimiter=";", fieldnames=HEADERS, extrasaction="ignore")
        
        #writer.writeheader()
        
        
        for row in reader: # Row given in form of a list of strings
            print row
            if isComplete(row, HEADERS) is True:
                writer.writerow(row)
        
        inputFile.close()
        resultFile.close()

"""
    Read raw book-to-movie index file, write to new output CSV
"""
def readBTMIndex_writeOutput(inFile, outFile, incompleteFile):

    #connectToIMDb()
    
    with open(inFile, 'rb') as inputFile, open(outFile, 'wb') as resultFile:
        # Read input file, delimiting by semicolns
        reader = csv.reader(inputFile, delimiter=';')
        
        # Define writer with desired headers
        writer = csv.DictWriter(resultFile, delimiter=";", fieldnames=HEADERS, extrasaction="ignore")
        
        writer.writeheader()
        
        # Grab book and movie mappings from each row
        ##i = 0 #if you need to limit number of iterations
        for row in reader: # Row given in form of a list of strings
            """ If you need to limit number of iterations
            i += 1
            if i > 230:
                break
            """
                
            # Keep book name and first movie
            bookName = row[0]
            firstMovie = row[1]
            print "\n\n----------------------\n-------------NEW ROW:", bookName, "Movie", firstMovie, "\n----------------------\n"
            
            # Keep track of whether writing information was successfully
            success = True
            newRow = {'Book': bookName}
            # Write a new row to the output file
            try:
                # Construct a row with desired values
                newRow = buildNewRow(row, newRow)
                
                # If row is incomplete, mark this row as incomplete
                if isComplete(newRow, HEADERS) is False:
                    # Mark row as incomplete
                    success = False
                    
                    #Add some identifying title
                    if newRow.get('Movie') == None or newRow.get('Movie') == "":
                        newRow.update({'Movie': firstMovie})
                
                # Write as much of new row as possible
                writer.writerow(newRow)
                
            except KeyboardInterrupt:
                print "ABORTING...."
                INCOMPLETE.append(row)
                inputFile.close()
                resultFile.close()
                saveUnresolvedToFile(incompleteFile)
                sys.exit(0)
            except:
                # Mark as unsuccessful
                success = False
                writer.writerow({'Book': bookName, 'Movie': firstMovie})
        
            # If row is incomplete, add to list
            if success is False:
                print "\nUNRESOLVED ROW:", row
                INCOMPLETE.append(row)
            
    
    # Close files
    inputFile.close()
    resultFile.close()
    
    
    saveUnresolvedToFile(incompleteFile)

"""
    Writes the list of problematic entries to a separate file for reference. Helpful for proofreading.
"""
def saveUnresolvedToFile(fileName):
    # Alert user of all unresolved rows
    if len(INCOMPLETE) > 0:
        print "\n\n===================\nALL INCOMPLETE ROWS:\n"
        for unsolved in INCOMPLETE:
            print unsolved
    
    # Save to a file
    with open(fileName, 'wb') as f:
        # Write incomplete to new file
        errorWriter = csv.writer(f)
        errorWriter.writerows(INCOMPLETE)
        

"""
    Builds a dictionary to write as a new row.
    If incomplete, add to incomplete list and notify user at end of run
"""
def buildNewRow(row, newRow):
    
    try:
        # Remove first element (book title) and empty strings
        mvTitleList = row
        mvTitleList.pop(0)
        mvTitleList = delRepeats(filter(None, mvTitleList))
        
        print "Unique Movie List", mvTitleList
        
        # Find matching list of movies
        print "\nSEARCHING TITLES:\n-------------\n"
        movieList = titlesToMovies(mvTitleList, True) # Change to True?
        
        print "\nList of Movies:\n-------------"
        for item in movieList:
            print item
        
        # If couldn't find movie in list, mark incomplete
        if movieList == []:
            ## TODO: Return first result instead?
            raise # Alert user that row is incomplete
        
        ## TODO: Convert to list of dictionary items
        print "\nCONVERT TO DICTIONARY TYPES:\n-------------\n"
        moviesAsDict = moviesToDict(movieList, True)
        print "\n---MOVIES AS DICTS"
        for dict in moviesAsDict:
            print dict
        
        ## TODO: Sort by gross revenue instead? (Better indicator of success/completion)
        # Sort
        moviesAsDict = descendSortByKey(moviesAsDict, 'IMDb Rating')
        
        # Search for most complete. Otherwise, grab most popular (first)
        print "\nGET BEST AVAILABLE (MOST COMPLETE):\n-------------\n"
        bestMovieDict = {}
        for dictItem in moviesAsDict:
            if isComplete(dictItem, HEADERS):
                bestMovieDict
                print "COMPLETE INFO:", bestMovieDict
                break
            elif isMoreComplete(dictItem, bestMovieDict, HEADERS):
                bestMovieDict = dictItem
                print "BEST SO FAR:", bestMovieDict
                break
        
        #If no complete movie, just get the most popular
        if bestMovieDict == None or bool(bestMovieDict) is False:
            print "\nGETTING HIGHEST RATED:\n-------------\n"
            #bestMovie = imdbInfo.getHighestRated(movieList)
            bestMovieDict = moviesAsDict[0] or None
            print "HIGHEST RATED:", bestMovieDict
        
        
        #print "HIGHEST RATED:", bestMovie
        
        
        if bestMovieDict == None:
        #if bestMovie == None:
            raise # Mark incomplete
        
        print "\nGETTING REMAINING INFO:\n-------------\n"
        newRow.update(bestMovieDict)
        """# Make IMDbMovie object
        movieObj = IMDbMovie(ia.get_imdbID(bestMovie), ia)
        
        
        ## TODO: Encode titles, or get English title?
        newRow.update({'Movie': movieObj.getTitle().encode('utf-8'), 'IMDb ID': "tt" + movieObj.getID(), 'IMDb Rating': movieObj.getRating()})
        newRow.update(movieObj.getBoxOfficeAsDict())"""
        
        return newRow
    except KeyboardInterrupt:
                print "ABORTING...."
                raise
    except: # If failed for any reason
        print "Unexpected error:", sys.exc_info()[0]
        print "\nUNRESOLVED ROW:", row
        return newRow
    


"""
    UNUSED: ORIGINAL APPROACH TO DATA GATHERING
    Takes CSV files exported from IMDb list and modifies to only include desired columns.
    Removes unwanted columns (anything not in FIELDS_TO_KEEP). Adds and populates new
    columns as specified in FIELDS_TO_ADD.
"""
def readWatchlist_writeCSVasDict():
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
    
    movieObject = IMDbMovie(movieID, ia)
    
    # Retrieve box office info from IMDbPY API
    #boxOffice = imdbInfo.getBoxOfficeAsDict(movieID)
    boxOffice = movieObject.getBoxOfficeAsDict()
    print boxOffice
    
    # Update row and return true
    row.update(boxOffice)
    return True

##---------------------##
##-- VALIDATING DATA --##
##---------------------##

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

"""
    Count the number of filled values in each dict. Return true if dict1 has more filled elements than dict2
"""    
def isMoreComplete(dict1, dict2, fields):
    dict1Count = 0
    dict2Count = 0
    for key in fields:
        # Count values in first dict
        value1 = dict1.get(key)
        if value1 != None and value1 != '':
            dict1Count += 1
        # Count values in second dict
        value2 = dict2.get(key)
        if value2 != None and value2 != '':
            dict2Count += 1
    #print "Count for 1:", dict1Count
    #print "Count for 2:", dict2Count
    return dict1Count > dict2Count


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
                new_row.extend(imdbInfo.KEY_NAMES)
            else:
                new_row = row
                movieID = re.sub('\D', '', new_row[header.index('const')])
                print 'Movie ID is:', movieID
                boxOffice = imdbInfo.get_as_list(movieID)
                new_row.extend(boxOffice)
                writer.writerow(new_row)
                rownum += 1

    # Close files
    inputFile.close()
    resultFile.close()
"""

"""
    Main method for testing/running program
"""
if __name__ == "__main__":
    #readBTMIndex_writeOutput("../Data/BookToMovieIndexProofread.csv", "exampleOutput2B.csv", "incomplete2B.csv")
    filterIncomplete("../Data/MovieOutput/exampleOutput2Stripped.csv", "exOutFilter.csv")
    