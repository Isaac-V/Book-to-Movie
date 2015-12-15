// CMPSCI 383 (Artificial Intelligence)
// Mary Moser (29154085), Isaac Vawter (28277700)

package webScrapers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
	CLASS: IMDbScraper
	Based on BasicScraper. Used to scrape and parse data from web pages on IMDb.com.
	Specifically, for our application, this class is used to find and parse the award
	count on a given film's award page. It also includes methods for reading the original
	data files created by the IMDbPY code, and adds an additional "Awards" column.
**/
public class IMDbScraper extends BasicScraper {
	
	// Fields
	final String IMDB_URL_BASE = "http://www.imdb.com/";
	final String TITLE_URL = "title/";
	final String AWARDS_QUERY = "awards?ref_=tt_awd";
	private URL currentPage;
	private ArrayList<String> currentPageLines;
	
	// Constructor
	public IMDbScraper() {
		super();
		System.out.println("Created IMDB scraper\n");
	}
	
	///////////////////////////////////////////////////////////////////
	
	/** GET AWARD COUNT FOR PARTICULAR MOVIE **/
	
	// Given a movie's ID constant ("tt" + IMDb ID), web scrape the appropriate web page,
	// parse the HTML element containing the award count, and return the total count as an
	// integer. If the HTML element is not found, or the element lists no awards, return 0
	public int getNumAwardsForMovie(String idConst) {
		// Ensure idConst is in its valid form
		// (NOTE: Was only really needed for one poor entry in input file)
		if(!isValidId(idConst)) {
			System.out.println("CAUTION: " + idConst + " is not a valid id! Better fix that...");
			idConst = fixId(idConst);
			System.out.println("Id is now " + idConst);
		}
		
		// Get the appropriate URL for this film's award page
		String movieAwardUrl = buildStringUrl(idConst);
		
		// Get and parse the award page.
		// Store the URL to currentPage, and the parsed lines to currentPageLines
		setAwardsPageData(movieAwardUrl);
		
		// Search the lines for the award count listing, and return total
		// number of nominations. Returns 0 if listing not found or contains no award record.
		return getNumAwardsAtPage(this.currentPage);
		
	}
	
	// Grabs award page based on 'const' value from CSV files.
	// The 'const' value is in the form "tt" + imdbID
	// and is used by IMDb to query pages for a movie with ID of imdbID
	public URL getAwardsPageByConst(String idConst) {
		System.out.println("In getAwardPageByConst method!!!!!!");
		
		
		String movieAwardUrl = buildStringUrl(idConst);
		
		System.out.println("Getting page: " + movieAwardUrl);
		return this.getPage(movieAwardUrl);
	}
	
	// Grabs award page by movie's IMDb id
	public URL getAwardsPageById(String id) {
		System.out.println("In getAwardPageById method!!!!!!");
		// Convert string to query form and pass to another method
		return this.getAwardsPageByConst("tt" + id);
	}
	
	// Takes the id constant and converts it to a url
	public String buildStringUrl(String idConst) {
		System.out.println("In buildStringUrl method!!!!!!");
		return IMDB_URL_BASE + TITLE_URL + idConst + "/" + AWARDS_QUERY;
	}
	
	// Find the number of awards listed on given page
	private int getNumAwardsAtPage(URL awardPage) {
	
		System.out.println("In getAwardsAtPage method!!!!!!");
		
		System.out.println("Grabbing awards page...");
		String divClassDesc = this.getAwardsDescDiv(awardPage);
		
		// Return total number of awards/nominations
		if(divClassDesc.isEmpty()) { // No div of this class used
			// Assume absence of listing means movie has no awards
			return 0;
		} else { // Match <div> tag found
			// Sum all awards listed within the div tag
			return countAwardsFromDiv(divClassDesc);
		}
	}
	
	///////////////////////////////////////////////////////////////////
	
	/** VALIDATING ID CONSTANTS **/
	
	// Return true if String has correct number of characters for an ID constant.
	// Return false otherwise. Mostly used for low-level testing to ensure no leading
	// zeros have accidentally been omitted or added.
	private boolean isValidId(String idConst) {
		if(idConst.length() != 9) {
			return false;
		}
		return true;
	}
	
	// Given an incomplete ID constant with missing leading zeros,
	// returns a corrected version with the correct number of leading zeros.
	// (Numerical ID must be 7 digits long; entire String with "tt" at front must be 9 chars long)
	private String fixId(String idConst) {
		String pattern = "(\\d+)";
		Pattern rePattern = Pattern.compile(pattern);
		Matcher match = rePattern.matcher(idConst);
		
		if(match.find()) {
			String numbers = match.group(0);
			int numPaddingZeros = 7 - numbers.length();
			String newString = "tt";
			for(int i = 0; i < numPaddingZeros; i++) {
				newString += "0";
			}
			newString += numbers;
			return newString;
		} else {
			return "";
		}
	}
	
	///////////////////////////////////////////////////////////////////
	
	/** HELPERS FOR WEB SCRAPING **/
	
	// Puts thread to sleep for specified number of seconds.
	// Used to regulate the number of requests per interval of time,
	// so as not to exceed IMDb's limit of 5 web service requests per second.
	private void sleepMe(int seconds){
		try{
			Thread.sleep(seconds * 1000);
		} catch(InterruptedException e) {
		    System.out.println(e.getMessage());
		}
	}
	
	// Extract and store the current page and page lines
	private void setAwardsPageData(String url){
	
		System.out.println("In setAwardsPageData method!!!!!!");
		URL awardsPage = this.getPage(url);
		ArrayList<String> pageLines = null;
		
		if(awardsPage != null){
			sleepMe(1);
			pageLines = this.getSourceLinesList(awardsPage);
			while(pageLines.size() == 0){
				sleepMe(1);
				pageLines = this.getSourceLinesList(awardsPage);
			}
			this.currentPage = awardsPage;
			this.currentPageLines = pageLines;
		}
		else{
			setAwardsPageData(url);
		}
	}
	
	// Sums all integers listed within the tag.
	// CAUTION: Assumes that numbers only appear between the opening and closing
	// div tag, i.e. that the class/id names do not have numbers
	private int countAwardsFromDiv(String divString) {
		System.out.println("In countAwardsFromDiv method!!!!!!");
		
		String pattern = "(\\d+)";
		Pattern rePattern = Pattern.compile(pattern);
		Matcher match = rePattern.matcher(divString);
		
		int awardCount = 0;
		while(match.find()) {
			System.out.println("Found " + match.group(0));
			awardCount += Integer.parseInt(match.group(0));
		}
		return awardCount;
	}
	
	// From award page, grabs as a string the HTML element <div class="desc">...</div>
	// This div, if it exists, contains information on the total number of wins and
	// nominations this movie has received. If the div is not found, return an empty string
	private String getAwardsDescDiv(URL awardPage) {
	
		System.out.println("In getAwardsDescDiv method!!!!!!");
		
		// Grab the line(s) that contain an element with class="desc"
		// (Should theoretically only be one per page)
		//System.out.println(awardPage);
		System.out.println("Getting key: " + "class=\"desc\"");
		ArrayList<String> lineMatchList = this.grabSourceLinesWithString("<div class=\"desc\">");
		
		// Return empty string if no match found
		if(lineMatchList.isEmpty()) {
			System.err.println("No desc class used");
			return "";
		}
		
		System.out.println("Found: " + "\n" + lineMatchList.get(0));
		
		// Grab description div
		String pattern = "(?s)<div class=\"desc\">(.*?)</div>";
		Pattern rePattern = Pattern.compile(pattern);
		Matcher match = rePattern.matcher(lineMatchList.get(0));
		
		// Return the <div> tag and its contents as a string
		if(match.find()) {
			System.out.println("Found pattern");
			return match.group(0);
		}
		else { // Could not find the matching div
			System.out.println("No match on regex");
			// Return an empty string
			return "";
		}
	}
	
	// Searches page lines of current web page for a given String.
	// Returns a list of all lines containing that String.
	private ArrayList<String> grabSourceLinesWithString(String s){
	
		System.out.println("In grabSourceLinesWithString method!!!!!!");
		
		ArrayList<String> lines = new ArrayList<>();
		
		if(this.currentPageLines != null){
			for(int i = 0; i < this.currentPageLines.size(); i++){
				String currS = this.currentPageLines.get(i);
				if(currS.contains(s)){
					lines.add(currS);
				}
			}
		}
		
		return lines;
	}
	
	///////////////////////////////////////////////////////////////////
	
	/** READING AND WRITING CSV FILES **/
	
	public void readWriteCSV(String inputCsv, String outputCsv) {
		System.out.println("Writing to file...");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputCsv));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsv, true));
			String currLine = "";
			String[] currCols;
			int idIndex = 2;
			
			for (int i = 1; (currLine = reader.readLine()) != null; i++) {
				// Grab this row
				currCols = currLine.split("\\s*;\\s*");
				System.out.println("Input: " + currLine);
				
				
				if(i == 1) { // If first line, marks headers
					// Add awards column
					String toAdd = "Awards";
					currLine += ";" + toAdd;
					// Determine the position of the id column
					// (has const for building url)
					idIndex = this.indexOf(currCols, "IMDb ID");
					System.out.println("ID at index:" + idIndex);
				} else {
					// Grab constant
					String idConst = currCols[idIndex];
					
					// Grab id constant
					String currID = currCols[idIndex];
					
					// Grab number of awards
					System.out.println("This movie's ID is " + currID);
					int awards = this.getNumAwardsForMovie(currCols[idIndex]);
					
					// Add column to row
					String toAdd = "" + awards;
					currLine += ";" + toAdd;
				}
				
				System.out.println("Output: " + currLine);
				System.out.println();
				
				writer.write(currLine);
				writer.newLine();
			}
			
			reader.close();
			writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// Given an String array, returns the first index containing the specified element
	public int indexOf(String[] array, String element) {
		for(int i = 0; i < array.length; i++) {
			if(array[i].equals(element)) {
				return i;
			}
		}
		return -1;
	}
	
	///////////////////////////////////////////////////////////////////
	
	/** RUNNING PROGRAM **/
	
	public static void main(String[] args) {
	
		// Run the file
		IMDbScraper scraper = new IMDbScraper();
		scraper.readWriteCSV("C:/Users/Mary/Documents/GitHub/Book-to-Movie/Data/MovieOutput/Filtered/exOutFilter.csv", "exOutFilterWithAwards.csv");
	}
	

} // end class IMDbScraper