package io.codextension.algorithm.regression;

import io.codextension.algorithm.regression.data.Feature;
import io.codextension.algorithm.regression.data.XYPair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * @author elie
 */
public class LinearRegression extends AbstractRegression {
	private static final Logger LOG = Logger.getLogger(LinearRegression.class);

	public String evaluate(File dataFile) throws FileNotFoundException, URISyntaxException {
		ArrayList<Feature> value = readDataByFeature(dataFile);
		ArrayList<XYPair> data = toXYPair(value, this.scaled);

        ArrayList<Float> thetas = new ArrayList<>();
        for (Float n : data.get(0).getX()) {
            thetas.add(0f);
        }
        ArrayList<Float> results = calculateGradientDescent(thetas, data, 0.0007f);

        String log = "";
        String log_rounded = "";

        for (int i = 1; i < results.size(); i++) {
            log += results.get(i) + "*x" + i + " + ";
            log_rounded += Math.round(results.get(i) * 1000.0) / 1000.0 + "*x" + i + " + ";
        }
        log += results.get(0);
        log_rounded += Math.round(results.get(0) * 1000.0) / 1000.0;
		String output = "The gradient descent linear function is y=" + log_rounded;
		output += "\n" + "The gradient descent linear function is (without rounding) y=" + log;
		return output;
	}

    protected float hypothesis(ArrayList<Float> thetas, ArrayList<Float> x) {
        float value = thetas.get(0);
		if (thetas.size() > 1) {
			for (int i = 1; i < thetas.size(); i++) {
				value += thetas.get(i) * x.get(i);
			}
		}
		return value;
	}

}
