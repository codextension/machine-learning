package io.codextension.algorithm.regression;

import io.codextension.algorithm.regression.data.Feature;
import io.codextension.algorithm.regression.data.XYPair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class LogisticRegression extends AbstractRegression {
	private static final Logger LOG = Logger.getLogger(LogisticRegression.class);

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
		LOG.info("The z = " + log_rounded);
		LOG.info("The (without rounding) z = " + log); // 1 / (1 + (1/e^z)) 2535

		ArrayList<Float> input = new ArrayList<>();
		input.add(1.0f);
        input.add(79.0f);
		// + hypothesis(results, input);
		return "The probability of X1 to be Y=1 is defined by the equation 1/(1+1/e^(" + log_rounded + "))";
	}

    protected float hypothesis(ArrayList<Float> thetas, ArrayList<Float> x) {
        float z = thetas.get(0);
		if (thetas.size() > 1) {
			for (int i = 1; i < thetas.size(); i++) {
				z += thetas.get(i) * x.get(i);
			}
		}

		Double expMinusZ = 1.0 / Math.exp(z);

		Double value = 1.0 / (1.0 + expMinusZ);
		return value.floatValue();
	}
}
