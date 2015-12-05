#!/usr/bin/env python

import imdb # IMDBpy package
import re # Regular Expression
import csv, codecs, cStringIO # For working with CSV files

KEY_NAMES = ['Gross Revenue', 'Budget', 'Net Profit']

# Strings to build regular expressions for filtering lists
## TODO: Use named expressions?
RES_NUM_FORMAT = '(?P<amount>([1-9][0-9,]+))' # Numbers with punctuation
RES_CURRENCY = '\$' # Defines accepted currency
RES_COUNTRY = '\((?P<country>([wW]orldwide|(Non-)?USA))\)' # Defines accepted countries

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
    def get_as_dict(self):
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

"""
    main for running program independently
"""
if __name__ == "__main__":
    try:
        imdbConnect = imdb.IMDb()
        movieObject = IMDbMovie('0070047', imdbConnect)
        movieObject.get_as_dict()
    except IOError as details:
        print 'Sorry, lost connection to HTTP', details