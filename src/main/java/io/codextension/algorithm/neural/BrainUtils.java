package io.codextension.algorithm.neural;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class BrainUtils {
    /**
     * https://en.wikipedia.org/wiki/Feature_scaling
     *
     * @param xs list of float values not scaled yet
     * @return a scaled list
     */
    public static List<Float> normalise(List<Float> xs) {
        Collections.sort(xs);

        List<Float> scaledFeatures = new LinkedList<>();
        for (Float x : xs) {
            scaledFeatures.add((x - xs.get(0)) / (xs.get(xs.size() - 1) - xs.get(0)));
        }

        return scaledFeatures;
    }

    public static Brain deserialize(String fileUrl) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(new File(fileUrl));
        ObjectInputStream oi = new ObjectInputStream(fi);
        Brain brain = (Brain) oi.readObject();
        oi.close();
        fi.close();

        return brain;
    }

    public static void serialize(Brain brain, String fileUrl) throws IOException {
        FileOutputStream f = new FileOutputStream(new File(fileUrl));
        ObjectOutputStream o = new ObjectOutputStream(f);

        // Write objects to file
        o.writeObject(brain);

        o.close();
        f.close();
    }

    public static Double sigmoid(Double value) {
        return 1.0f / (1 + Math.exp(-value));
        //return Math.tanh(value);
    }

    public static Double derivativeSigmoid(Double value) {
        Double sigmoid = sigmoid(value);
        return sigmoid * (1.0d - sigmoid);
    }
}
