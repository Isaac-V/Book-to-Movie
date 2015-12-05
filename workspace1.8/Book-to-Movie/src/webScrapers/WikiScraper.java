package webScrapers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import inputOutputEnums.Input;

public class WikiScraper {
	
	private BasicScraper scraper;
	private URL currentPage;
	private ArrayList<String> currentPageLines;
	private Map<String, String> currentData;

	
	
	public WikiScraper(){
		this.scraper = new BasicScraper();

		System.out.println("Checkpoint 1");
	};
	
	

	public void setPageLines(String url){
		this.currentPage = scraper.getPage(url);
		this.currentPageLines = scraper.getSourceLinesList(
				scraper.getPage(url), key)
				
	}
	
	private URL amazonBookPage(String title){
		URL bookPage = null;
		System.out.println("Checkpoint 3");
		String bookURL = getURLfromSearch(title);
		
		if(bookURL != null){
			bookPage = scraper.getPage(bookURL);
		}

		System.out.println("Checkpoint 9");
		return bookPage;
	}
	
	private String getURLfromSearch(String title){
		String bookURL = null;
		System.out.println("Checkpoint 4");
		URL searchPage = amazonSearchPage(title);
		String keyPhrase = "a-row s-result-list-parent-container";
		
		ArrayList<String> sourceLines = scraper.sourceLinesWithString(searchPage, keyPhrase);
		
		if(!sourceLines.isEmpty()){
			bookURL = getURLfromSearchHelper(sourceLines.get(0));
		}
		
		return bookURL;
	}
	
	private String getURLfromSearchHelper(String sourceLine) {
		String bookURL = "";
		String keyPhrase = "      ";
		for(int index = 0; index < sourceLine.length(); index++){
			keyPhrase = keyPhrase.substring(1) + sourceLine.charAt(index);
			if(keyPhrase.equals("href=\"")){
				System.out.println("Checkpoint 8");
				index++;
				while(sourceLine.charAt(index) != '\"'){
					bookURL += sourceLine.charAt(index);
					index++;
				}
				break;
			}
		}
		System.out.println("bookURL = " + bookURL);
		return bookURL;
	}

	private URL amazonSearchPage(String title){
		URL searchPage = null;
		String titleWords = title.replace(' ', '+');
		System.out.println("Checkpoint 5");
		String amazonSearch = "http://www.amazon.com/s/ref=nb_sb_noss_2?url=search-alias%3Dstripbooks&field-keywords=";
		amazonSearch += titleWords;
		System.out.println("amazonSearch = " + amazonSearch);

		searchPage = scraper.getPage(amazonSearch);
		
		System.out.println("Checkpoint 6");
		return searchPage;
	}
	
}
