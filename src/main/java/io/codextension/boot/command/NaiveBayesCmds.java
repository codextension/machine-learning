package io.codextension.boot.command;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.codextension.algorithm.Data;
import io.codextension.algorithm.naivebayes.NaiveBayes;
import io.codextension.boot.config.Algorithm;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.*;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by elie on 25.04.17.
 */
@ShellComponent
@ShellCommandGroup("Naive Bayes commands")
public class NaiveBayesCmds {
    private Data dataTrainer = new Data();
	private boolean trained = false;

	@Autowired
	private CommonCmds commonCmds;

	@Autowired
    private Collection<NaiveBayes> algorithms;

    public Availability isNaiveBayes() {
        return commonCmds.getAlgorithm() != null && commonCmds.getAlgorithm() == Algorithm.NAIVE_BAYES ? Availability.available() : Availability.unavailable("You did not select this algorithm");
    }

    public Availability isTrained() {
		return isNaiveBayes();
	}


    @ShellMethod(key = "read", value = "Train the algorithm with the test data")
    @ShellMethodAvailability("isNaiveBayes")
    public String naiveBayes(@ShellOption(value = "class", help = "class name of the training data") @NotNull String cls, @ShellOption(value = "file", help = "The file location of the training data") @NotNull File trainingDataPath) {

		dataTrainer.addClass(cls);

		try {

			if (trainingDataPath.isDirectory()) {
				Stream<Path> files = Files.list(trainingDataPath.toPath());
				files.forEach(new Consumer<Path>() {
					@Override
					public void accept(Path path) {
						try {
							Scanner trainingData = new Scanner(path);
							while (trainingData.hasNextLine()) {
								dataTrainer.addTrainingData(cls, trainingData.nextLine().trim());
							}
						} catch (IOException ex) {
						}
					}
				});
			} else {
				Scanner trainingData = new Scanner(trainingDataPath);
				while (trainingData.hasNextLine()) {
					dataTrainer.addTrainingData(cls, trainingData.nextLine().trim());
				}
			}
		} catch (IOException e) {
			return "Cannot load the properties";
		}
		return "training data added with success";
	}

    @ShellMethod(key = "train", value = "Train the algorithm with the test data")
    @ShellMethodAvailability("isNaiveBayes")
    public String train() {
        NaiveBayes naiveBayes = algorithms.iterator().next();
		naiveBayes.init(dataTrainer.get());
		trained = naiveBayes.train();
		return "Data trained";
	}

    @ShellMethod(key = "reset", value = "Reset and clear all data")
    @ShellMethodAvailability("isNaiveBayes")
    public void reset() {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("naivebayes");
		MongoCollection<Document> occurrences = database.getCollection("occurrences");
		MongoCollection<Document> probabilities = database.getCollection("probabilities");

		occurrences.deleteMany(new Document());
		probabilities.deleteMany(new Document());

		mongoClient.close();
        dataTrainer = new Data();
		trained = false;

	}

    @ShellMethod(key = "findClass", value = "Evaluate the text against the trained data")
    @ShellMethodAvailability({"isTrained"})
    public String find(@ShellOption(value = "value", help = "String to evaluate") String value,
                       @ShellOption(value = "file", help = "File containing the string to evaluate") File file) {
        NaiveBayes naiveBayes = algorithms.iterator().next();
		if (value == null) {
			try {
				if (file.isDirectory()) {
					return file.getName() + " is not a file!";
				} else {
					Scanner trainingData = new Scanner(file);
					value = "";
					while (trainingData.hasNextLine()) {
						value += trainingData.nextLine().trim() + " ";
					}
					return "The class of the data provided is: " + naiveBayes.findClass(value);
				}
			} catch (IOException ex) {
				return ex.getMessage();
			}
		} else {
			return "The class of the data provided is: " + naiveBayes.findClass(value);
		}
	}

    @ShellMethod(key = "accuracy", value = "Evaluate the text against the trained data")
    @ShellMethodAvailability("isTrained")
    public String accuracy(@ShellOption(value = "file", help = "Directory containing all test data") @NotNull File file,
                           @ShellOption(value = "test", help = "What to expect") @NotNull String test) {
        NaiveBayes naiveBayes = algorithms.iterator().next();
		try {
			if (file.isDirectory()) {
				List<Double> c = new ArrayList<>();
				c.add(0.0);
				c.add(0.0);
				Stream<Path> files = Files.list(file.toPath());
				files.forEach(new Consumer<Path>() {
					@Override
					public void accept(Path path) {
						try {
							c.set(0, c.get(0) + 1.0);
							Scanner trainingData = new Scanner(path);
							String value = "";
							while (trainingData.hasNextLine()) {
								value += trainingData.nextLine().trim() + " ";
							}
							String result = naiveBayes.findClass(value);
							if (result.equals(test)) {
								c.set(1, c.get(1) + 1.0);
							}
						} catch (IOException ex) {
						}
					}
				});
				return "The accuracy for " + test + " is: " + 100 * (c.get(1) / c.get(0)) + "%";
			} else {
				return "You should provide a directory";
			}
		} catch (IOException ex) {
			return ex.getMessage();
		}
	}


}
