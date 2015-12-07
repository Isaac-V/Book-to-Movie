package main;

import webScrapers.WikiScraper;

public class Main {

	public static void main(String[] args) {
		System.out.println("Checkpoint 0");
		WikiScraper scraper= new WikiScraper();
		scraper.setPageLines("https://en.wikipedia.org/wiki/List_of_fiction_works_made_into_feature_films_%28S-Z%29");
		scraper.getData();
	}

}
