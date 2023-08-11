package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MainIndex {
    public static void main(String[] args) throws IOException {

        File module = new File("module_manual_IMACS.pdf");
        PDDocument pdDocument;
        PDFTextStripper pdfStripper;
        String text;
        File moduleTextFile;

        try {
            pdDocument = PDDocument.load(module);
            pdfStripper = new PDFTextStripper();
            text = pdfStripper.getText(pdDocument);
            //System.out.println(text);
            moduleTextFile = new File("moduleTextFile.txt");
            FileWriter myWriter = new FileWriter("moduleTextFile.txt");

            if (moduleTextFile.length() == 0) {
                myWriter.write(text);
                myWriter.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> modules;
        try {
            //Store each module separately in a list
            modules = CreateChapters.readChapters("moduleTextFile.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Remove all characters except letters and space===========================================
        modules = DataCleaning.removeSymbols(modules);

        //Tokenization=============================================================================
        List<List<String>> module_tokens = DataCleaning.createTokens(modules);

        //STOP WORD ELIMINATION====================================================================
        List<List<String>> sw_eliminated = DataCleaning.stopWordElimination(module_tokens);

        //Lemmatization / Stemming=================================================================
        Lemmatization lemma = new Lemmatization();
        List<List<String>> lemmatized = lemma.lemmatize(sw_eliminated);

        //Calculate importance of each terms ( importance(t) = tf(t) / idf(t) )
        //Map<String, Map<String, Double>> importance = new HashMap<>();
        TermImportance tfidf = new TermImportance(lemmatized);
        //Map<String, Double> importance = tfidf.getTFIDF();
        Map<String, String> moduleText = tfidf.getModules();


        //ESCO ================================================================
        List<String> moduleList = new ArrayList<>(moduleText.values());
        List<String> moduleName = new ArrayList<>(moduleText.keySet());

        //JSON array contains Occupations and SKills (Will be used later)
        JSONArray skills = new JSONArray();
        JSONArray occupations = new JSONArray();

        HashMap<String, Double> tfidfWC;
        List<String> tokenisedModule;
        List<String> descendingWords;
        for (int i = 0; i < moduleList.size(); i++) {
            System.out.println("Module Name: " + moduleName.get(i));

            tokenisedModule = new ArrayList<>(List.of(moduleList.get(i).split(" ")));
            tfidfWC = tfidfFUNCTION(tokenisedModule);

            //ascending to descending
            HashMap<String, Double> ascendingtfidfWC;
            ascendingtfidfWC = sortByValue(tfidfWC);
            System.out.println(ascendingtfidfWC);
            descendingWords = new ArrayList<>(ascendingtfidfWC.keySet());

            //API CODE (ESCO)
            String uriSkill = "https://ec.europa.eu/esco/api/search?language=en&type=skill&text=" + descendingWords.get(0);

            URL urlgetSkill = new URL(uriSkill);

            String readLine;
            HttpURLConnection connection = (HttpURLConnection) urlgetSkill.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                StringBuffer response = new StringBuffer();
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                in.close();

                ///////// Cleaning the JSON result
                JSONObject jsonObject = new JSONObject(response.toString()).getJSONObject("_embedded");
                JSONArray results = jsonObject.getJSONArray("results");
                //System.out.println(results);

                for (int k = 0; k < results.length(); k++) {

                    JSONObject obj = results.getJSONObject(k);
                    JSONObject newobj = new JSONObject();
                    newobj.put("Skills", obj.get("title"));
//                  newobj.put("URI",obj.get("uri"));
                    skills.put(newobj);
                }
            }

            System.out.println(skills);
            skills = new JSONArray();
            //Occupation
            String uriOccupation = "https://ec.europa.eu/esco/api/search?language=en&type=occupation&text=" + descendingWords.get(0);
            URL urlgetOccupation = new URL(uriOccupation);
            HttpURLConnection connection2 = (HttpURLConnection) urlgetOccupation.openConnection();
            connection2.setRequestMethod("GET");
            int responseCode2 = connection2.getResponseCode();
            String readLine2;
            if (responseCode2 == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader((connection2.getInputStream())));
                StringBuffer response = new StringBuffer();
                while ((readLine2 = in.readLine()) != null) {
                    response.append(readLine2);
                }
                in.close();

                /////// The Code for JSON Object

                JSONObject jsonObject = new JSONObject(response.toString()).getJSONObject("_embedded");
                JSONArray results = jsonObject.getJSONArray("results");

                for (int k = 0; k < results.length(); k++) {

                    JSONObject obj = results.getJSONObject(k);
                    JSONObject newobj = new JSONObject();
                    newobj.put("Occupations ", obj.get("title"));
//                  newobj.put("URI",obj.get("uri"));
                    occupations.put(newobj);

                }
                System.out.println(occupations + "\n");
                occupations = new JSONArray();
            }
        }
    }

    public static HashMap<String, Double> tfidfFUNCTION(List<String> wordLIST) {

        int[] TFn = new int[wordLIST.size()];
        HashMap<String, Integer> wordCount = new HashMap<>();
        for (int i = 0; i < wordLIST.size(); i++) {
            String[] words = wordLIST.get(i).split(" ");
            for (int j = 0; j < words.length; j++) {
                if (wordCount.containsKey(words[j])) {
                    wordCount.put(words[j], wordCount.get(words[j]) + 1);
                    TFn[i] = wordCount.get(words[j]) + 1;
                } else {
                    wordCount.put(words[j], 1);
                }
            }
        }

        LinkedHashMap<String, Double> IDF = new LinkedHashMap<>();
        Double[] IDFn = new Double[wordLIST.size()];
        for (int i = 0; i < wordLIST.size(); i++) {
            IDF.put(wordLIST.get(i), Math.log(wordLIST.size() + TFn[i]));
            IDFn[i] = Math.log(wordLIST.size() + TFn[i]);
        }

        LinkedHashMap<String, Double> TFIDF = new LinkedHashMap<>();
        for (int i = 0; i < wordLIST.size(); i++) {
            TFIDF.put(wordLIST.get(i), TFn[i] * IDFn[i]);
        }
        return TFIDF;

    }
    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}