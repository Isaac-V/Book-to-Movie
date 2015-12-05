package webScrapers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IMDbScraper extends BasicScraper {
	
	// Fields
	final String IMDB_URL_BASE = "http://www.imdb.com/";
	final String TITLE_URL = "title/";
	final String AWARDS_QUERY = "awards?ref_=tt_awd";
	
	// Constructor
	public IMDbScraper() {
		super();
		System.out.println("Created IMDB scraper");
	}
	
	/* Public Methods */
	
	// Grabs award page based on 'const' value from CSV files.
	// The 'const' value is in the form "tt" + imdbID
	// and is used by IMDb to query pages for a movie with ID of imdbID
	public URL getAwardsPageByConst(String idConst) {
		
		
		String movieAwardUrl = IMDB_URL_BASE + TITLE_URL + idConst + "/" + AWARDS_QUERY;
		
		System.out.println("Getting page: " + movieAwardUrl);
		return this.getPage(movieAwardUrl);
	}
	
	// Grabs award page by movie's IMDb id
	public URL getAwardsPageById(String id) {
		// Convert string to query form and pass to another method
		return this.getAwardsPageByConst("tt" + id);
	}
	
	// Grabs award page by full movie's title URL, as given by CSV file
	public URL getAwardsPageByTitleURL(String movieTitleUrl) {
		return this.getPage(movieTitleUrl + AWARDS_QUERY);
	}
	
	// Find the number of awards listed on given page
	public int getNumAwardsAtPage(URL awardPage) {
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
	
	// Sums all integers listed within the tag.
	// CAUTION: Assumes that numbers only appear between the opening and closing
	// div tag, i.e. that the class/id names do not have numbers
	private int countAwardsFromDiv(String divString) {
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
		
		// Grab the line(s) that contain an element with class="desc"
		// (Should theoretically only be one per page)
		System.out.println(awardPage);
		System.out.println("Getting key: " + "class=\"desc\"");
		ArrayList<String> lineMatchList = this.sourceLinesWithString(awardPage, "<div class=\"desc\">");
		
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
	

	public static void main(String[] args) {
		IMDbScraper scraper = new IMDbScraper();
		String movieConst = "1754656";
		URL moviePage = scraper.getAwardsPageById(movieConst);
		int awards = scraper.getNumAwardsAtPage(moviePage);
		
		System.out.println("This movie won " + awards + " awards");
	}
	

}
