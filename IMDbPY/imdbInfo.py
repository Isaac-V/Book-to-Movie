#!/usr/bin/env python

##-------------##
##-- IMPORTS --##
##-------------##

import imdb # IMDBpy package
import re # Regular Expression
import csv, codecs, cStringIO # For working with CSV files

##-----------------##
##-- GLOBAL VARS --##
##-----------------##

KEY_NAMES = ['Gross Revenue', 'Budget', 'Net Profit']

# Strings to build regular expressions for filtering lists
## TODO: Use named expressions?
RES_NUM_FORMAT = '(?P<amount>([1-9][0-9,]+))' # Numbers with punctuation
RES_CURRENCY = '\$' # Defines accepted currency
RES_COUNTRY = '\((?P<country>([wW]orldwide|(Non-)?USA))\)' # Defines accepted countries

RES_YEAR = '\([1][0-9]{3}\)'

# Names of acceptable kinds
## TODO: List preferred types?
OK_KINDS = ['movie', 'tv series', 'tv movie', 'tv mini series', 'short', 'tv short']

imdbConnect = None

##--------------------------------------------------------------------------------------

##----------------------------------------------------------------------##
##-- IMDbMovie Class: Specifies individual movie with imdb connection --##
##----------------------------------------------------------------------##

## TODO: Globalize functions (rather than put inside class)?
# For individual movies
class IMDbMovie:
    """
        Initialize a new IMDbMovie object
    """
    def __init__(self, id, connection):
        self.movieID = id
        
        try:
            self.ia = connection or imdb.IMDb()
            self.movie = self.ia.get_movie(self.movieID)
            print 'Got movie:', self.movie.get('title')
        except:
            print "Sorry, lost internet!"
            raise
            
    """
        Returns true if value is empty
    """
    def is_not_available(self, value):
        # N/A represented as empty string
        return value == ''
        
    """
        Return box office information as a dictionary
    """
    def getBoxOfficeAsDict(self):
        # Get values in list form
        list = self.get_as_list()
        
        # Create dictionary with KEY_NAMES as keys and values at corresponding indices are values.
        # Assumes returned list gives box office info in same order as KEY_NAMES list
        dict = {}
        for i in range(0, len(KEY_NAMES)):
            dict.update({KEY_NAMES[i]: list[i]})
        print dict
        return dict
    
    """
        Return box office information as a list
    """
    def get_as_list(self):
        # Get current movie
        movie = self.movie
        
        # Grab list of business information from IMDb
        self.ia.update(movie, 'business') # get the business information
        business = movie.get('business') or {} # may be empty
        
        # If no business information, return list of empty elements
        if bool(business) is False:
            print 'Box office info N/A'
            return ['','','']
        
        # Get gross revenue
        grossint = self.__get_gross_int(business)
        
        # Get estimated budget
        budgetint = self.__get_budget_int(business)
        
        # Calculate net profit
        if isinstance(grossint, int) and isinstance(budgetint, int):
            netint = grossint - budgetint
            print "NET PROFIT:", netint
        else:
            # If here, at least one of values (budget or gross) are N/A,
            # or otherwise not parsed to an integer.
            # Cannot calculate on non-integers, so return empty string
            print "Net profit info N/A"
            netint = ''
        
        # Return list of all values
        return [grossint, budgetint, netint]
    
    """
        From list of business information, grabs 'gross', a list of information about
        the movie's gross revenue in different countries, etc.
        Filters list of gross information according to defined regular expression,
        and returns as an integer the maximum monetary value from the filtered list.
    """
    def __get_gross_int(self, business):
        gross_list = business.get('gross') or {} # to see what else is available: print
        
        # If no gross information found, return empty string
        if bool(gross_list) is False: # No gross info
            print 'Gross info N/A'
            return ''
        
        # Define regular expression for searching gross list.
        # CURRENT PATTERN: Match all instances with dollar ('$') as currency, where the country
        # is either "worldwide" or "USA". Exclude any instances that are re-releases.
        # Return only the number (including commas)
        searchPattern = re.compile(RES_CURRENCY + RES_NUM_FORMAT + '(?=\s' + RES_COUNTRY + ')(?!.*\(re-release\))')
        
        # Using regular expression defined above, find all matches in gross list.
        # Return a list of integers representing the monetary value of gross record
        dollarList = self.__filter_and_convert(gross_list, searchPattern)
        print "Matches for US Dollars:", dollarList
        
        if dollarList == []:
            print "No gross info matches criteria"
            return ''
        else:
            return max(dollarList)
    
    """
        Searches list for instances matching regular expression regex.
        If instance is a match, convert to appropriate form and append to a new list.
        Return resulting list.
    """
    def __filter_and_convert(self, list, regex):
        # List of (converted) matching results
        matchResults = []
        
        # Search list for instances that match regular expression
        for item in list:
            match = re.match(regex, item)
            if match: # Found a match
                print 'PATTERN MATCHED:', item
                # Convert to appropriate form before appending to list.
                grossInt = self.__convert_money_to_int(match)
                
                # Append converted results to result list
                matchResults.append(grossInt)
        
        # Return list of results
        return matchResults
    
    """
        Searches list of business information for budget information.
        Return first instance as an integer.
    """
    def __get_budget_int(self, business):
        # Grab budget
        budget_list = business.get('budget')
        
        #If no budget information returned, replace with empty string
        if bool(budget_list) is False:
            print "BUDGET INFO N/A"
            return ''
        
        # Grab first (and presumably only) item in budget
        match = re.match(r'' + RES_CURRENCY + RES_NUM_FORMAT, budget_list[0])
        
        # Convert to int and return
        return self.__convert_money_to_int(match)
    
    """
        Presumes that argument match is in the form:
            RES_CURRENCY + RES_NUM_FORMAT (e.g. '$755,888')
        Grabs the numerical part of the string, i.e. the actual monetary value
        without currency symbols or commas. Returns as an integer.
    """
    def __convert_money_to_int(self, match):
        # Get string version of numbers, ignoring currency and commas
        moneyStr = match.group('amount').replace(',','')
        # Return as integer
        return int(moneyStr)

    #--- end class

##---------------------------------------------------------------------

##----------------------##
##-- GLOBAL FUNCTIONS --##
##----------------------##

"""
    Search titles by search term. Return a movie whose title matches exactly, or an empty string if no match.
    If search term includes parenthetical year, match only if search result's title and year match exactly.
    
    This method also takes into account titles with ampersands ('&') in place of 'and'.
"""
def getExactMatch(searchTerm):
    print "Searching for: ", searchTerm
    searchResults = imdbConnect.search_movie(searchTerm)
    
    # Number of results
    print "Number of results:", len(searchResults)
    
    # Search for exact match
    for item in searchResults:
        
        # If unacceptable type, ignore
        if acceptableKind(item) is True:
            if isExactMatch(item, searchTerm):
                return item
            else: # No alternative titles exist for this movie
                print "No alternate titles for this movie; try again"
            print "\n"
        
    # No matching results; return empty string
    print "Out of luck; looks like that film isn't in IMDb"
    return None


"""
    Returns true if movie is an exact match of the search term
"""
def isExactMatch(movie, searchTerm):
    # Grab title
    movieTitle = movie['title']
    print "TITLE", movieTitle
    
    ## TODO: Better handle year
    ## Suggestions: Check if it exists. Separate methods for each (year and non-year)?
    # Grab year (if it exists)
    movieYear = movie.get('year') or ""
    
    print "YEAR:", movieYear
    
    # If search term has year at the end, match exact year
    if hasYearAtEnd(searchTerm):
        titleToMatch = movieTitle + " (" + str(movieYear) + ")"
    else: # Match by title only
        titleToMatch = movieTitle
    
    # Check if matches name or name with year
    print "Match against:", titleToMatch
    if titlesMatch(titleToMatch, searchTerm):
        print "Found match:", movie.summary()
        return True
    
    # If doesn't match exactly, check alternative titles
    
    ##-- BUG: If search term has year, will not recognize alt titles exists
    ##-- (Nature of imdb search results)
    ##--- Can use update, but problems with unicode parsing
    # Get list of alternative titles
    #imdbConnect.update(movie)
    altTitleList = movie.get('akas') or [] # May be None
    
    print altTitleList
    print "Alt titles?", altTitleList != []
    # Compare all alternate titles to search term
    for alt in altTitleList:
        print "ALT TITLE:", alt
        #If search term has year at the end, match exact year
        if hasYearAtEnd(searchTerm):
            altToMatch = alt + " (" + str(movieYear) + ")"
        else: # Match by title only
            altToMatch = alt
        
        print "Match against:", altToMatch
        # Check if matches title, or title + year
        if titlesMatch(altToMatch, searchTerm):
            # This movie's alternative title matches the search term.
            print "Matching Alternate title!"
            # Return this movie.
            return True
    
    # No actual or alternative title matches exactly
    return False
    
"""def isExactMatch(movie, searchTerm):
    # Grab title
    movieTitle = movie['title']
    print "TITLE", movieTitle
    
    ## TODO: Better handle year
    ## Suggestions: Check if it exists. Separate methods for each (year and non-year)?
    # Grab year (if it exists)
    if movie.get('year') != None:
        movieYear = movie.get('year')
        movieTitleWithYear = movieTitle + " (" + str(movieYear) + ")"
    else:
        print "No year available"
        movieYear = ""
        movieTitleWithYear = movieTitle
    
    print "YEAR:", movieYear
    
    if hasYearAtEnd(searchTerm):
        titleToMatch = movieTitle + " (" + str(movieYear) + ")"
    else:
        titleToMatch = movieTitle
    
    # Check if matches name or name with year
    #if titlesMatch(movieTitle, searchTerm) or titlesMatch(movieTitleWithYear, searchTerm):
    if titlesMatch(titleToMatch, searchTerm):
        print "Found match:", movie.summary()
        return True
    
    # If doesn't match exactly, check alternative titles
    if movie.get('akas') != None: # True is movie has alternative titles
        # Get list of alternative titles
        altTitleList = movie.get('akas') or []
        
        # Compare all alternate titles to search term
        for alt in altTitleList:
            # Create variations of title
            altWithYear = alt + " (" + str(movieYear) + ")"
            
            print "ALT TITLE:", altWithYear
            
            # Check if matches title, or title + year
            if titlesMatch(alt, searchTerm) or titlesMatch(altWithYear, searchTerm):
                # This movie's alternative title matches the search term.
                # Return this movie.
                print "Alternate title! Actual name is", movieTitle
                #break;
                return True
    
    return False
"""
"""
    Gets closest title match from search result, rather than returning empty string
"""
def getClosestMatch(searchTerm):
    exactMatch = getExactMatch(searchTerm)
    # If found an exact match, return it
    if exactMatch != None:
        return exactMatch
    
    # If couldn't find exact match, it could be that the year is slightly off
    if hasYearAtEnd(searchTerm) is False:
        # Year was already excluded; no need to search same term again
        
        # Simply repeat search and grab first item, or empty string
        searchResults = imdbConnect.search_movie(searchTerm)
        if len(searchResults) > 0:
            return searchResults[0]
        else:
            return None
    else:
        # Remove year from search term
        searchSansYear = stripYearAtEnd(searchTerm)
        print "Try searching without year:", searchSansYear
        
        # Search without year
        exactMatch = getExactMatch(searchSansYear)
        
        # If exact match was found without year, return it
        if exactMatch != None:
            print "Found a match by title only"
            return exactMatch
        
        # Return first search result
        # Simply repeat search and grab first item, or empty string
        searchResults = imdbConnect.search_movie(searchTerm)
        if len(searchResults) > 0:
            return searchResults[0]
        else:
            print "Sorry, can't find any IMDb results whatsoever"
            return None
        

"""
    Returns true if two titles match. Takes into account possibility
    that one of titles may have '&' in place of 'and'
"""        
def titlesMatch(title1, title2):
    if title1 == title2:
        return True
        
    # If one of titles has ampersands,
    # check non-ampersand versions of titles
    if " & " in title1:
        if title1.replace('&', 'and') == title2:
            return True
    if " & " in title2:
        if title2.replace('&', 'and') == title1:
            return True
            
    return False
    
"""
    Returns true if year is on at the end of string
"""
def hasYearAtEnd(string):
    return re.search(r'\s' + RES_YEAR + '(?=$)', string) != None

"""
    Removes parenthetical year at end of search term
"""    
def stripYearAtEnd(searchTerm):
    # Searches to see if year is appended to end of string
    match = re.search(r'\s' + RES_YEAR + '(?=$)', searchTerm)
    
    # Remove year if present, or return original string
    if match != None:
        return searchTerm[:match.start()] + searchTerm[match.end():]
    else:
        return searchTerm

"""
    Returns true if movie is an acceptable type (e.g. a TV episode or video game)
"""
def acceptableKind(movie):
    # Check what kind of movie this is
    kind = movie['kind']
    
    print "KIND:", kind
    
    # Return true only if kind is within set of acceptable film formats
    if kind in OK_KINDS:
        return True
        
    # Return false otherwise
    return False
    
##------------------------------------------------------------------

##-------------------------------------##
##-- MAIN METHOD FOR RUNNING/TESTING --##
##-------------------------------------##

"""
    main for running program independently
"""
if __name__ == "__main__":
    """try:
        imdbConnect = imdb.IMDb()
        movieObject = IMDbMovie('0070047', imdbConnect)
        movieObject.getBoxOfficeAsDict()
    except IOError as details:
        print 'Sorry, lost connection to HTTP', details"""
    imdbConnect = imdb.IMDb()
    getClosestMatch('Hopelessly Lost (1973)')