package main;

import webScrapers.AmazonBookScraper;

public class Main {

	public static void main(String[] args) {
		AmazonBookScraper scraper = new AmazonBookScraper();
		scraper.setTitlesFromCSV("C:\\source-files\\Book-to-Movie\\Data\\"
				+ "BookToMovieIndexProofread.csv");
		scraper.getTitleData();
	}

}
