package io.codextension.algorithm.regression.data;

import java.io.Serializable;
import java.util.*;

/**
 * Created by elie on 03.06.17.
 */
public class Feature implements Serializable {

	private String name;
	private List<Data> data;
	private float min;
	private float max;
	private float mean;

	public Feature(String name) {
		this.name = name;
		this.data = new LinkedList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Data> getData() {
		return data;
	}

	public int size() {
		return data.size();
	}

	public void addData(float value) {
		this.data.add(new Data(value));

		ArrayList<Data> sortedData = new ArrayList<>(data);
		Collections.sort(sortedData);
		min = sortedData.get(0).getValue();
		max = sortedData.get(sortedData.size() - 1).getValue();
		mean = 0;
		for (Data d : data) {
			mean += d.getValue();
		}
		mean = mean / size();

		for (Data d : data) {
			d.setScaled((d.getValue() - mean) / (max - min));
		}
	}

	public float getMin() {
		return min;
	}

	public float getMax() {
		return max;
	}

	public float getMean() {
		return mean;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Feature feature = (Feature) o;

		return name != null ? name.equals(feature.name) : feature.name == null;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "Feature {" +
				"name='" + name + '\'' +
				", data=" + Arrays.toString(data.toArray()) +
				'}';
	}
}
