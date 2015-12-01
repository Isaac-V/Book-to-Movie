package main;

import webScrapers.AmazonBookScraper;

public class Main {

	public static void main(String[] args) {
		System.out.println("Checkpoint 0");
		AmazonBookScraper scraper= new AmazonBookScraper();
		scraper.setANNInputsFromTitle("Cinderella dinosaur");
	}

}
