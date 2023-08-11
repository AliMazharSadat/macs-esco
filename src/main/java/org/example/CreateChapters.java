package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateChapters {

    public static List<String> readChapters(String textFile) throws IOException
    {
        FileReader fileReader = new FileReader(textFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            contentBuilder.append(line).append("\n");
        }
        String lines = contentBuilder.toString();
        String[] chapters = lines.split("Module Name");
        // Store the elements of array to an arraylist for more efficient modification
        List<String> modules = new ArrayList<>();
        Collections.addAll(modules, chapters);
        modules.remove(0);

        bufferedReader.close();
        return modules;
    }
}
