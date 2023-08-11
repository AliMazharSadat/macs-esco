package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataCleaning {

    public static List<String> removeSymbols(List<String> modules) {
        List<String> without_symbols = new ArrayList<>();

        for (String module : modules) {
            without_symbols.add(
                    module.replaceAll("[^a-zA-Z\\s]", "").replaceAll("[\\n|\\r]", "")
            );
        }

        return without_symbols;
    }

    public static List<List<String>> createTokens(List<String> modules) {
        List<List<String>> tokens = new ArrayList<List<String>>();
        List<String> temp = new ArrayList<>();
        String[] tokenized = null;
        String test;
        for (String module : modules) {
            tokenized = module.split(" +");
            Collections.addAll(temp, tokenized);
            tokens.add(temp);
            temp = new ArrayList<>();
        }
        return tokens;
    }

    public static List<List<String>> stopWordElimination(List<List<String>> modules) throws IOException {
        FileReader fileReader = new FileReader("stopwords.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> stop_words =  new ArrayList<>();
        String line = bufferedReader.readLine();
        while (line != null) {
            stop_words.add(line);
            line = bufferedReader.readLine();
        }

        List<List<String>> withOutStop = new ArrayList<List<String>>();
        List<String> temp = new ArrayList<String>();
        for (List<String> module : modules) {
            for (String s : module) {
                if (stop_words.contains(s.toLowerCase())) {
                    continue;
                }
                temp.add(s.toLowerCase());
            }
            withOutStop.add(temp);
            temp = new ArrayList<String>();
        }
        return withOutStop;
    }
}
