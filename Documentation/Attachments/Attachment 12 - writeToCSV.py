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
    
    @param movieList: List of Movie types (defined in imdb package)
    @param encodeTitles: If True, attempt to encode Unicode film titles with "utf-8"
"""
def moviesToDict(movieList, encodeTitles):
    dictList = []
    for movie in movieList:
        movieObj = IMDbMovie(imdbInfo.getMovieID(movie), ia)
        dictList.append(movieObj.convertToDict(encodeTitles))
    return dictList

"""
    Sort dictionary list in descending order of key value
"""
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
        
        # Write only complete rows to new file
        for row in reader: # Row given in form of a list of strings
            print row
            if isComplete(row, HEADERS) is True:
                writer.writerow(row)
        
        #Closer files
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
        for row in reader: # Row given in form of a list of strings
                
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
                
            # If something goes wrong and need to manually interrupt process
            except KeyboardInterrupt:
                print "ABORTING...."
                # Save current list of incomplete rows
                INCOMPLETE.append(row)
                inputFile.close()
                resultFile.close()
                saveUnresolvedToFile(incompleteFile)
                sys.exit(0)
            # If problem writing row for any reason
            except:
                # Mark as unsuccessful
                success = False
                writer.writerow({'Book': bookName, 'Movie': firstMovie})
        
            # If row is incomplete, add to INCOMPLETE list
            if success is False:
                print "\nUNRESOLVED ROW:", row
                INCOMPLETE.append(row)
            
    
    # Close files
    inputFile.close()
    resultFile.close()
    
    # Write list of problematic entries to a separate file for reference
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
        
        # Convert to dictionary type
        print "\nCONVERT TO DICTIONARY TYPES:\n-------------\n"
        moviesAsDict = moviesToDict(movieList, True)
        print "\n---MOVIES AS DICTS"
        for dict in moviesAsDict:
            print dict
        
        ## TODO: Sort by gross revenue instead? (Better indicator of success/completion)
        # Sort from highest to lowerst IMDb Rating
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
            bestMovieDict = moviesAsDict[0] or None
            print "HIGHEST RATED:", bestMovieDict
        
        # If no film available, mark this row as problematic
        if bestMovieDict == None:
            raise # Mark incomplete
        
        # Update new row and return it
        print "\nGETTING REMAINING INFO:\n-------------\n"
        newRow.update(bestMovieDict)
        
        return newRow
        
    # If need to manually interrupt process for any reason
    except KeyboardInterrupt:
                print "ABORTING...."
                raise
    except: # If failed for any reason
        # This row will be marked as unresolved
        print "Unexpected error:", sys.exc_info()[0]
        print "\nUNRESOLVED ROW:", row
        return newRow
    

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
    Count the number of filled values in each dict.
    Return true if dict1 has more filled elements than dict2, i.e. contains more keys from fields
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
    return dict1Count > dict2Count

##-------------##
##-- RUNNING --##
##-------------##

"""
    Main method for testing/running program
"""
if __name__ == "__main__":
    #readBTMIndex_writeOutput("../Data/BookToMovieIndexProofread.csv", "exampleOutput2B.csv", "incomplete2B.csv")
    filterIncomplete("../Data/MovieOutput/exampleOutput2Stripped.csv", "exOutFilter.csv")
    