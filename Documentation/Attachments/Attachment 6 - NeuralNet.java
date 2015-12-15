// CMPSCI 383 (Artificial Intelligence)
// Mary Moser (29154085), Isaac Vawter (28277700)

package brain;

import java.lang.Math;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

//NeuralNet class representing an Artificial Neural Network (ANN) with supervised
//learning functionality.
public class NeuralNet {
	
	//Instance Variables:
	int inputSize; //Input Layer Size
	ArrayList<Neuron> inputLayer; //Input Layer
	int outputSize; //Output Layer Size
	ArrayList<Neuron> outputLayer; //Output Layer
	ArrayList<Integer> hiddenLayerSizes; //List representing the size and number of hidden layers.
	ArrayList<ArrayList<Neuron>> hiddenLayers; //Hidden Layers
	double learningRate; //Learning rate for all neurons in the ANN
	Map<String, int[]> inputMap; //Input Data mapped to a String Key
	Map<String, int[]> outputMap; //Output Data mapped to a String Key
	ArrayList<String> trainingSet; //List of String Keys to be used for training
	ArrayList<String> testingSet; //List of String Keys to be used for testing
	Map<String, int[]> testOutputs; //Outputs of testing
	
	//Constructor method, takes in parameters for ANN layer size and learning rate:
    public NeuralNet(	int inputSize, 
    					ArrayList<Integer> hiddenLayerSizes,
    					int outputSize, 
    					double learningRate){
    	this.inputSize = inputSize;
    	this.hiddenLayerSizes = hiddenLayerSizes;
    	this.outputSize = outputSize;
    	this.learningRate = learningRate;
    	this.inputLayer = new ArrayList<>();
    	this.hiddenLayers = new ArrayList<>();
    	this.outputLayer = new ArrayList<>();
    	this.testOutputs = new HashMap<>();
    	init();
    }
    
    //Method for adding neurons to layers and connecting them:
    private void init(){
    	
    	//Add neurons to layers:
    	addNeurons(this.inputLayer, this.inputSize);
    	addNeurons(this.outputLayer, this.outputSize);
    	for(int i = 0; i < this.hiddenLayerSizes.size(); i++){
    		ArrayList<Neuron> hiddenLayer = new ArrayList<>();
    		addNeurons(hiddenLayer, this.hiddenLayerSizes.get(i));
    		this.hiddenLayers.add(hiddenLayer);
    	}
    	
    	//Connect layers:
    	for(int j = 0; j < this.hiddenLayers.size(); j++){
    		if(j == 0){
    			fullyConnectLayers(this.inputLayer, this.hiddenLayers.get(j));
    		}
    		if(j == (this.hiddenLayers.size()-1)){
    			fullyConnectLayers(this.hiddenLayers.get(j), this.outputLayer);
    		}
    		else{
    			fullyConnectLayers(this.hiddenLayers.get(j), this.hiddenLayers.get(j + 1));
    		}
    	}
    }
    
    //Helper method for init, adds a specified number of neurons to the provided ANN layer:
    private void addNeurons(List<Neuron> layer, int number){
        for(int i = 0; i < number; i++){
            layer.add(new Neuron(this.learningRate));
        }
    }
    
    //Helper method for init, connects every neuron in the first layer to every neuron in
    //the second layer, with the first layer being dendrites and the second layer being
    //axon terminals:
    private void fullyConnectLayers(List<Neuron> preSyn, List<Neuron> postSyn){
        for(Neuron preNeuron : preSyn){
            for(Neuron postNeuron : postSyn){
                preNeuron.addAxonTerminal(postNeuron);
                postNeuron.addDendrite(preNeuron);
            }
        }
    }
    
    //Method for setting the inputs for supervised learning:
    public void setInputMap(Map<String, int[]> inputMap){
    	this.inputMap = inputMap;
    }
    
    //Method for setting the outputs for supervised learning:
    public void setOutputMap(Map<String, int[]> outputMap){
    	this.outputMap = outputMap;
    }
    
    //Method for setting the training set for supervised learning:
    public void setTrainingSet(ArrayList<String> trainingSet){
    	this.trainingSet = trainingSet;
    }
    
    //Method for setting the testing set for supervised learning:
    public void setTestingSet(ArrayList<String> testingSet){
    	this.testingSet = testingSet;
    }

    //Method for training the ANN using the provided input map and training set until
    //a specified error threshold is reached. Each set of inputs indicated by the 
    //training set is applied to the neural network. The resulting outputs are then
    //compared to correct outputs, squared error is calculated, and backpropagation
    //is invoked. After the whole training set has gone through the ANN, the average
    //squared error is calculated to see if another iteration is required.
    public void train(double errorTHold) {
        
        //Training the Neural Network:
        double avgSqError = 1.0;
        while(avgSqError > errorTHold){
        	
        	//Total of squared errors:
            double errorSum = 0;
            
            //Training Iterations:
            for(String titleKey : this.trainingSet){
            	
            	//Inputs and outputs:
            	int[] inputArray = this.inputMap.get(titleKey);
            	int[] outputArray = this.outputMap.get(titleKey);
            	
            	//Prepare input layer:
            	for(int i = 0; i < this.inputSize; i++){
            		this.inputLayer.get(i).incomingAP(null, inputArray[i]);
            	}
                
            	//Generate outputs:
                layerActionPotential(inputLayer);
                for(int j = 0; j < this.hiddenLayers.size(); j++){
                	layerActionPotential(this.hiddenLayers.get(j));
                }
                layerActionPotential(outputLayer);
                
                //Calculate squared errors and set correct outputs at output layer to prepare for 
                //backpropagation:
                for(int k = 0; k < this.outputSize; k++){
                	Neuron outputNeuron = this.outputLayer.get(k);
                    errorSum += Math.pow(outputArray[k] - outputNeuron.getNeuronOutput(), 2);
                    outputNeuron.setCorrectOutput(outputArray[k]);
                }
                
                //Invoke backpropagation, input Layer doesn't use backpropagation its inputs/outputs 
                //are fixed:
                layerBackpropagation(outputLayer);
                for(int m = (this.hiddenLayers.size()-1); m >= 0; m--){
                	layerBackpropagation(this.hiddenLayers.get(m));
                }

            }
            
            //Calculate average squared error:
            avgSqError = errorSum / (this.trainingSet.size() * this.outputSize);
            
            //Observe the average network error decrease:
            System.out.println(avgSqError);
        }
    }
    
    //Method for testing the ANN using the provided input map and testing set. Each set 
    //of inputs indicated by the training set is applied to the neural network. The resulting 
    //outputs are then compared to correct outputs, squared error is calculated, and predicted 
    //outputs are stored. After the whole testing set has gone through the ANN, the average
    //squared error is calculated and displayed.
    public void test() {
    	
    	//Total of squared errors:
    	double errorSum = 0;
    	
    	//Testing Iterations:
        for(String titleKey : this.testingSet){
        	
        	//Inputs and outputs:
        	int[] inputArray = this.inputMap.get(titleKey);
        	int[] outputArray = this.outputMap.get(titleKey);
        	
        	//Prepare input layer:
        	for(int i = 0; i < this.inputSize; i++){
        		this.inputLayer.get(i).incomingAP(null, inputArray[i]);
        	}
            
        	//Generate outputs:
            layerActionPotential(inputLayer);
            for(int j = 0; j < this.hiddenLayers.size(); j++){
            	layerActionPotential(this.hiddenLayers.get(j));
            }
            layerActionPotential(outputLayer);
            
            //Calculate squared errors:
            for(int k = 0; k < this.outputSize; k++){
            	Neuron outputNeuron = this.outputLayer.get(k);
                errorSum += Math.pow(outputArray[k] - outputNeuron.getNeuronOutput(), 2);
            }
            
            //Generate and store prediction:
            int[] prediction = predictedOutput(outputLayer);
            this.testOutputs.put(titleKey, prediction);
            
        }
        
        //Calculate and display average squared error:
        double avgSqError = errorSum / (this.testingSet.size() * this.outputSize);
        System.out.println("Testing Error: " + avgSqError);
    }
    
    //Helper method for training and testing. Fires action potentials from all neurons in the
    //provided layer:
    private void layerActionPotential(List<Neuron> neurons){
        for(Neuron neuron : neurons){
            neuron.actionPotential();
        }
    }
    
    //Helper method for training. Invokes backpropagation in all neurons in the provided layer:
    private void layerBackpropagation(List<Neuron> neurons){
        for(Neuron neuron : neurons){
            neuron.backpropagation();
        }
    }

    //Helper method for testing. Generates a prediction based on the local max of sub-categories
    //in the ANN output. This function uses hard-coded indices based on the book inputs for our
    //project:
	private int[] predictedOutput(ArrayList<Neuron> outputLayer) {
		int[] prediction = new int[this.outputSize];

		prediction[localMaxOutput(0, 8)] = 1;
		
		prediction[localMaxOutput(9, 23)] = 1;
		
		prediction[localMaxOutput(24, 38)] = 1;
		
		prediction[localMaxOutput(39, 43)] = 1;
		
		return prediction;
	}
	
	//Helper method or predictedOutput, finds the local maximum in the output layer within a 
	//sub-categories indices. Returns the index of the local maximum. Since the outputs for our
	//project are mutually exclusive within each sub-category, only the highest is taken as output. 
	private int localMaxOutput(int start, int end){
		int localMaxIndex = 0;
		double localMaxVal = 0;
		for(int i = start; i <= end; i++){
			if(this.outputLayer.get(i).getNeuronOutput() > localMaxVal){
				localMaxVal = this.outputLayer.get(i).getNeuronOutput();
				localMaxIndex = i;
			}
		}
		return localMaxIndex;
	}
	
	//Prints out the predicted outputs from testing and the actual outputs for comparison:
	public void printPredictionComp(){
		for(String s : this.testOutputs.keySet()){
			System.out.print(s + " | ");
			for(int i = 0; i < this.outputSize; i++){
				System.out.print(this.testOutputs.get(s)[i]);
			}
			System.out.println("");
			
			System.out.print(s + " | ");
			for(int i = 0; i < this.outputSize; i++){
				System.out.print(this.outputMap.get(s)[i]);
			}
			System.out.println("");
		}
	}
	
	//Finds and displays the standard deviation of prediction for each sub-category:
	public void printPredictionError(){
		
		//Sub-category offset totals by distance from correct output:
		int[] ratingOffsets = new int[9];
		int[] boxOffsets = new int[15];
		int[] prodOffsets = new int[15];
		int[] nomOffsets = new int[5];
		
		//Iterate through all predictions:
		for(String s : this.testingSet){
			
			//Predicted and correct output:
			int[] predOut = this.testOutputs.get(s);
			int[] actualOut = this.outputMap.get(s);
			
			//Offset for rating sub-category:
			int predIndex = localFlaggedIndex(predOut, 0, 8);
			int correctIndex = localFlaggedIndex(actualOut, 0, 8);
			ratingOffsets[Math.abs(predIndex - correctIndex)]++;
			
			//Offset for box office revenue sub-category:
			predIndex = localFlaggedIndex(predOut, 9, 23);
			correctIndex = localFlaggedIndex(actualOut, 9, 23);
			boxOffsets[Math.abs(predIndex - correctIndex)]++;
			
			//Offset for production costs sub-category:
			predIndex = localFlaggedIndex(predOut, 24, 38);
			correctIndex = localFlaggedIndex(actualOut, 24, 38);
			prodOffsets[Math.abs(predIndex - correctIndex)]++;
			
			//Offset for award nominations sub-category:
			predIndex = localFlaggedIndex(predOut, 39, 43);
			correctIndex = localFlaggedIndex(actualOut, 39, 43);
			nomOffsets[Math.abs(predIndex - correctIndex)]++;
		}
		
		//Display standard deviations:
		printOffsetError("Rating Offset", ratingOffsets);
		printOffsetError("BoxOffice Offset", boxOffsets);
		printOffsetError("ProdCost Offset", prodOffsets);
		printOffsetError("Noms Offset", nomOffsets);
	}
	
	//Helper method for printPredictionError, finds the index of the provided array
	//within sub-category indices that is marked with a 1:
	private int localFlaggedIndex(int[] array, int start, int end){
		for(int i = start; i <= end; i++){
			if(array[i] == 1) return i;
		}
		return -1;
	}
	
	//Helper method for printPredictionError, calculates and displays sub-category 
	//standard deviation:
	private void printOffsetError(	String s, 
									int[] offsets){
		
		//Calculate standard deviation:
		double sqTotal = 0;
		for(int i = 0; i < offsets.length; i++){
			sqTotal += i*i*offsets[i];
		}
		double stdDev = Math.sqrt(sqTotal/(this.testingSet.size()-1));
		
		//Display sub-category standard deviation:
		System.out.print(s + " | ");
		System.out.println("Std. Dev: " + stdDev);
	}
        

}