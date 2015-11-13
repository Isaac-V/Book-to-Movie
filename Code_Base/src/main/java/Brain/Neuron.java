package Brain;

import java.utils.Map;
import java.utils.HashMap;
import java.utils.ArrayList;

public class Neuron {

    private Map<Neuron, double> dendrites; //Associate input-neuron to weight
    private Map currentPSPs<double, double>; //Store current iteration's inputs with their weights
    private double pspSum;
    
    private List<Neuron> axonTerminals;
    private double output;
    private double errorSignal;
    
    public Neuron(List<Neuron> inputs, List<Neuron> outputs) {
        this.dendrites = new HashMap<>();
        this.currentPSPs = new HashMap<>();
        if(inputs != null){
            for(Neuron neuron : inputs){
                dendrites.put(neuron, 0);
            }
        }
        inputSum = 0;
        
        this.axonTerminals = new ArrayList<>();
        if(outputs != null){
            for(Neuron neuron : outputs){
                axonTerminals.add(neuron);
            }
        }
    }
    
    public Neuron() {
        this.dendrites = new HashMap<>();
        this.currentPSPs = new HashMap<>();
        inputSum = 0;
        
        this.axonTerminals = new ArrayList<>();
    }
    
    public void addAxonTerminal(Neuron neuron){
        if(neuron != null){
            axonTerminals.add(neuron);
        }
    }
    
    public void addDendrite(Neuron neuron){
        if(neuron != null){
            dendrites.add(neuron);
        }
    }
    
    public void pSP(Neuron preSynNeu, double input){
        if(preSynNeu == null){
            pspSum += input;
        }
        else {
            double weight = dendrites.get(preSynNeu)
            pspSum += input * weight;
            currentInputs.put(input, weight);
        }
    }
    
    public void actionPotential(){
        output = 1 / (1 + Math.pow(Math.E, (-1 * pspSum)));
        for(Neuron neuron : axonTerminals){
            neuron.pSP(this, output);
        }
    }

}