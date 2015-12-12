//Mary Moser
//Isaac Vawter

package brain;

import java.lang.Math;
import java.lang.Double;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Neuron {

	// Instance Variables:
	private Random random;
    private Map<Neuron, Double> dendrites; //Associate input-neuron to weight
    private Map<Neuron, Double> neuronInputs; //Associate input-values to source Neuron
    private boolean inputLayer; //Indicates if this neuron is in the input layer
    private List<Neuron> axonTerminals; //Neurons the output goes to
    private double neuronOutput; //Output from the Neuron
    private double correctOutput; //Corrected output from backpropagation
    
  // Simple constructor method:
    public Neuron() {
    	this.random = new Random();
        this.dendrites = new HashMap<>();
        this.neuronInputs = new HashMap<>();
        this.inputLayer = false;
        this.axonTerminals = new ArrayList<>();
    }
    
    // Complicated constructor method, may not be useful:
    public Neuron(List<Neuron> inputNeurons, List<Neuron> outputNeurons) {
    	this.random = new Random();
        this.dendrites = new HashMap<>();
        this.neuronInputs = new HashMap<>();
        this.inputLayer = false;
        this.axonTerminals = new ArrayList<>();
        if(inputNeurons != null){
            for(Neuron neuron : inputNeurons){
                dendrites.put(neuron, new Double(random.nextDouble()));
            }
        }
        if(outputNeurons != null){
            for(Neuron neuron : outputNeurons){
                axonTerminals.add(neuron);
            }
        }
    }
    
    // Method representing an incoming action potential, i.e. receiving another
    // neuron's output:
    public void incomingAP(Neuron preSynNeu, double input){
        if(preSynNeu == null){
            neuronOutput = input;
            inputLayer = true;
        }
        else {
            neuronInputs.put(preSynNeu, new Double(input));
        }
    }
    
    // Method representing an action potential, i.e. sending output to all
    // axonTerminal neurons. Also initializes the correctOutput:
    public void actionPotential(){
        if(!inputLayer){
            neuronOutput = determineOutput(dendrites, neuronInputs);
        }
        for(Neuron neuron : axonTerminals){
            neuron.incomingAP(this, neuronOutput);
        }
        correctOutput = neuronOutput;
    }
    
    // Method that uses the Sigmoid Function and the  inputs and weights to 
    // determine an output value for the neuron. Also used during backpropagation
    // to find Error derivatives:
    private double determineOutput(Map<Neuron, Double> weights, Map<Neuron, Double> inputs){
        double pspSum = 0;
        for(Neuron neuron : weights.keySet()){
            pspSum += weights.get(neuron) * inputs.get(neuron).doubleValue();
        }
        double output = 1 / (1 + Math.pow(Math.E, -1 * pspSum));
        return output;
    }
    
    // Method that determines the value of the Squared Error Function based on the 
    // inputs and weights:
    private double getError(Map<Neuron, Double> weights, Map<Neuron, Double> inputs){
        return 0.5 * Math.pow(correctOutput - determineOutput(weights, inputs), 2);
    }
    
    // Method that creates a Map of delta-values for either weights or inputs by taking an
    // arithmetic derivative of the Squared Error Function with respect to individual weights  
    // or inputs. Since inputs and weights are symmetrical when determining output, this method 
    // simply looks at values and the calling function needs to be sure that the pspTargetHalf
    // parameter represents the values that deltas need to be calculated for, either input 
    // values or weight values. The term "psp" is used to refer to post-synaptic potentials, in
    // this case represented by input * weight:
    private Map<Neuron, Double> errorUpdates(Map<Neuron, Double> pspTargetHalf, Map<Neuron, Double> pspNonTargetHalf){
        Map<Neuron, Double> deltaValues = new HashMap<>();

        for(Neuron neuron : pspTargetHalf.keySet()){
            double baseValue = pspTargetHalf.get(neuron).doubleValue();
            
            pspTargetHalf.put(neuron, baseValue - 0.00000001);
            double errorPoint1 = getError(pspTargetHalf, pspNonTargetHalf);
            
            pspTargetHalf.put(neuron, baseValue + 0.00000001);
            double errorPoint2 = getError(pspTargetHalf, pspNonTargetHalf);
            
            double deltaValue = (errorPoint2 - errorPoint1) / 0.00000002;
            // System.out.println(deltaValue);
            pspTargetHalf.put(neuron, baseValue);
            deltaValues.put(neuron, deltaValue);
        }
        
        return deltaValues;
    }
    
    // Method that performs backpropagation for this neuron. It uses the delta-values 
    // found by the errorUpdates method to update dendrites and propagates errors to
    // the neurons that send inputs to this neuron:
    public void backpropagation(){
        Map<Neuron, Double> dendriteUpdates = errorUpdates(dendrites, neuronInputs);
        Map<Neuron, Double> inputUpdates = errorUpdates(neuronInputs, dendrites);
        for(Neuron neuron : dendrites.keySet()){
            dendrites.put(neuron, dendrites.get(neuron) - dendriteUpdates.get(neuron));
        }
        for(Neuron neuron : inputUpdates.keySet()){
            neuron.modifyCorrectOutput(inputUpdates.get(neuron));
        }
    }
    
    // Method to add a neuron to the list that this neuron sends its output to:
    public void addAxonTerminal(Neuron neuron){
        if(neuron != null){
            axonTerminals.add(neuron);
        }
    }
    
    // Method to add a neuron to the list that this neuron receives input from,
    // initializes the weight of that neuron's input to a random value:
    public void addDendrite(Neuron neuron){
        if(neuron != null){
            dendrites.put(neuron, new Double(random.nextDouble()));
        }
    }
    
    public double getNeuronOutput(){
        return neuronOutput;
    }
    
    public void setCorrectOutput(double correctOutput){
        this.correctOutput = correctOutput;
    }
    
    public void modifyCorrectOutput(double deltaOutput){
        this.correctOutput -= deltaOutput;
    }
    
    @Override
    public String toString(){
        String neuronString = "Dendrites: ";
        for(Neuron neuron : dendrites.keySet()){
            neuronString += "(Weight=" + dendrites.get(neuron) + ") ";
        }
        neuronString += "\n";
        return neuronString;
    }

}