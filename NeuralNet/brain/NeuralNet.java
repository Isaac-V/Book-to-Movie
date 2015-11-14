//Mary Moser
//Isaac Vawter

package brain;

import java.lang.Math;
import java.lang.Double;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class NeuralNet {
    
    private static void addNeurons(List<Neuron> layer, int number){
        for(int i = 0; i < number; i++){
            layer.add(new Neuron());
        }
    }
    
    private static void fullyConnectLayers(List<Neuron> preSyn, List<Neuron> postSyn){
        for(Neuron preNeuron : preSyn){
            for(Neuron postNeuron : postSyn){
                preNeuron.addAxonTerminal(postNeuron);
                postNeuron.addDendrite(preNeuron);
            }
        }
    }
    
    private static void layerActionPotential(List<Neuron> neurons){
        for(Neuron neuron : neurons){
            neuron.actionPotential();
        }
    }
    
    private static void layerBackpropagation(List<Neuron> neurons){
        for(Neuron neuron : neurons){
            neuron.backpropagation();
        }
    }
    
    private static void printLayer(List<Neuron> neurons, String title){
        System.out.println(title + ":\n");
        for(Neuron neuron : neurons){
            System.out.println(neuron.toString());
        }
    }
    
    public static void main(String[] args) {
    	
    	//Initialize neural network:
    	ArrayList<Neuron> inputLayer = new ArrayList<>();
    	ArrayList<Neuron> hiddenLayer = new ArrayList<>();
    	ArrayList<Neuron> outputLayer = new ArrayList<>();
    	
        addNeurons(inputLayer, 2);
        addNeurons(hiddenLayer, 4);
        addNeurons(outputLayer, 1);
        
        fullyConnectLayers(inputLayer, hiddenLayer);
        fullyConnectLayers(hiddenLayer, outputLayer);
        
        
        //Initialize Learning Set:
        Map<ArrayList<Double>, Double> learningSet = new HashMap<>();
        
        ArrayList<Double> list1 = new ArrayList<>();
        list1.add(0.0);
        list1.add(0.0);
        
        ArrayList<Double> list2 = new ArrayList<>();
        list2.add(0.0);
        list2.add(1.0);
        
        ArrayList<Double> list3 = new ArrayList<>();
        list3.add(1.0);
        list3.add(0.0);
        
        ArrayList<Double> list4 = new ArrayList<>();
        list4.add(1.0);
        list4.add(1.0);
        
        learningSet.put(list1, 0.0);
        learningSet.put(list2, 1.0);
        learningSet.put(list3, 1.0);
        learningSet.put(list4, 0.0);
        
        //Training the Neural Network:
        double avgSqError = 1.0;
        while(avgSqError > 0.00001){
            double errorSum = 0;
            for(ArrayList<Double> inputs : learningSet.keySet()){
                inputLayer.get(0).incomingAP(null, inputs.get(0));
                inputLayer.get(1).incomingAP(null, inputs.get(1));
                
                layerActionPotential(inputLayer);
                layerActionPotential(hiddenLayer);
                layerActionPotential(outputLayer);
                
                for(Neuron outputNeuron : outputLayer){
                    errorSum += 0.5 * Math.pow(learningSet.get(inputs) - outputNeuron.getNeuronOutput(), 2);
                    outputNeuron.setCorrectOutput(learningSet.get(inputs));
                }
                
                //Input Layer doesn't use backpropagation, its inputs/outputs are fixed
                layerBackpropagation(outputLayer);
                layerBackpropagation(hiddenLayer);
            }
            avgSqError = errorSum / learningSet.keySet().size();
            
            //Observe the average network error decrease:
            System.out.println(avgSqError);
        }
        
        //Print Neural Network and Training Results:
        System.out.println("\n");
        printLayer(hiddenLayer, "Hidden Layer");
        printLayer(outputLayer, "Output Layer");
        
        for(ArrayList<Double> inputs : learningSet.keySet()){
            inputLayer.get(0).incomingAP(null, inputs.get(0));
            inputLayer.get(1).incomingAP(null, inputs.get(1));
            
            layerActionPotential(inputLayer);
            layerActionPotential(hiddenLayer);
            layerActionPotential(outputLayer);
            
            String results = "Inputs: " + inputs.get(0).intValue();
            results += ", " + inputs.get(1).intValue();
            results += " Output: " + outputLayer.get(0).getNeuronOutput();
            System.out.println(results);
        }
    }
}