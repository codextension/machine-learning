package io.codextension.boot.command;

import io.codextension.algorithm.neural.*;
import io.codextension.boot.config.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.*;
import org.springframework.shell.table.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

@ShellComponent
@ShellCommandGroup("Neural Network commands")
public class NeuralNetworkCmds {
    private static final Logger LOG = LoggerFactory.getLogger(NeuralNetworkCmds.class);
    public static final double MATCH_RANGE = 0.1d;

    private boolean trained = false;
    private NeuralNetwork neuralNetwork;
    private int nbHiddenLayers;
    private int sizeHiddenLayer;

    @Autowired
    private CommonCmds commonCmds;

    @PostConstruct
    public void init() {
        neuralNetwork = null;
    }

    public Availability isNeuralNetwork() {
        return commonCmds.getAlgorithm() != null && commonCmds.getAlgorithm() == Algorithm.NEURAL_NETWORK ? Availability.available() : Availability.unavailable("You did not select this algorithm");
    }

    public Availability isTrained() {
        return trained ? Availability.available() : Availability.unavailable("Neural network not trained yet");
    }

    public Availability isConfigured() {
        return neuralNetwork != null ? Availability.available() : Availability.unavailable("Neural Network not configured yet");
    }

    @ShellMethod(key = "configure", value = "Configure the Neural Network")
    @ShellMethodAvailability({"isNeuralNetwork"})
    public String configure(
    		@ShellOption(value = "mode", defaultValue = "SGD", help = "NeuralNetwork mode: SGD, Batch or Mini-Batch") MethodType mode, 
    		@ShellOption(value = "nb", help = "Number of hidden layers") @NotNull int nbHiddenLayers, 
    		@ShellOption(value = "size", help = "Number of neurons in each hidden layer") @Min(1) int sizeHiddenLayer, 
    		@ShellOption(value = "rate", help = "The learning rate") @NotNull Double learningRate, 
    		@ShellOption(value = "withBias", help = "Include a bias neuron") boolean withBias
    		) {
        neuralNetwork = new NeuralNetwork();

        neuralNetwork.setMatchRange(MATCH_RANGE);
        neuralNetwork.setLearningRate(learningRate);
        neuralNetwork.setMethod(mode);
        neuralNetwork.setWithBias(withBias);

        this.sizeHiddenLayer = sizeHiddenLayer;
        this.nbHiddenLayers = nbHiddenLayers;

        return "NeuralNetwork configured correctly";
    }

    private static CellMatcher at(final int theRow, final int col) {
        return new CellMatcher() {
            @Override
            public boolean matches(int row, int column, TableModel model) {
                return row == theRow && column == col;
            }
        };
    }

    @ShellMethod(key = "evaluate", value = "Evaluate against test data")
    @ShellMethodAvailability({"isTrained"})
    public Table evaluate(@ShellOption(value = "file", help = "The file location of the test data") @NotNull File testFile) {

        List<HashMap<ValueType, Double[]>> values = extractData(testFile);
        if (values == null) {
            throw new RuntimeException("Cannot extract data, please check that the file exists and has the right pattern");
        }

        String[][] data = new String[values.size() + 1][3];
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        data[0][0] = "Input";
        data[0][1] = "Expected output";
        data[0][2] = "Evaluated output";

        int x = 1;
        for (HashMap<ValueType, Double[]> value : values) {
            NeuralNetwork neuralNetwork = new NeuralNetwork();
            neuralNetwork.setBrain(this.neuralNetwork.getBrain());

            for (int i = 0; i < value.get(ValueType.INPUT).length; i++) {
                neuralNetwork.getBrain().getInputs().get(i).setValue(value.get(ValueType.INPUT)[i]);
            }

            neuralNetwork.guess();
            for (Double d : value.get(ValueType.INPUT)) {
                data[x][0] = (data[x][0] == null ? "" : data[x][0]) + d.intValue() + "";
            }
            data[x][1] = "";
            data[x][2] = "";
            int i = 0;
            for (Neuron neuron : neuralNetwork.getBrain().getOutputs()) {
                data[x][1] += value.get(ValueType.OUTPUT)[i].intValue() + "";
                data[x][2] += neuron.activationValue() + "";
                i++;
                x++;
            }
            tableBuilder.on(at(x, 0)).addAligner(SimpleHorizontalAligner.values()[0]);
            tableBuilder.on(at(x, 1)).addAligner(SimpleVerticalAligner.values()[1]);
            tableBuilder.on(at(x, 2)).addAligner(SimpleVerticalAligner.values()[2]);
        }
        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
    }

	private List<HashMap<ValueType, Double[]>> extractData(File file) {
		try (Scanner scanner = new Scanner(file)) {
			List<HashMap<ValueType, Double[]>> trainingInstances = new ArrayList<>();
			while (scanner.hasNextLine()) {
				HashMap<ValueType, Double[]> trainingInstance = new HashMap<>();
				String line = scanner.nextLine();
				String[] outputValues = line.split(";")[0].split("");
				String[] inputValues = line.split(";")[1].split("");
				Double[] inputs = new Double[inputValues.length];
				Double[] outputs = new Double[outputValues.length];

				for (int i = 0; i < inputValues.length; i++) {
					inputs[i] = Double.parseDouble(inputValues[i]);
				}
				for (int i = 0; i < outputValues.length; i++) {
					outputs[i] = Double.parseDouble(outputValues[i]);
				}
				trainingInstance.put(ValueType.INPUT, inputs);
				trainingInstance.put(ValueType.OUTPUT, outputs);
				trainingInstances.add(trainingInstance);
			}
			return trainingInstances;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

    @ShellMethod(key = "calibrate", value = "Train the algorithm with the test instances")
    @ShellMethodAvailability("isConfigured")
    public String calibrate(@ShellOption(value = "file", help = "The file location of the training data") @NotNull File trainingFile) {
        List<HashMap<ValueType, Double[]>> trainingInstances = extractData(trainingFile);
        NeuralNetwork neuralNetwork = new NeuralNetwork();

        neuralNetwork.setMatchRange(MATCH_RANGE);
        neuralNetwork.setLearningRate(this.neuralNetwork.getLearningRate());
        neuralNetwork.setMethod(this.neuralNetwork.getMethod());
        neuralNetwork.setWithBias(this.neuralNetwork.isWithBias());

        neuralNetwork.initialize(trainingInstances, nbHiddenLayers, sizeHiddenLayer);

        int think = neuralNetwork.think();
        this.neuralNetwork = neuralNetwork;
        trained = think > 0;
        try {
            BrainUtils.serialize(neuralNetwork.getBrain(), System.getProperty("java.io.tmpdir") + File.separator + "weights.dat");
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
        return "Trained successfully. Number of iterations: " + think;
    }
}
