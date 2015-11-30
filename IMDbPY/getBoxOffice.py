#!/usr/bin/env python

import imdb # IMDBpy package
import re # Regular Expression
import csv, codecs, cStringIO

KEY_NAMES = ['Gross Revenue', 'Budget', 'Net Profit']

def is_not_available(value):
    # N/A represented as empty string
    return value == ''
    
def get_as_dict(movieID):
    list = get_as_list(movieID)
    dict = {}
    for i in range(0, len(KEY_NAMES)):
        dict.update({KEY_NAMES[i]: list[i]})
    #print dict
    return dict

def get_as_list(movieID):
  
    # Connect via http
    ia = imdb.IMDb()

    # Get specific movie
    movie = ia.get_movie(movieID)
    ia.update(movie, 'business') # get the business information
    print 'Got movie:', movie.get('title')

    business = movie.get('business') or {} # may be empty
    
    # If no business information, return empty list
    if bool(business) is False:
        print 'No box office info available'
        return ['','','']
    
    # Get gross revenue
    grossint = get_gross_int(business)
    
    # Get estimated budget
    budgetint = get_budget_int(business)
    
    # Calculate net
    #print 'NET PROFIT'
    if isinstance(grossint, int) and isinstance(budgetint, int):
        netint = grossint - budgetint
    else:
        netint = ''
    #print netint
    
    return [grossint, budgetint, netint]
    
def get_gross_int(business):
    gross_list = business.get('gross') or {} # to see what else is available: print
    
    # If no gross information found, return empty string
    if bool(gross_list) is False: # No gross info
        print 'No gross info'
        return ''
    grossint = -1;

    # Grab first 'worldwide' item in list
    print("WORLD WIDE GROSS REVENUE")
    for item in gross_list:
        if 'worldwide' in item or 'Worldwide' in item:
            print item
            # Match found. Convert to int
            match = re.match(r"\$([1-9][0-9,]+)", item)
            gross = match.group()[1:]
            grossint = int(gross.replace(',', ''))
            print grossint
            break

    # If no worldwide found, grab first item in list
    if grossint is -1:
        match = re.match(r"\$([1-9][0-9,]+)", gross_list[0])
        gross = match.group()[1:]
        grossint = int(gross.replace(',', ''))
        #print grossint
    
    return grossint
    
def get_budget_int(business):
    # Grab budget
    #print("BUDGET")
    budget_list = business.get('budget')
    
    #If no budget information returned, replace with empty string
    if bool(budget_list) is False:
        return ''
    
    # Grab first (and presumably only) item in budget
    match = re.match(r"\$([1-9][0-9,]+)", budget_list[0])
    # Convert to int
    budget = match.group()[1:]
    budgetint = int(budget.replace(',', ''))
    #print budgetint
    
    return budgetint

if __name__ == "__main__":
    get_as_dict('1754656', ['Gross Revenue', 'Budget', 'Net Profit'])