// CMPSCI 383 (Artificial Intelligence)
// Mary Moser (29154085), Isaac Vawter (28277700)

package webScrapers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WikiScraper {
	
	private BasicScraper scraper;
	private URL currentPage;
	private ArrayList<String> pageLines;
	private Map<Integer, String> tableRows;
	private Map<Integer, String[]> tableColumns;

	
	public WikiScraper(){
		this.scraper = new BasicScraper();
		this.currentPage = null;
		this.pageLines = null;
		this.tableRows = new HashMap<>();
		this.tableColumns = new HashMap<>();
		System.out.println("Checkpoint 1");
	};
	

	public void setPageLines(String url){
		this.currentPage = scraper.getPage(url);
		if(this.currentPage != null){
			this.pageLines = scraper.getSourceLinesList(this.currentPage);
		}
		System.out.println("Checkpoint 2");
	}
	
	public void getData(){
		extractTableRows();
		displayQuoteStrings();
	}
	
	private void extractTableRows(){
		if(this.pageLines == null){
			System.out.println("No Lines");
			return;
		}
		int count = 0;
		for(int index = 97; index < 2123; index++){ //Hard-code source lines to target table
			if(pageLines.get(index).equals("<tr>")){
				String row = "";
				while(!pageLines.get(index).equals("</tr>")){
					index++;
					row += pageLines.get(index);
				}
				this.tableRows.put(count, row);
				this.tableColumns.put(count, new String[2]);
//				extractColumns(row);
				count++;
			}
		}
		System.out.println(count);
		extractColumns();
	}
	
	private void extractColumns(){
		for(int h = 0; h < this.tableRows.keySet().size(); h++){
			String row = this.tableRows.get(h);
			String[] columns = this.tableColumns.get(h);
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
			columns[0] = books;
			columns[1] = movies;
		}
	}

	private void displayQuoteStrings(){
		for(int i = 0; i < this.tableColumns.keySet().size(); i++){
			String[] currEntry = this.tableColumns.get(i);
			String currEntry0 = currEntry[0];
			String key0 = "  ";
			String printString0 = ";";
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
						printString0 = quoteString + ";";
					}
					count++;
				}
			}
			System.out.print(printString0);
			
			String currEntry1 = currEntry[1];
			String key1 = "  ";
			String printString1 = "";
			for(int j = 0; j < currEntry1.length(); j++){
				key1 = key1.substring(1) + currEntry1.charAt(j);
				if(key1.equals("\">")){
					j++;
					String quoteString = "";
					while(currEntry1.charAt(j) != '<'){
						quoteString += currEntry1.charAt(j);
						j++;
					}
					printString1 += quoteString + ";";
				}
			}
			System.out.println(printString1 + ";");
		}
	}
}
