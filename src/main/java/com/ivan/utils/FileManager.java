package com.ivan.utils;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class FileManager {

    private static final String INPUT_EN_FILE = "input-en.txt";
    private static final String INPUT_UK_FILE = "input-uk.txt";
    private static final String OUTPUT_FILE = "encrypted.txt";

    public static String readInputFile(Locale locale) throws IOException {
        String filename = locale == Locale.EN ? INPUT_EN_FILE : INPUT_UK_FILE;
        InputStream inputStream = FileManager.class.getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        return content.toString().trim();
    }

    public static String readEncryptedFile() throws IOException {
        InputStream inputStream = FileManager.class.getClassLoader().getResourceAsStream(OUTPUT_FILE);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found: " + OUTPUT_FILE);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        return content.toString().trim();
    }

    public static void writeOutputFile(String text) throws IOException {
        Path path = Paths.get("src/main/resources/" + OUTPUT_FILE);
        BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

        writer.write(text);
        writer.close();
    }
}
