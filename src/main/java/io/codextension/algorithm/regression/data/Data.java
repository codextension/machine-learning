package io.codextension.algorithm.regression.data;

/**
 * Created by elie on 03.06.17.
 */
public class Data implements Comparable<Data> {
	private float value;
	private float scaled;

	public Data(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getScaled() {
		return scaled;
	}

	void setScaled(float scaled) {
		this.scaled = scaled;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Data data = (Data) o;

		return Float.compare(data.value, value) == 0;
	}

	@Override
	public int hashCode() {
		return (value != +0.0f ? Float.floatToIntBits(value) : 0);
	}

	@Override
	public String toString() {
		return "Data {" +
				"value=" + value +
				", scaled=" + scaled +
				'}';
	}

	@Override
	public int compareTo(Data o) {
		return this.value >= o.getValue() ? 1 : -1;
	}
}
