package io.codextension.algorithm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by elie on 22.04.17.
 */
public class Data {
	private LinkedHashMap<String, List<String>> classData;

    public Data() {
		classData = new LinkedHashMap<>();
	}
	public boolean addClass(String value) {
		if (classData.containsKey(value)) {
			return false;
		}
		classData.put(value, new ArrayList<>());
		return true;
	}

	public void addTrainingData(String clazz, String data) {
		addClass(clazz);
		classData.get(clazz).add(data);
	}

	public void assignTrainingData(String clazz, List<String> data) {
		classData.put(clazz, data);
	}

	public LinkedHashMap<String, List<String>> get() {
		return classData;
	}
}
