package webScrapers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class BasicScraper {
	
	private String errorMessage;

	
	public BasicScraper(){
		this.errorMessage = "No Error";
	};
	
	
	public String getErrorMessage(){
		return this.errorMessage;
	}
	
	private void resetErrorMessage(){
		this.errorMessage = "No Error";
	}
	
	public URL getPage(String url){
		URL page = null;
		
		resetErrorMessage();
		
		try{
			page = new URL(url);
		} catch(MalformedURLException e){
			System.out.println(e.getMessage());
		}
		
		if(page == null){
			this.errorMessage = "Failed to Resolve Page";
			System.out.println(errorMessage);
		}
		
		return page;
	}
	
	public String sourceLineWithString(URL page, String key){
		String targetLine = null;
		
		resetErrorMessage();
		
		try{
			BufferedReader pageReader = new BufferedReader(
			        new InputStreamReader(page.openStream()));
			String sourceLine = pageReader.readLine();
			while(sourceLine != null){
				if(sourceLine.contains(key)){
					targetLine = sourceLine;
					break;
				}
				else{
					sourceLine = pageReader.readLine();
				}
			}
			
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
		
		if(targetLine == null){
			this.errorMessage = "No Line in "+ page.getPath() + " with \"" + key + "\"";
		}
		
		return targetLine;
	}

}
