package brain;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TrainingParser {
	
//	Map<String, String> bookMovieIndex;
	Map<String, int[]> bookInputs;
	Map<String, int[]> movieOutputs;
	Map<String, Integer> keyWordIndex;
	ArrayList<String> trainingBooks;
	ArrayList<String> testingBooks;
	
	int inputSize;
	int outputSize;
	
	public TrainingParser(	//String indexFilename,
							String bookDataFilename,
							String movieDataFilename,
							int inputSize,
							int outputSize){
		
//		this.bookMovieIndex = new HashMap<>();
		this.bookInputs = new HashMap<>();
		this.movieOutputs = new HashMap<>();
		this.keyWordIndex = new HashMap<>();
		this.trainingBooks = new ArrayList<>();
		this.testingBooks = new ArrayList<>();
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		
//		initIndex(indexFilename);
		initKeyWordIndex();
		initBookInput(bookDataFilename);
		initMovieOutput(movieDataFilename);
		initTrainingBooks();
	}


//	private void initIndex(String indexFilename){
//		try{
//			BufferedReader reader = new BufferedReader(new FileReader(
//					indexFilename));
//			String indexLine = reader.readLine();
//			while(indexLine != null){
//				String bookTitle = "";
//				String movieTitle = "";
//				int i = 0;
//				while(indexLine.charAt(i) != ';'){
//					bookTitle += indexLine.charAt(i);
//					i++;
//				}
//				i++;
//				while(indexLine.charAt(i) != ';'){
//					movieTitle += indexLine.charAt(i);
//					i++;
//				}
//				this.bookMovieIndex.put(bookTitle, movieTitle);
//			}
//			reader.close();
//		} catch (IOException e){
//			System.out.println(e.getMessage());
//		}
//	}
	
	private void initKeyWordIndex() {
		this.keyWordIndex.put("sci-fi", 0);
		this.keyWordIndex.put("crime", 1);
		this.keyWordIndex.put("classic", 2);
		this.keyWordIndex.put("adventure", 3);
		this.keyWordIndex.put("comedy", 4);
		this.keyWordIndex.put("mystery", 5);
		this.keyWordIndex.put("fantasy", 6);
		this.keyWordIndex.put("romance", 7);
		this.keyWordIndex.put("tragedy", 8);
		this.keyWordIndex.put("wizard", 9);
		this.keyWordIndex.put("drama", 10);
		this.keyWordIndex.put("satire", 11);
		this.keyWordIndex.put("tech", 12);
		this.keyWordIndex.put("vampire", 13);
		this.keyWordIndex.put("histor", 14);
		this.keyWordIndex.put("myth", 15);
		this.keyWordIndex.put("western", 16);
		this.keyWordIndex.put("space", 17);
		this.keyWordIndex.put("animal", 18);
		this.keyWordIndex.put("voyage", 19);
	}

	private void initBookInput(String bookDataFilename) {
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(
					bookDataFilename));
			
			String bookLine = reader.readLine();

			while(bookLine != null){
				int i = 0;
				
				String title = "";
				while(bookLine.charAt(i) != ';'){
					title += bookLine.charAt(i);
					i++;
				}
				
				this.bookInputs.put(title, getInputsFromString(bookLine));
				
				bookLine = reader.readLine();
			}
			reader.close();
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	private void initMovieOutput(String movieDataFilename) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(
					movieDataFilename));
			
			String movieLine = reader.readLine();
			movieLine = reader.readLine();

			while(movieLine != null){
				int i = 0;
				
				String bookTitle = "";
				while(movieLine.charAt(i) != ';'){
					bookTitle += movieLine.charAt(i);
					i++;
				}
								
				this.movieOutputs.put(bookTitle, getOutputsFromString(movieLine));
				
				movieLine = reader.readLine();
			}
			reader.close();
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	private void initTrainingBooks() {
		for(String bookTitle : this.bookInputs.keySet()){
			double val = Math.random();
			if(val < 0.8){
				this.trainingBooks.add(bookTitle);
			}
			else{
				this.testingBooks.add(bookTitle);
			}
		}
	}
	
	private int[] getInputsFromString(String bookLine){
		int index = 0;
		int i = 0;
		int[] inputs = new int[this.inputSize];

		//Book Title Column
		while(bookLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		String bestSeller = "";
		while(bookLine.charAt(i) != ';'){
			bestSeller += bookLine.charAt(i);
			i++;
		}
		inputs[index] = Integer.parseInt(bestSeller);
		i++;
		index++;
		
		String rating = "";
		while(bookLine.charAt(i) != ';'){
			rating += bookLine.charAt(i);
			i++;
		}
		int ratingVal = (int) Double.parseDouble(rating);
		if(ratingVal == 5){
			inputs[4] = 1;
		}
		else{
			inputs[ratingVal] = 1;
		}
		i++;
		index += 4;
		
		String reviews = "";
		while(bookLine.charAt(i) != ';'){
			reviews += bookLine.charAt(i);
			i++;
		}
		int revCount = Integer.parseInt(reviews);
		int revIndex = 31 - Integer.numberOfLeadingZeros(revCount);
		if(revIndex > 14){
			inputs[index + 14] = 1;
		}
		else{
			inputs[index + revIndex] = 1;
		}
		i++;
		index += 15;
		
		String ageGroup = "";
		while(bookLine.charAt(i) != ';'){
			ageGroup += bookLine.charAt(i);
			i++;
		}
		if(ageGroup.equals("child")){
			inputs[index] = 1;
		}
		else if(ageGroup.equals("teen")){
			inputs[index + 1] = 1;
		}else{
			inputs[index + 2] = 1;
		}
		i++;
		index += 3;
		
		while(i < bookLine.length()){
			String keyword = "";
			while(bookLine.charAt(i) != ';' && i < bookLine.length()){
				keyword += bookLine.charAt(i);
				i++;
			}
			inputs[index + this.keyWordIndex.get(keyword)] = 1;
			i++;
		}
		
		return inputs;
	}
	
	private int[] getOutputsFromString(String movieLine){
		int index = 0;
		int i = 0;
		int[] outputs = new int[this.outputSize];
		
//		String bookTitle = "";
		while(movieLine.charAt(i) != ';'){
//			bookTitle += movieLine.charAt(i);
			i++;
		}
		i++;
		
//		String movieTitle = "";
		while(movieLine.charAt(i) != ';'){
//			movieTitle += movieLine.charAt(i);
			i++;
		}
		i++;
		
		//IMDB ID Column
		while(movieLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		String rating = "";
		while(movieLine.charAt(i) != ';'){
			rating += movieLine.charAt(i);
			i++;
		}
		int ratingVal = (int) Double.parseDouble(rating);
		if(ratingVal == 10){
			outputs[8] = 1;
		}
		else if(ratingVal == 0){
			outputs[0] = 1;
		}
		else{
			outputs[ratingVal-1] = 1;
		}
		i++;
		index += 9;
		
		String boxOffice = "";
		while(movieLine.charAt(i) != ';'){
			boxOffice += movieLine.charAt(i);
			i++;
		}
		int boxRev = Integer.parseInt(boxOffice)/100000;
		int revIndex = 31 - Integer.numberOfLeadingZeros(boxRev);
		if(revIndex > 14){
			outputs[index + 14] = 1;
		}
		else{
			outputs[index + revIndex] = 1;
		}
		i++;
		index += 15;
		
		String prodCost = "";
		while(movieLine.charAt(i) != ';'){
			prodCost += movieLine.charAt(i);
			i++;
		}
		int budget = Integer.parseInt(prodCost)/10000;
		int budIndex = 31 - Integer.numberOfLeadingZeros(budget);
		if(budIndex > 14){
			outputs[index + 14] = 1;
		}
		else{
			outputs[index + budIndex] = 1;
		}
		i++;
		index += 15;
		
		//Profit Column
		while(movieLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		String nomnoms = "";
		while(i < movieLine.length() && movieLine.charAt(i) != ';'){
			nomnoms += movieLine.charAt(i);
			i++;
		}
		int nomCat = Integer.parseInt(nomnoms) / 20;
		if(nomCat > 4){
			outputs[index + 4] = 1;
		}
		else{
			outputs[index + nomCat] = 1;
		}
		
		return outputs;
	}


//	public Map<String, String> getBookMovieIndex() {
//		return bookMovieIndex;
//	}


	public Map<String, int[]> getBookInputs() {
		return bookInputs;
	}


	public Map<String, int[]> getMovieOutputs() {
		return movieOutputs;
	}


	public ArrayList<String> getTrainingBooks() {
		return trainingBooks;
	}


	public ArrayList<String> getTestingBooks() {
		return testingBooks;
	}

}
