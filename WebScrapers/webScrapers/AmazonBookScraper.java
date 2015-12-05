package webScrapers;

import inputOutputEnums.Input;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class AmazonBookScraper{
	
	
	private BasicScraper scraper;
	private URL currentPage;
	private ArrayList<String> currentPageLines;
	private Map<String, String> currentData;
	private Map<Input, Boolean[]> currentANNInputs;
	
	
	public AmazonBookScraper(){
		this.scraper = new BasicScraper();
		this.currentANNInputs = new HashMap<>();
		this.currentANNInputs.put(Input.BESTSELLER, new Boolean[Input.BESTSELLER.size()]);
		this.currentANNInputs.put(Input.RATING, new Boolean[Input.RATING.size()]);
		this.currentANNInputs.put(Input.REVIEWCOUNT, new Boolean[Input.REVIEWCOUNT.size()]);
		this.currentANNInputs.put(Input.AGEGROUP, new Boolean[Input.AGEGROUP.size()]);
		this.currentANNInputs.put(Input.THEMES, new Boolean[Input.THEMES.size()]);
		this.currentANNInputs.put(Input.TYPE, new Boolean[Input.TYPE.size()]);
		System.out.println("Checkpoint 1");
	};
	
	public Map<Input, Boolean[]> getANNInputs(){
		return this.currentANNInputs;
	}
	
	public void setANNInputsFromTitle(String title){
		System.out.println("Checkpoint 2");
		URL bookPage = amazonBookPage(title);
		
		if(bookPage == null){
			System.out.println("No Book Page");
		}
		

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
		
		ArrayList<String> sourceLines = scraper.getSourceLinesWithString(searchPage, keyPhrase);
		
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
