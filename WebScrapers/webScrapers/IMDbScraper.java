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
//import com.csvreader.CsvReader;
//import com.csvreader.CsvWriter;

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
		System.out.println("Created IMDB scraper");
	}
	
	/* Public Methods */
	
	
	public int getNumAwardsForMovie(String idConst) {
	
		System.out.println("In getMe method!!!!!!");
		
		String movieAwardUrl = IMDB_URL_BASE + TITLE_URL + idConst + "/" + AWARDS_QUERY;
		
		setAwardsPageData(movieAwardUrl);
		
		/*System.out.println("currentPage: " + currentPage);
		
		for(int i = 0; i < currentPageLines.size(); i++) {
			System.out.println("Current line " + i + ":\n" + currentPageLines.get(i));
		}*/
		
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
	
	// Grabs award page by full movie's title URL, as given by CSV file
	/*public URL getAwardsPageByTitleURL(String movieTitleUrl) {
		return this.getPage(movieTitleUrl + AWARDS_QUERY);
	}*/
	
	
	
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
	
	
	
	/* Private Helpers */
	
	private void sleepMe(){
		try{
			Thread.sleep(1000);
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
			pageLines = this.getSourceLinesList(awardsPage);
			while(pageLines.size() == 0){
				sleepMe();
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
			System.out.println("Found");
			System.out.println(match.group(0));
			awardCount += Integer.parseInt(match.group(0));
		}
		return awardCount;
	}
	
	// From award page, grabs as a string the html element <div class="desc">...</div>
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
	
	private ArrayList<String> grabSourceLinesWithString(String s){
	
		System.out.println("In grabSourceLinesWithString method!!!!!!");
		
		ArrayList<String> lines = new ArrayList<>();
		
		if(this.currentPageLines != null){
			for(int i = 0; i < this.currentPageLines.size(); i++){
				//System.out.println("Source line " + i + ":\n" + currentPageLines.get(i));
				String currS = this.currentPageLines.get(i);
				if(currS.contains(s)){
					lines.add(currS);
				}
			}
		}
		
		return lines;
	}
	
	
	
	///////////////////////////////////////////////////////////////////
	
	/* Writing results */
	
	public void readWriteCSV(String inputCsv, String outputCsv) {
		System.out.println("Writing to file...");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputCsv));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsv));
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
					
					// Grab number of awards
					String currID = currCols[idIndex];
					System.out.println("This movie's ID is " + currID);
					int awards = this.getNumAwardsForMovie(currCols[idIndex]);
					
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
	
	public int indexOf(String[] array, String element) {
		for(int i = 0; i < array.length; i++) {
			if(array[i].equals(element)) {
				return i;
			}
		}
		return -1;
	}
	

	public static void main(String[] args) {
	
		// TODO: Write to loop through csv file, preferably with movieID added
		/*IMDbScraper scraper = new IMDbScraper();
		String movieConst = "tt1707386";
		//URL moviePage = scraper.getAwardsPageById(movieConst);
		scraper.getMe(movieConst);
		int awards = scraper.getMe(movieConst);
		
		System.out.println("This movie won " + awards + " awards");*/
		
		IMDbScraper scraper = new IMDbScraper();
		scraper.readWriteCSV("sample.txt", "sampleOut.txt");
	}
	

} // end class IMDbScraper

/*
class IMDbCSVReader {

	private String fileName;
	private char delimiter;
	
	public IMDbCSVReader(String fileName, char delimiter) {
		this.fileName = fileName;
		this.delimiter = delimiter;
	}
	
	public ReadFromFile() {
		try {
			
			CsvReader csvMovies = new CsvReader(this.fileName, this.delimiter);
		
			csvMovies.readHeaders();

			while (csvMovies.readRecord()) {
				String movieTitle = csvMovies.get("Movie");
				String movieIDConst = csvMovies.get("IMDb ID");
				
				// perform program logic here
				System.out.println("Pulling info for " + movieTitle + ":" + movieIDConst);
			}
	
			csvMovies.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	

} // class IMDbCSVReader

class IMDbCSVWriter {
	
	public IMDbCSVWriter() {
	
	}


} // class IMDbCSVWriter*/
