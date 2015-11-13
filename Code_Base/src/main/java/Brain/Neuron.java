package Brain;

import java.utils.Map;
import java.utils.HashMap;
import java.utils.ArrayList;

public class Neuron {

    private Map<Neuron, double> dendrites;
    private List<Neuron> axonTerminals;
    private double inputSum;
    private double output;
    
    public Neuron(List<Neuron> inputs, List<Neuron> outputs) {
        this.dendrites = new HashMap<>();
        this.axonTerminals = new ArrayList<>();
        inputSum = 0;
        output = 0;
        if(inputs != null){
            for(Neuron neuron : inputs){
                dendrites.put(neuron, 0);
            }
        }
        if(outputs != null){
            for(Neuron neuron : outputs){
                axonTerminals.add(neuron);
            }
        }
    }
    
    public Neuron() {
        this.dendrites = new HashMap<>();
        this.axonTerminals = new ArrayList<>();
        inputSum = 0;
        output = 0;
        threshold = 0;
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
    
    public void postSynPotential(Neuron preSynNeu, double potential){
        if(preSynNeu == null){
            inputSum += potential;
        }
        else {
            inputSum += potential * dendrites.get(preSynNeu);
        }
    }
    
    public void actionPotential(){
        output = 1 / (1 + Math.pow(Math.E, (-1 * inputSum)));
        for(Neuron neuron : axonTerminals){
            neuron.postSynPotential(this, output);
        }
    }

}