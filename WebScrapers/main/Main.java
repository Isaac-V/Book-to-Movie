package main;

import webScrapers.AmazonBookScraper;
import webScrapers.WikiScraper;

public class Main {

	public static void main(String[] args) {
		System.out.println("Checkpoint 0");
		WikiScraper scraper= new WikiScraper();
		scraper.setPageLines("https://en.wikipedia.org/wiki/List_of_children's_books_made_into_feature_films");
		scraper.getData();
	}

}
