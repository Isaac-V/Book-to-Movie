// CMPSCI 383 (Artificial Intelligence)
// Mary Moser (29154085), Isaac Vawter (28277700)

package webScrapers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;

import resources.Input;



public class AmazonBookScraper{
	
	
	private BasicScraper scraper;
	private URL currentPage;
	private ArrayList<String> currentPageLines;
    private ArrayList<String> titles;
    private ArrayList<String> keyWords;
	private Map<String, String> titleData;
	private Map<Input, Boolean[]> currentANNInputs;
	
	
	public AmazonBookScraper(){
		this.scraper = new BasicScraper();
		this.titleData = new TreeMap<>();
		this.keyWords = new ArrayList<>();
		this.keyWords.add("sci-fi");
		this.keyWords.add("crime");
		this.keyWords.add("classic");
		this.keyWords.add("adventure");
		this.keyWords.add("comedy");
		this.keyWords.add("mystery");
		this.keyWords.add("fantasy");
		this.keyWords.add("romance");
		this.keyWords.add("tragedy");
		this.keyWords.add("wizard");
		this.keyWords.add("drama");
		this.keyWords.add("satire");
		this.keyWords.add("tech");
		this.keyWords.add("vampire");
		this.keyWords.add("histor");
		this.keyWords.add("myth");
		this.keyWords.add("western");
		this.keyWords.add("space");
		this.keyWords.add("animal");
		this.keyWords.add("voyage");
//		this.currentANNInputs = new TreeMap<>();
//		this.currentANNInputs.put(Input.BESTSELLER, new Boolean[Input.BESTSELLER.size()]);
//		this.currentANNInputs.put(Input.RATING, new Boolean[Input.RATING.size()]);
//		this.currentANNInputs.put(Input.REVIEWCOUNT, new Boolean[Input.REVIEWCOUNT.size()]);
//		this.currentANNInputs.put(Input.AGEGROUP, new Boolean[Input.AGEGROUP.size()]);
//		this.currentANNInputs.put(Input.THEMES, new Boolean[Input.THEMES.size()]);
//		this.currentANNInputs.put(Input.TYPE, new Boolean[Input.TYPE.size()]);
	};
	
	public Map<Input, Boolean[]> getANNInputs(){
		return this.currentANNInputs;
	}
	
	public void setANNInputsFromTitle(String title){
		System.out.println("Checkpoint 2");
	}
	
	public void filterBookData(){
		ArrayList<String> dataStore = new ArrayList<>();
		try{
			BufferedReader pageReader = new BufferedReader(new FileReader(
					"C:\\Users\\Isaac\\Desktop\\BookData.csv"));
			String sourceLine = pageReader.readLine();
			while(sourceLine != null){
				dataStore.add(sourceLine);
				sourceLine = pageReader.readLine();
			}
			pageReader.close();
			
			FileWriter writer = new FileWriter(
					"C:\\Users\\Isaac\\Desktop\\BookDataFiltered.csv", true);
			for(int i = 0; i<dataStore.size(); i++){
				if(!dataStore.get(i).contains(";0;0.0;0;")){
					writer.write(dataStore.get(i) + "\n");
				}
			}
			writer.close();
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	public void getTitleData(){

		for(int titleIndex = 0; titleIndex < this.titles.size(); titleIndex++){
			String title = this.titles.get(titleIndex);
//				sleepMe();
			setAmazonBookPageSource(title);
			if(this.currentPageLines != null){
				System.out.println(this.currentPageLines.size());
			}
			String dataString = "";
			dataString += parseBestSeller() + ";";
			dataString += parseRating() + ";";
			dataString += parseReviewCount() + ";";
			dataString += parseAgeGroup() + ";";
			for(String keyword : parseKeywords()){
				dataString += keyword + ";";
			}
			
			this.titleData.put(title, dataString);
			
			try{
				FileWriter writer = new FileWriter(
						"C:\\Users\\Isaac\\Desktop\\BookData.csv", true);
				String output = title + ";" + dataString + "\n";
//				if(this.currentPage != null){
//					writer.write(this.currentPage.toString() + "\n");
//				}
				writer.write(output);
				System.out.print(output);
				writer.close();
			} catch (IOException e){
				System.out.println(e.getMessage());
			}
			
			
		}
			
			
			for(String title : this.titleData.keySet()){
				System.out.println(title + ";" + this.titleData.get(title));
			}
			
		
	}
    
    private ArrayList<String> parseKeywords(){
        ArrayList<String> keywords = new ArrayList<>();
        if(this.currentPage == null || this.currentPageLines == null){
        	return keywords;
        }
        
        Map<String, Boolean> keyWordMap = new TreeMap<>();
        for(String s : this.keyWords){
        	keyWordMap.put(s, false);
        }
        
        ArrayList<Integer> targetDescLines = getSourceLineNumsWithString("bookDesc_override_CSS");
        
        ArrayList<String> targetSubjectLines = getSourceLinesWithString("zg_hrsr");
        
        if(!targetDescLines.isEmpty()){
        	for(int i = targetDescLines.get(0) + 18; i < targetDescLines.get(0) + 23; i++){
        		String descLine = this.currentPageLines.get(i).toLowerCase();
            	for(String k : this.keyWords){
            		if(descLine.contains(k)){
            			keyWordMap.put(k, true);
            		}
            	}
            }
        }
        
        for(String s : targetSubjectLines){
        	String subjLine = s.toLowerCase();
        	for(String k : this.keyWords){
        		if(subjLine.contains(k)){
        			keyWordMap.put(k, true);
        		}
        	}
        }
        
        for(String k : keyWordMap.keySet()){
        	if(keyWordMap.get(k)){
        		keywords.add(k);
        	}
        }
        return keywords;
    }
    
    private String parseAgeGroup(){
    	if(this.currentPage == null){
        	return "adult";
        }
    	
    	boolean teen = false;
    	boolean child = false;

        ArrayList<String> targetLines = getSourceLinesWithString("zg_hrsr");
        
        for(String s : targetLines){
        	if(s.contains("Teens")){
        		teen = true;
        	}
        	else if(s.contains("Children's")){
        		child = true;
        	}
        }
        
        if(child){
        	return "child";
        }
        else if(teen){
        	return "teen";
        }
        else return "adult";
    }
    
    private int parseReviewCount(){
        int reviewCount = 0;
        if(this.currentPage == null){
        	return reviewCount;
        }
        
        ArrayList<String> targetLines = getSourceLinesWithString("acrCustomerReviewText");
        
        if(!targetLines.isEmpty()){
        	String rcLine = targetLines.get(0);
        	String rcStr = "";
        	boolean toggle = false;
        	for(int i = 0; i < rcLine.length(); i++){
        		if(toggle){
        			if(rcLine.charAt(i) == ' '){
        				break;
        			}
        			else{
        				rcStr += rcLine.charAt(i);
        			}
        		}
        		if(rcLine.charAt(i) == '>'){
        			toggle = true;
        		}
        	}

        	reviewCount = Integer.parseInt(rcStr.replace(",", ""));
        }
        
        return reviewCount;
    }
    
    
    private double parseRating(){
        double rating = 0.0;
        if(this.currentPage == null){
        	return rating;
        }
        
        ArrayList<String> targetLines = getSourceLinesWithString("reviewCountTextLinkedHistogram");
        
        if(!targetLines.isEmpty()){
        	String ratingLine = targetLines.get(0);
        	String preStr = "       ";
        	String ratingStr = "";
        	boolean toggle = false;
        	for(int i = 0; i < ratingLine.length(); i++){
        		if(preStr.equals("title=\"")){
        			toggle = true;
        		}
        		else{
        			preStr = preStr.substring(1) + ratingLine.charAt(i);
        		}
        		
        		if(toggle){
        			if(ratingLine.charAt(i) == ' '){
        				break;
        			}
        			else{
        				ratingStr += ratingLine.charAt(i);
        			}
        		}
        	}
        	
        	if(ratingStr.length() > 0){
        		rating = Double.parseDouble(ratingStr);
        	}
        	
        }
        
        return rating;
    }
    
    private int parseBestSeller(){
        int bestSeller = 0;
        if(this.currentPage == null){
        	return bestSeller;
        }
        
        if(!getSourceLineNumsWithString( "'rank-number'>1").isEmpty()){
        	bestSeller = 1;
        }
        return bestSeller;
    }
    
    public void setTitlesFromCSV(String indexFilePath){
        
        ArrayList<String> titles = new ArrayList<>();
        
        try{
			BufferedReader pageReader = new BufferedReader(new FileReader(indexFilePath));

            String indexLine = pageReader.readLine();
            while(indexLine != null){
                String title = "";
                for(int i = 0; i < indexLine.length(); i++){
                    char c = indexLine.charAt(i);
                    if(c == ';'){
                        break;
                    }
                    else{
                        title += c;
                    }
                }
                titles.add(title);
                indexLine = pageReader.readLine();
            }
            pageReader.close();
			
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
        
        this.titles = titles;
    }
	
	private void setAmazonBookPageSource(String title){
		
		String bookURL = getURLfromSearch(title);
		
//		System.out.println("Amazon Book Page Loading...");
		if(bookURL != null){
			setBookPageData(bookURL);
		}
		else{
			this.currentPage = null;
			this.currentPageLines = null;
		}
		
	}
	
	private void setBookPageData(String url){
		URL bookPage = this.scraper.getPage(url);
		ArrayList<String> pageLines = null;
		
		if(bookPage != null){
			pageLines = this.scraper.getSourceLinesList(bookPage);
			while(pageLines.size() == 0){
				sleepMe();
				pageLines = this.scraper.getSourceLinesList(bookPage);
			}
			this.currentPage = bookPage;
			this.currentPageLines = pageLines;
		}
		else{
//			sleepMe();
			setBookPageData(url);
		}
	}
	
	private String getURLfromSearch(String title){
		String bookURL = null;
		URL searchPage = amazonSearchPage(title);
		String keyPhrase = "a-row s-result-list-parent-container";
		
//		System.out.println("Finding First Search Result...");
		ArrayList<String> sourceLines = this.scraper.getSourceLinesWithString(
				searchPage, keyPhrase);
		
		int counter = 0;
		while(sourceLines.size() == 0 && counter < 10){
			sleepMe();
			sourceLines = this.scraper.getSourceLinesWithString(
					searchPage, keyPhrase);
			counter++;
		}
		
		if(sourceLines.size() > 0){
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
				index++;
				while(sourceLine.charAt(index) != '\"'){
					bookURL += sourceLine.charAt(index);
					index++;
				}
				break;
			}
		}
//		System.out.println("bookURL = " + bookURL);
		return bookURL;
	}

	private URL amazonSearchPage(String title){
		URL searchPage = null;
		String titleWords = title.replace(' ', '+');
//		System.out.println("Amazon Search...");
		String amazonSearch = "http://www.amazon.com/s/ref=nb_sb_noss_2?url=search-alias%3Dstripbooks&field-keywords=";
		amazonSearch += titleWords;
//		System.out.println("amazonSearch = " + amazonSearch);

		searchPage = this.scraper.getPage(amazonSearch);
		
		return searchPage;
	}
	
	private void sleepMe(){
		try{
			Thread.sleep(1000);
		} catch(InterruptedException e) {
		    System.out.println(e.getMessage());
		}
	}
	
	private ArrayList<Integer> getSourceLineNumsWithString(String s){
		ArrayList<Integer> lineNums = new ArrayList<>();
		
		if(this.currentPageLines != null){
			for(int i = 0; i < this.currentPageLines.size(); i++){
				if(this.currentPageLines.get(i).contains(s)){
					lineNums.add(i);
				}
			}
		}
		
		return lineNums;
	}
	
	private ArrayList<String> getSourceLinesWithString(String s){
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
	
}
