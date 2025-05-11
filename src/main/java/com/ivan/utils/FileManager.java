package com.ivan.utils;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class FileManager {

    public static final String INPUT_EN_FILE = "input-en.txt";
    public static final String INPUT_UK_FILE = "input-uk.txt";
    public static final String OUTPUT_FILE = "encrypted.txt";

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

    public static void writeInputFile(String text) throws IOException {
        Path path = Paths.get("src/main/resources/" + INPUT_EN_FILE);
        BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        writer.write(text);
        writer.close();
    }

    public static byte[] readLargeBinaryFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public static void writeLargeBinaryFile(String filePath, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        }
    }

    public static String readLargeFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }

    public static void writeLargeFile(String filePath, String text) throws IOException {
        Files.write(Paths.get(filePath), text.getBytes(StandardCharsets.UTF_8));
    }
}