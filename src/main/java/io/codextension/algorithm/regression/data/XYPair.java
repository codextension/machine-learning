package io.codextension.algorithm.regression.data;

import java.util.ArrayList;

public class XYPair {
    private float y;
    private ArrayList<Float> x;

    public XYPair(ArrayList<Float> x, float y) {
		this.x = new ArrayList<>();
		this.x.add(1f); // adding x0 which has always the value 1
		this.x.addAll(x);
		this.y = y;
    }

    public XYPair(String[] xs, float y) {
        this.x = new ArrayList<>();
        this.x.add(1f); // adding x0 which has always the value 1
        for (String x : xs) {
            this.x.add(Float.parseFloat(x));
        }
        this.y = y;
    }

    public ArrayList<Float> getX() {
        return x;
    }

    public void setX(ArrayList<Float> x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "XYPair [x=" + x + ", y=" + y + "]";
    }

}
