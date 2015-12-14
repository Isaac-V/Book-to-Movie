//Mary Moser
//Isaac Vawter

package brain;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//TrainingParser class for parsing data files and generating training
//data for an Artificial Neural Network:
public class TrainingParser {
	
	//Instance varables:
	Map<String, int[]> bookInputs; //ANN inputs keyed to book title
	Map<String, int[]> movieOutputs; //ANN outputs keyed to book titles
	Map<String, Integer> keyWordIndex; //Book keywords index
	ArrayList<String> trainingBooks; //Training set
	ArrayList<String> testingBooks; //Testing set
	int inputSize; //ANN input array size
	int outputSize; //ANN output array size
	
	//Constructor method, takes in data filenames and input/output sizes
	//and invokes methods that parse the data files and fill the instance
	//variable data structures:
	public TrainingParser(	String bookDataFilename,
							String movieDataFilename,
							int inputSize,
							int outputSize){
		
		this.bookInputs = new HashMap<>();
		this.movieOutputs = new HashMap<>();
		this.keyWordIndex = new HashMap<>();
		this.trainingBooks = new ArrayList<>();
		this.testingBooks = new ArrayList<>();
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		
		initKeyWordIndex();
		initBookInput(bookDataFilename);
		initMovieOutput(movieDataFilename);
		initTrainingBooks();
	}

	//Helper function for constructor that initializes the book keyword
	//index with hard-coded keyword strings:
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

	//Helper method for constructor that parses the book data file:
	private void initBookInput(String bookDataFilename) {
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(
					bookDataFilename));
			
			String bookLine = reader.readLine();
			
			//Read each line of the book data file:
			while(bookLine != null){
				int i = 0;
				
				//Parse book title:
				String title = "";
				while(bookLine.charAt(i) != ';'){
					title += bookLine.charAt(i);
					i++;
				}
				
				//Associate ANN inputs to book title:
				this.bookInputs.put(title, getInputsFromString(bookLine));
				
				bookLine = reader.readLine();
			}
			reader.close();
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	//Helper function for initBookInput that parses out ANN inputs from a
	//line of the book data file. Iterates through the provided string and
	//expects a specific format with data seperated by semi-colons:
	private int[] getInputsFromString(String bookLine){
		int index = 0; //inputs array index
		int i = 0; //bookLine string index
		int[] inputs = new int[this.inputSize]; //ANN inputs array

		//Skip book title:
		while(bookLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		//Parse bestSeller indicator, takes one index in inputs array:
		String bestSeller = "";
		while(bookLine.charAt(i) != ';'){
			bestSeller += bookLine.charAt(i);
			i++;
		}
		inputs[index] = Integer.parseInt(bestSeller);
		i++;
		index++;
		
		//Parse Amazon rating: 
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
		i++; //Advance past semicolon
		index += 4; //Takes up four indices in inputs array
		
		//Parse Amazon review count:
		String reviews = "";
		while(bookLine.charAt(i) != ';'){
			reviews += bookLine.charAt(i);
			i++;
		}
		int revCount = Integer.parseInt(reviews);
		int revIndex = 31 - Integer.numberOfLeadingZeros(revCount); //Integer log-base-2
		if(revIndex > 14){
			inputs[index + 14] = 1;
		}
		else{
			inputs[index + revIndex] = 1;
		}
		i++; //Advance past semicolon
		index += 15; //Takes up 15 indices in inputs array
		
		//Parse age group:
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
		i++; //Advance past semicolon
		index += 3; //Takes up 3 indices in inputs array
		
		//Parse keywords:
		while(i < bookLine.length()){
			String keyword = "";
			while(bookLine.charAt(i) != ';' && i < bookLine.length()){
				keyword += bookLine.charAt(i);
				i++;
			}
			inputs[index + this.keyWordIndex.get(keyword)] = 1;
			i++; //Advance past semicolon after each keyword
		}
		
		//Return ANN inputs:
		return inputs;
	}
	
	//Helper method for constructor that parses the movie data file:
	private void initMovieOutput(String movieDataFilename) {
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(
					movieDataFilename));
			
			String movieLine = reader.readLine();
			movieLine = reader.readLine(); //Skip first line
			
			//Read each line of the movie data file:
			while(movieLine != null){
				int i = 0;
				
				//Parse book title:
				String bookTitle = "";
				while(movieLine.charAt(i) != ';'){
					bookTitle += movieLine.charAt(i);
					i++;
				}
				
				//Associate ANN outputs to book title:
				this.movieOutputs.put(bookTitle, getOutputsFromString(movieLine));
				
				movieLine = reader.readLine();
			}
			reader.close();
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	//Helper function for initBookInput that parses out ANN inputs from a
	//line of the book data file. Iterates through the provided string and
	//expects a specific format with data seperated by semi-colons:
	private int[] getOutputsFromString(String movieLine){
		int index = 0; //outputs array index
		int i = 0; //movieLine string index
		int[] outputs = new int[this.outputSize]; //ANN outputs array
		
		//Skip book title
		while(movieLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		//Skip movie title
		while(movieLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		//Skip IMDB ID
		while(movieLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		//Parse IMDB rating:
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
		i++; //Advance past semicolon
		index += 9; //Takes up 9 indices of outputs array
		
		//Parse Box Office Revenue:
		String boxOffice = "";
		while(movieLine.charAt(i) != ';'){
			boxOffice += movieLine.charAt(i);
			i++;
		}
		int boxRev = Integer.parseInt(boxOffice)/100000;
		int revIndex = 31 - Integer.numberOfLeadingZeros(boxRev); //Integer log-base-2
		if(revIndex > 14){
			outputs[index + 14] = 1;
		}
		else if(revIndex < 0){
			outputs[index] = 1;
		}
		else{
			outputs[index + revIndex] = 1;
		}
		i++; //Advance past semicolon
		index += 15; //Takes up 15 indices of outputs array
		
		//Parse Production Cost Estimate:
		String prodCost = "";
		while(movieLine.charAt(i) != ';'){
			prodCost += movieLine.charAt(i);
			i++;
		}
		int budget = Integer.parseInt(prodCost)/10000;
		int budIndex = 31 - Integer.numberOfLeadingZeros(budget); //Integer log-base-2
		if(budIndex > 14){
			outputs[index + 14] = 1;
		}
		else if(budIndex < 0){
			outputs[index] = 1;
		}
		else{
			outputs[index + budIndex] = 1;
		}
		i++; //Advance past semicolon
		index += 15; //Takes up 15 indices of outputs array
		
		//Profit Column
		while(movieLine.charAt(i) != ';'){
			i++;
		}
		i++;
		
		//Parse Award Nominations:
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
		
		//Return ANN outputs:
		return outputs;
	}
	
	//Helper method for constructor that creates the training and testing set
	//randomly selecting 80% of the book titles for the training set and 20% 
	//for the testing set:
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


	//Getter methods for retrieving parsed information and training/testing sets:
	
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
