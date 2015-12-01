package webScrapers;

import inputOutputEnums.Input;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;



public class AmazonBookScraper {
	
	private String errorMessage;
	private Map<Input, Boolean[]> aNNInputs;
	
	public AmazonBookScraper(){
		this.errorMessage = "No Error";
		this.aNNInputs = new HashMap<>();
		this.aNNInputs.put(Input.BESTSELLER, new Boolean[Input.BESTSELLER.size()]);
		this.aNNInputs.put(Input.RATING, new Boolean[Input.RATING.size()]);
		this.aNNInputs.put(Input.REVIEWCOUNT, new Boolean[Input.REVIEWCOUNT.size()]);
		this.aNNInputs.put(Input.AGEGROUP, new Boolean[Input.AGEGROUP.size()]);
		this.aNNInputs.put(Input.THEMES, new Boolean[Input.THEMES.size()]);
		this.aNNInputs.put(Input.TYPE, new Boolean[Input.TYPE.size()]);
		System.out.println("Checkpoint 1");
	};
	
	public Map<Input, Boolean[]> getANNInputs(){
		return this.aNNInputs;
	}
	
	public String getErrorMessage(){
		return this.errorMessage;
	}
	
	public void resetErrorMessage(){
		this.errorMessage = "No Error";
	}
	
	public void setANNInputsFromTitle(String title){
		System.out.println("Checkpoint 2");
		URL bookPage = amazonBookPage(title);
		
		if(bookPage == null){
			System.out.println(this.errorMessage);
			resetErrorMessage();
		}
		
		try{
			System.out.println("Checkpoint 10");
			BufferedReader pageReader = new BufferedReader(
			        new InputStreamReader(bookPage.openStream()));
			String sourceLine = pageReader.readLine();
//			while(sourceLine != null){
//				System.out.println(sourceLine);
//				sourceLine = pageReader.readLine();
//			}
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	private URL amazonBookPage(String title){
		URL bookPage = null;
		System.out.println("Checkpoint 3");
		String bookURL = getURLfromSearch(title);
		
		if(bookURL == null && errorMessage.equals("No Error")){
			this.errorMessage = "No Search Results";
			return null;
		}
		else if(bookURL == null){
			return null;
		}
		
		try{
			bookPage = new URL(bookURL);
		} catch(MalformedURLException e){
			System.out.println(e.getMessage());
		}
		System.out.println("Checkpoint 9");
		return bookPage;
	}
	
	private String getURLfromSearch(String title){
		String bookURL = null;
		System.out.println("Checkpoint 4");
		URL searchPage = amazonSearchPage(title);
		
		if(searchPage == null && errorMessage.equals("No Error")){
			this.errorMessage = "Failed Amazon Search";
			return null;
		}
		else if(searchPage == null ){
			return null;
		}
		
		try{
			BufferedReader pageReader = new BufferedReader(
			        new InputStreamReader(searchPage.openStream()));
			String sourceLine = pageReader.readLine();
			while(sourceLine != null){
				if(sourceLine.contains("a-row s-result-list-parent-container")){
					System.out.println("Checkpoint 7");
					bookURL = getURLfromSearchHelper(sourceLine);
					break;
				}
				else{
					sourceLine = pageReader.readLine();
				}
			}
			
		} catch (IOException e){
			System.out.println(e.getMessage());
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
		try{
			searchPage = new URL(amazonSearch);
		} catch(MalformedURLException e){
			System.out.println(e.getMessage());
		}
		System.out.println("Checkpoint 6");
		return searchPage;
	}
	
}