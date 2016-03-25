//Mary Moser
//Isaac Vawter

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import brain.NeuralNet;
import brain.TrainingParser;

//Main class for running our project's ANN:
public class ANNMain {
	
	//Main method:
	public static void main(String[] args) {
		
		//Define input/output layer sizes and learning rate:
		int inputLayerSize = 43;
		int outputLayerSize = 44;
		double learningRate = 0.25;
		
		//Parse hard-coded data files:
		TrainingParser parser = new TrainingParser(
				System.getProperty("user.dir") + File.separator + "BookDataFinal.csv",
				System.getProperty("user.dir") + File.separator + "MovieDataFinal.csv",
				inputLayerSize,
				outputLayerSize);
		
		//Get inputs, outputs, training, and testing sets from parser:
		Map<String, int[]> bookInputs = parser.getBookInputs();
		Map<String, int[]> movieOutputs = parser.getMovieOutputs();
		ArrayList<String> trainingSet = parser.getTrainingBooks();
		ArrayList<String> testingSet = parser.getTestingBooks();
		
		//Define sizes of each hidden layer:
		ArrayList<Integer> hiddenLayerSizes = new ArrayList<>();
		hiddenLayerSizes.add(12);
		
		//Initiallize ANN:
		NeuralNet ann = new NeuralNet(	inputLayerSize, 
										hiddenLayerSizes, 
										outputLayerSize, 
										learningRate);
		ann.setInputMap(bookInputs);
		ann.setOutputMap(movieOutputs);
		ann.setTrainingSet(trainingSet);
		ann.setTestingSet(testingSet);
		
		//Conduct supervised training:
		ann.train(0.0674);
		
		//Test ANN and display standard deviations:
		ann.test();
		ann.printPredictionError();
	}
	
	
	
}


