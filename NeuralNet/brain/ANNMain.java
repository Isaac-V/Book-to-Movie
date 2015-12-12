package main;

import java.util.ArrayList;
import java.util.Map;

import brain.NeuralNet;
import brain.TrainingParser;

public class ANNMain {
	
	
	public static void main(String[] args) {
		TrainingParser parser = new TrainingParser(
				"C:\\Users\\Isaac\\Desktop\\BookDataFinal.csv",
				"C:\\Users\\Isaac\\Desktop\\MovieDataFinal.csv",
				43, 44);
		
		Map<String, int[]> bookInputs = parser.getBookInputs();
		Map<String, int[]> movieOutputs = parser.getMovieOutputs();
		
		ArrayList<String> trainingSet = parser.getTrainingBooks();
		ArrayList<String> testingSet = parser.getTestingBooks();
		
		NeuralNet ann = new NeuralNet(43, 10, 44);
		ann.setInputMap(bookInputs);
		ann.setOutputMap(movieOutputs);
		ann.setTrainingSet(trainingSet);
		ann.setTestingSet(testingSet);
		
		ann.train(0.025);
		ann.test();
		ann.printPredictionComp();

	}
	
	
	
}


