package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermImportance {

    // A HashMap to store the term frequency of each term in each text
    private Map<String, Map<String, Integer>> tf;
    // A HashMap to store the inverse document frequency of each term
    private Map<String, Double> idf;
    // A List of all the texts in the collection
    private List<List<String>> modules;
    // The total number of texts in the collection
    private final int N;

    public TermImportance(List<List<String>> modules) {
        this.tf = new HashMap<>();
        this.idf = new HashMap<>();
        this.modules = modules;
        this.N = modules.size();

        // Calculate the term frequency for each term in each module
        for (List<String> text : modules) {
            Map<String, Integer> tfInText = new HashMap<>();
            for (String term : text) {
                if (!tfInText.containsKey(term)) {
                    tfInText.put(term, 0);
                }
                tfInText.put(term, tfInText.get(term) + 1);
            }
            tf.put(text.toString(), tfInText);
        }
        /*System.out.println(tf);*/

        // Calculate the inverse document frequency for each term
        for (Map<String, Integer> tfInText : tf.values()) {
            for (String term : tfInText.keySet()) {
                if (!idf.containsKey(term)) {
                    idf.put(term, 0.0);
                }
                idf.put(term, idf.get(term) + 1);
            }
        }
        for (String term : idf.keySet()) {
            idf.put(term, Math.log(N / idf.get(term)));
        }
    }
    public Map<String, Map<String, Double>> tfidf() {
        // A HashMap to store the TF-IDF scores for each term in each text
        Map<String, Map<String, Double>> tfidf = new HashMap<>();
        for (String text : tf.keySet()) {
            Map<String, Double> tfidfInText = new HashMap<>();
            for (String term : tf.get(text).keySet()) {
                double tfValue = tf.get(text).get(term);
                double idfValue = idf.get(term);
                tfidfInText.put(term, tfValue * idfValue);
            }
            tfidf.put(text, tfidfInText);
        }
        return tfidf;
    }

    public Map<String, Double> getTFIDF() {
        Map<String, Double> tfidfScores = new HashMap<>();
        for (String text : tf.keySet()) {
            for (String term : tf.get(text).keySet()) {
                double tfValue = tf.get(text).get(term);
                double idfValue = idf.get(term);
                double tfidf = tfValue * idfValue;
                tfidfScores.put(term, tfidf);
            }
        }
        return tfidfScores;
    }

    public Map<String, String> getModules() {
        Map<String, String> textData = new HashMap<>();
        for (int i = 0; i < modules.size(); i++) {
            String key = modules.get(i).get(0) + " " + modules.get(i).get(1) + " " + modules.get(i).get(2);
            String value =  String.join(" ", modules.get(i));
            textData.put(key, value);
        }
        return textData;
    }
}

