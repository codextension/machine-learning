package io.codextension.algorithm.naivebayes;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import io.codextension.algorithm.MapUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by elie on 22.04.17.
 */
@Component
public class NaiveBayes {
	private static final Logger LOG = LoggerFactory.getLogger(NaiveBayes.class.getName());
	Map<String, Integer> countCls = new HashMap<>();
	Double totalCount = 0.0;
	private Map<String, List<String>> data;

	public void init(Map<String, List<String>> data) {
		this.data = data;

		LOG.info("Counting all words per classifier ...");
		for (String cls : this.data.keySet()) {
			totalCount += this.data.get(cls).size();
			countCls.put(cls, this.data.get(cls).size());
		}
	}

	public boolean train() {
		MongoClient mongoClient = new MongoClient("192.168.0.21", 32768);
		MongoDatabase database = mongoClient.getDatabase("naivebayes");
		MongoCollection<Document> occurrences = database.getCollection("occurrences");
		MongoCollection<Document> probabilities = database.getCollection("probabilities");

		probabilities.createIndex(Indexes.text("name"));

		LOG.info("Calculating probabilities of each Classifier ...");
		for (String cls : countCls.keySet()) {
			Document doc = new Document();
			doc.put("name", cls.toLowerCase());
			doc.put("value", countCls.get(cls) / totalCount);
			probabilities.insertOne(doc);
		}

		LOG.info("Counting occurrences of all words ...");
		occurrences.createIndex(Indexes.compoundIndex(Indexes.text("word"), Indexes.text("class")));

		for (String cls : this.data.keySet()) {
			Map<String, Document> mapping = new HashMap<>();
			List<String> values = this.data.get(cls);
			for (String value : values) {
				String[] v = value.split("\\s+");
				for (String m : v) {
					String n = m.replaceAll("[^\\w]", "");
					int c = 1;
					Document doc = new Document();
					doc.put("class", cls);
					doc.put("word", n);
					if (mapping.containsKey(n)) {
						doc = mapping.get(n);
						c += Integer.parseInt(doc.get("count").toString());
					}
					doc.put("count", c);
					mapping.put(n, doc);
				}
			}
			occurrences.insertMany(new ArrayList<Document>(mapping.values()));
		}

		List<String> distinctWords = new ArrayList<>();

		DistinctIterable<String> words = occurrences.distinct("word", String.class);
		List<String> classes = new ArrayList<String>(data.keySet());

		words.forEach(new Block<String>() {
			@Override
			public void apply(final String value) {
				distinctWords.add(value);
			}
		});

		for (String clazz : classes) {
			Double divider = 0.0;
			FindIterable<Document> value = occurrences.find(Filters.eq("class", clazz));
			List<String> validWords = new ArrayList<>();
			for (Document aValue : value) {
				validWords.add(aValue.getString("word"));
				divider += Integer.parseInt(aValue.get("count").toString()) + 1;
			}
			value = occurrences.find(Filters.ne("class", clazz));
			for (Document aValue : value) {
				if (!validWords.contains(aValue.getString("word"))) {
					divider += 1;
				}
			}
			for (String word : distinctWords) {
				String key = word.toLowerCase() + ":" + clazz.toLowerCase();
				distinctWords.indexOf(word);
				value = occurrences.find(Filters.and(Filters.eq("word", word), Filters.eq("class", clazz)));
				Integer countWord = 1;
				if (value.first() != null) {
					countWord = Integer.parseInt(value.first().get("count").toString()) + 1;
				}

				Document doc = new Document();
				doc.put("name", key);
				doc.put("value", countWord / divider);
				probabilities.insertOne(doc);
			}
		}

		mongoClient.close();
		return true;
	}

	public String findClass(String value) {
		MongoClient mongoClient = new MongoClient("192.168.0.21", 32768);
		MongoDatabase database = mongoClient.getDatabase("naivebayes");
		MongoCollection<Document> probabilities = database.getCollection("probabilities");
		MongoCollection<Document> occurrences = database.getCollection("occurrences");

		DistinctIterable<String> classes = occurrences.distinct("class", String.class);
		String[] v = value.split("\\s+");
		Map<String, Double> valueProb = new HashMap<>();
		LOG.info("Calculating probablities for: " + value);
		for (String clazz : classes) {
			Double q = 0d;
			for (String m : v) {
				String n = m.replaceAll("[^\\w]", "") + ":" + clazz;
				FindIterable<Document> x = probabilities.find(Filters.eq("name", n));
				if (x.first() != null) {
					q += Math.log10(x.first().getDouble("value"));
					LOG.info("Propability of " + m + ": " + x.first().getDouble("value") + ", q= " + q);
				}
			} // using https://stats.stackexchange.com/questions/163088/how-to-use-log-probabilities-for-gaussian-naive-bayes

			Double p = probabilities.find(Filters.eq("name", clazz)).first().getDouble("value");

			valueProb.put(clazz, q + Math.log10(p));
			LOG.info("Probability of [" + clazz + ":value] is " + (q + Math.log10(p)));
		}

		mongoClient.close();

		return MapUtils.sortByValue(valueProb, MapUtils.SortOrder.DESCENDING).entrySet().iterator().next().getKey();
	}
}
