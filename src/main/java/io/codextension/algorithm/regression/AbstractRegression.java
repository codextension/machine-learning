package io.codextension.algorithm.regression;

import io.codextension.algorithm.regression.data.Feature;
import io.codextension.algorithm.regression.data.Result;
import io.codextension.algorithm.regression.data.XYPair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;

public abstract class AbstractRegression {
    private static final Logger LOG = Logger.getLogger(AbstractRegression.class);
    private static final float COMPARE_TO = 0.000001f;
	private static final float LAMBDA = 0.0001f;

	protected boolean regularised;
	protected boolean scaled;

	public void setRegularised(boolean regularised) {
		this.regularised = regularised;
	}

	public void setScaled(boolean value) {
		this.scaled = value;
	}


	protected ArrayList<Feature> readDataByFeature(File file) throws URISyntaxException, FileNotFoundException {
		ArrayList<Feature> trainingData = new ArrayList<Feature>();

        Scanner fileScanner = new Scanner(file);

        if (fileScanner.hasNextLine()) {
            String[] featuresArray = fileScanner.nextLine().split("\t");
            int c = 1;
            for (String fa : featuresArray) {
                if (c == featuresArray.length) {
                    trainingData.add(new Result(fa));
                } else {
                    trainingData.add(new Feature(fa));
                }
                c++;
            }
        }

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            String[] data_instance = line.split("\t");
            float y = Float.parseFloat(data_instance[data_instance.length - 1]);

            int c = 1;
            for (Feature f : trainingData) {
                if (f instanceof Result) {
                    f.addData(y);
                } else {
                    f.addData(Float.parseFloat(data_instance[c - 1]));
                }
                c++;
            }
        }
        fileScanner.close();


        return trainingData;
    }

    protected ArrayList<XYPair> toXYPair(ArrayList<? extends Feature> features, boolean scaled) {
        ArrayList<XYPair> xyPairs = new ArrayList<>();

        int c = 0;

        while (c < features.get(0).size()) {
            ArrayList<Float> xs = new ArrayList<>();
            float y = 0f;
            for (Feature feature : features) {
                if (feature.getClass() == Feature.class) {
                    if (scaled) {
                        xs.add(feature.getData().get(c).getScaled());
                    } else {
                        xs.add(feature.getData().get(c).getValue());
                    }
                } else {
                    y = feature.getData().get(c).getValue();
                }
            }
            xyPairs.add(new XYPair(xs, y));
            c++;
        }

        return xyPairs;
    }

    protected ArrayList<Float> calculateGradientDescent(ArrayList<Float> thetas, ArrayList<XYPair> xy_array, float alpha) {
        ArrayList<Float> temp_thetas = new ArrayList<>();
        ArrayList<Float> deltas = new ArrayList<>();
        int counter = 0;
        LOG.info("Starting the iterations ...");
        while (!isDeltaEquals(deltas, COMPARE_TO)) {
			temp_thetas = costFunction(thetas, xy_array, alpha);
			deltas.clear();
            for (int i = 0; i < thetas.size(); i++) {
                deltas.add(Math.abs(thetas.get(i) - temp_thetas.get(i)));
            }

            thetas = new ArrayList<>(temp_thetas);
            temp_thetas.clear();
            counter++;
            LOG.debug("Iteration " + counter + "\t ,Thetas so far: " + thetas + "\t, Delta: " + deltas);
        }

        LOG.info("The gradient descent found a result after " + counter + " iterations");
        return thetas;
    }

	private ArrayList<Float> costFunction(ArrayList<Float> thetas, ArrayList<XYPair> xy_array, float alpha) {
		ArrayList<Float> temp_thetas = new ArrayList<>();
		ArrayList<Float> summation_thetas = new ArrayList<>();

		for (XYPair xy : xy_array) {
			for (int i = 0; i < xy.getX().size(); i++) {
                if (summation_thetas.size() == i) summation_thetas.add(0f);
                summation_thetas.set(i, summation_thetas.get(i) + (hypothesis(thetas, xy.getX()) - xy.getY()) * xy.getX().get(i));
            }
        }

        BigDecimal division = new BigDecimal(alpha);
		float regularisation = 0f;
		float div = division.divide(new BigDecimal(xy_array.size()), new MathContext(4)).floatValue();
		for (int i = 0; i < summation_thetas.size(); i++) {
			if (regularised) {
				regularisation = (LAMBDA * thetas.get(i));
			} else {
				regularisation = 0.0f;
			}
			temp_thetas.add(thetas.get(i) - (div * (summation_thetas.get(i) + regularisation)));
		}

		return temp_thetas;
	}

    private boolean isDeltaEquals(ArrayList<Float> deltas, float compareTo) {
        boolean result = true;

        if (deltas.size() == 0) return false;
        for (Float d : deltas) {
            result = result && d < compareTo;
        }

        return result;
    }

    protected abstract float hypothesis(ArrayList<Float> thetas, ArrayList<Float> x);

	public abstract String evaluate(File dataFile) throws FileNotFoundException, URISyntaxException;
}
