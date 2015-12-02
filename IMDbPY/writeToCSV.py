import csv, codecs, cStringIO
import getBoxOffice
import re # Regular Expression

""" fieldsToKeep specifies table headers from original file that will be preserved in output file.
    All other headers in file will be ignored.
"""
fieldsToKeep = ['const', 'Title', 'IMDb Rating', 'Year', 'URL']
""" fieldsToAdd specifies new table headers that will be added to the output file.
    These define new columns of data that will be filled.
"""
fieldsToAdd = getBoxOffice.KEY_NAMES

""" Specify file names of input and output files (to easily change) """
IN_FILE_NAME = 'SmallTestList.csv'
OUT_FILE_NAME = 'SmallOutputList.csv'


""" Takes CSV files exported from IMDb list and modifies to only include desired columns.
    Removes unwanted columns (anything not in fieldsToKeep). Adds and populates new
    columns as specified in fieldsToAdd.
"""
def readWriteCSVasDict():
    with open(IN_FILE_NAME, 'rb') as inputFile, open(OUT_FILE_NAME, 'wb') as resultFile:
        reader = csv.DictReader(inputFile)
        # Define writer. Headers specified by joined list of fieldsToKeep and fieldsToAdd
        # Any column whose key is not in fields parameter is excluded from the output file
        writer = csv.DictWriter(resultFile, fieldsToKeep + fieldsToAdd, extrasaction="ignore")
        
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
            
            # Write modified row to output file
            writer.writerow(new_row)

    # Close files
    inputFile.close()
    resultFile.close()
    
def updateBoxOffice(row):
    movieID = re.sub('\D', '', row['const'])
    print 'Movie ID is:', movieID
    boxOffice = getBoxOffice.get_as_dict(movieID)
    print boxOffice
    row.update(boxOffice)

""" NOTE: Cannot remove columns (yet) """    
def writeCSVasList():
    with open(IN_FILE_NAME, 'rb') as inputFile, open(OUT_FILE_NAME, 'wb') as resultFile:
        reader = csv.reader(inputFile)
        # Define writer. Headers specified by joined list of fieldsToKeep and fieldsToAdd
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
    
### --------------------------------------------------------------

### TODO: Create Binary Output file.

### --------------------------------------------------------------

if __name__ == "__main__":
    readWriteCSVasDict()