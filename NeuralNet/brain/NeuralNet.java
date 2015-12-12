//Mary Moser
//Isaac Vawter

package brain;

import java.lang.Math;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class NeuralNet {
	
	int inputSize;
	int hiddenSize;
	int outputSize;
	ArrayList<Neuron> inputLayer;
	ArrayList<Neuron> hiddenLayer;
	ArrayList<Neuron> outputLayer;
	Map<String, int[]> inputMap;
	Map<String, int[]> outputMap;
	ArrayList<String> trainingSet;
	ArrayList<String> testingSet;
	Map<String, int[]> testOutputs;
	
    public NeuralNet(int inputSize, int hiddenSize, int outputSize){
    	this.inputSize = inputSize;
    	this.hiddenSize = hiddenSize;
    	this.outputSize = outputSize;
    	this.inputLayer = new ArrayList<>();
    	this.hiddenLayer = new ArrayList<>();
    	this.outputLayer = new ArrayList<>();
    	this.testOutputs = new HashMap<>();
    	init();
    }
    
    private void init(){
    	addNeurons(this.inputLayer, this.inputSize);
    	addNeurons(this.hiddenLayer, this.hiddenSize);
    	addNeurons(this.outputLayer, this.outputSize);
    	fullyConnectLayers(this.inputLayer, this.hiddenLayer);
    	fullyConnectLayers(this.hiddenLayer, this.outputLayer);
    }
    
    public void setInputMap(Map<String, int[]> inputMap){
    	this.inputMap = inputMap;
    }
    
    public void setOutputMap(Map<String, int[]> outputMap){
    	this.outputMap = outputMap;
    }
    
    public void setTrainingSet(ArrayList<String> trainingSet){
    	this.trainingSet = trainingSet;
    }
    
    public void setTestingSet(ArrayList<String> testingSet){
    	this.testingSet = testingSet;
    }
	
    private void addNeurons(List<Neuron> layer, int number){
        for(int i = 0; i < number; i++){
            layer.add(new Neuron(0.5));
        }
    }
    
    private void fullyConnectLayers(List<Neuron> preSyn, List<Neuron> postSyn){
        for(Neuron preNeuron : preSyn){
            for(Neuron postNeuron : postSyn){
                preNeuron.addAxonTerminal(postNeuron);
                postNeuron.addDendrite(preNeuron);
            }
        }
    }
    
    private void layerActionPotential(List<Neuron> neurons){
        for(Neuron neuron : neurons){
            neuron.actionPotential();
        }
    }
    
    private void layerBackpropagation(List<Neuron> neurons){
        for(Neuron neuron : neurons){
            neuron.backpropagation();
        }
    }
    
    private void printLayer(List<Neuron> neurons, String title){
        System.out.println(title + ":\n");
        for(Neuron neuron : neurons){
            System.out.println(neuron.toString());
        }
    }
    
    public void train(double errorTHold) {
        
        //Training the Neural Network:
        double avgSqError = 1.0;
        while(avgSqError > errorTHold){
            double errorSum = 0;
            for(String titleKey : this.trainingSet){
            	
            	int[] inputArray = this.inputMap.get(titleKey);
            	int[] outputArray = this.outputMap.get(titleKey);
            	
            	for(int i = 0; i < this.inputSize; i++){
            		this.inputLayer.get(i).incomingAP(null, inputArray[i]);
            	}
                
                layerActionPotential(inputLayer);
                layerActionPotential(hiddenLayer);
                layerActionPotential(outputLayer);
                
                for(int j = 0; j < this.outputSize; j++){
                	Neuron outputNeuron = this.outputLayer.get(j);
                    errorSum += 0.5 * Math.pow(outputArray[j] - outputNeuron.getNeuronOutput(), 2);
                    outputNeuron.setCorrectOutput(outputArray[j]);
                }
                
                //Input Layer doesn't use backpropagation, its inputs/outputs are fixed
                layerBackpropagation(outputLayer);
                layerBackpropagation(hiddenLayer);
            }
            avgSqError = errorSum / (this.trainingSet.size() * this.outputSize);
            
            //Observe the average network error decrease:
            System.out.println(avgSqError);
        }
    }
        
    public void test() {
    	double errorSum = 0;
        for(String titleKey : this.testingSet){
        	
        	int[] inputArray = this.inputMap.get(titleKey);
        	int[] outputArray = this.outputMap.get(titleKey);
        	
        	for(int i = 0; i < this.inputSize; i++){
        		this.inputLayer.get(i).incomingAP(null, inputArray[i]);
        	}
            
            layerActionPotential(inputLayer);
            layerActionPotential(hiddenLayer);
            layerActionPotential(outputLayer);
            
            for(int j = 0; j < this.outputSize; j++){
            	Neuron outputNeuron = this.outputLayer.get(j);
                errorSum += 0.5 * Math.pow(outputArray[j] - outputNeuron.getNeuronOutput(), 2);
            }
            
            int[] prediction = predictedOutput(outputLayer);
            this.testOutputs.put(titleKey, prediction);
            
        }
        double avgSqError = errorSum / (this.testingSet.size() * this.outputSize);
        System.out.println("Testing Error: " + avgSqError);
    }

	private int[] predictedOutput(ArrayList<Neuron> outputLayer) {
		int[] prediction = new int[this.outputSize];
		double localMax = 0;
		int maxIndex = 0;
		for(int i = 0; i < 9; i++){
			if(outputLayer.get(i).getNeuronOutput() > localMax){
				localMax = outputLayer.get(i).getNeuronOutput();
				maxIndex = i;
			}
		}
		prediction[maxIndex] = 1;
		localMax = 0;
		maxIndex = 0;
		for(int i = 9; i < 24; i++){
			if(outputLayer.get(i).getNeuronOutput() > localMax){
				localMax = outputLayer.get(i).getNeuronOutput();
				maxIndex = i;
			}
		}
		prediction[maxIndex] = 1;
		localMax = 0;
		maxIndex = 0;
		for(int i = 24; i < 39; i++){
			if(outputLayer.get(i).getNeuronOutput() > localMax){
				localMax = outputLayer.get(i).getNeuronOutput();
				maxIndex = i;
			}
		}
		prediction[maxIndex] = 1;
		localMax = 0;
		maxIndex = 0;
		for(int i = 39; i < 43; i++){
			if(outputLayer.get(i).getNeuronOutput() > localMax){
				localMax = outputLayer.get(i).getNeuronOutput();
				maxIndex = i;
			}
		}
		prediction[maxIndex] = 1;
		return prediction;
	}
	
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
        
        
        //Print Neural Network and Training Results:
//        System.out.println("\n");
//        printLayer(hiddenLayer, "Hidden Layer");
//        printLayer(outputLayer, "Output Layer");
//        
//        for(ArrayList<Double> inputs : learningSet.keySet()){
//            inputLayer.get(0).incomingAP(null, inputs.get(0));
//            inputLayer.get(1).incomingAP(null, inputs.get(1));
//            
//            layerActionPotential(inputLayer);
//            layerActionPotential(hiddenLayer);
//            layerActionPotential(outputLayer);
//            
//            String results = "Inputs: " + inputs.get(0).intValue();
//            results += ", " + inputs.get(1).intValue();
//            results += " Output: " + outputLayer.get(0).getNeuronOutput();
//            System.out.println(results);
//        }

}