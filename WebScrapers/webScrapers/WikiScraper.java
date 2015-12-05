package webScrapers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class WikiScraper {
	
	private BasicScraper scraper;
	private URL currentPage;
	private ArrayList<String> currentPageLines;
	private ArrayList<ArrayList<String>> currentTableEntries;
	private Map<String, ArrayList<String>> currentData;

	
	public WikiScraper(){
		this.scraper = new BasicScraper();
		this.currentPage = null;
		this.currentPageLines = null;
		this.currentData = new HashMap<>();
		this.currentTableEntries = new ArrayList<>();
		currentTableEntries.add(new ArrayList<>());
		currentTableEntries.add(new ArrayList<>());
		System.out.println("Checkpoint 1");
	};
	

	public void setPageLines(String url){
		this.currentPage = scraper.getPage(url);
		if(this.currentPage != null){
			this.currentPageLines = scraper.getSourceLinesList(this.currentPage);
		}
		System.out.println("Checkpoint 2");
	}
	
	public void getData(){
		extractTableRows();
		displayQuoteStrings();
	}
	
	private void extractTableRows(){
		if(this.currentPageLines == null){
			System.out.println("No Lines");
			return;
		}
		int count = 0;
		for(int index = 0; index < 2251; index++){
			if(currentPageLines.get(index).equals("<tr>")){
				count++;
				String row = "";
				while(!currentPageLines.get(index).equals("</tr>")){
					index++;
					row += currentPageLines.get(index);
				}
				extractColumns(row);
			}
		}
		System.out.println(count);
		this.currentTableEntries.get(0).remove(0);
		this.currentTableEntries.get(1).remove(0);
		System.out.println(this.currentTableEntries.get(0).get(0));
		System.out.println(this.currentTableEntries.get(1).get(0));
		System.out.println(this.currentTableEntries.get(0).get(322));
		System.out.println(this.currentTableEntries.get(1).get(322));
		System.out.println(this.currentTableEntries.get(0).size());
		System.out.println(this.currentTableEntries.get(1).size());
	}
	
	private void extractColumns(String row){
		String books = "";
		String movies = "";
		String breakPoint = "     ";
		for(int i = 0; i<row.length(); i++){
			char ch = row.charAt(i);
			if(!breakPoint.equals("</td>")){
				breakPoint = breakPoint.substring(1) + ch;
				books += ch;
			}
			else{
				movies += ch;
			}
		}
		this.currentTableEntries.get(0).add(books);
		this.currentTableEntries.get(1).add(movies);
	}

	private void displayQuoteStrings(){
		for(int i = 0; i < this.currentTableEntries.get(0).size(); i++){
			String currEntry0 = this.currentTableEntries.get(0).get(i);
			String key0 = "  ";
			int count = 0;
			for(int j = 0; j < currEntry0.length(); j++){
				key0 = key0.substring(1) + currEntry0.charAt(j);
				if(key0.equals("\">")){
					j++;
					String quoteString = "";
					while(currEntry0.charAt(j) != '<'){
						quoteString += currEntry0.charAt(j);
						j++;
					}
					if(count == 0){
						System.out.print(quoteString + ";");
					}
					count++;
				}
			}
			
			String currEntry1 = this.currentTableEntries.get(1).get(i);
			String key1 = "  ";
			for(int j = 0; j < currEntry1.length(); j++){
				key1 = key1.substring(1) + currEntry1.charAt(j);
				if(key1.equals("\">")){
					j++;
					String quoteString = "";
					while(currEntry1.charAt(j) != '<'){
						quoteString += currEntry1.charAt(j);
						j++;
					}
					System.out.print(quoteString + ";");
				}
			}
			System.out.println();
		}
	}
}
