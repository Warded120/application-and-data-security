package com.ivan.lab9;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class CombinedCipher {
    private static final String ALPHABET = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ ";
    private static final int ALPHABET_SIZE = ALPHABET.length();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Get shift value for Caesar cipher
        System.out.println("----------------------------------------------");
        System.out.println("Enter the shift value for Caesar cipher (positive for right shift, negative for left):");
        int shift = scanner.nextInt();
        scanner.nextLine(); // Clear newline

        try {
            // Step 2: Read plaintext from input file
            String plaintext = FileManager.readInputFile(Locale.UK).toUpperCase();
            System.out.println("----------------------------------------------");
            System.out.println("Plaintext: " + plaintext);

            // Step 3: Generate random substitution table
            char[] substitutionTable = generateSubstitutionTable();
            System.out.println("----------------------------------------------");
            System.out.println("Substitution table: " + Arrays.toString(substitutionTable));

            // Step 4: Encrypt the CMPtext
            String caesarEncrypted = caesarEncrypt(plaintext, shift);
            String finalEncrypted = substitutionEncrypt(caesarEncrypted, substitutionTable);
            System.out.println("----------------------------------------------");
            System.out.println("Encrypted text: " + finalEncrypted);

            // Step 5: Write encrypted text, shift, and substitution table to file
            writeCipherOutput(finalEncrypted, shift, substitutionTable);

            // Step 6: Decrypt the text
            String[] cipherData = readCipherOutput();
            String encryptedText = cipherData[0];
            int readShift = Integer.parseInt(cipherData[1]);
            char[] readSubstitutionTable = cipherData[2].toCharArray();
            String caesarDecrypted = substitutionDecrypt(encryptedText, readSubstitutionTable);
            String decryptedText = caesarDecrypt(caesarDecrypted, readShift);
            System.out.println("----------------------------------------------");
            System.out.println("Decrypted text: " + decryptedText);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Caesar cipher encryption
    private static String caesarEncrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index != -1) {
                // Apply shift, handle wrap-around with modulo
                int newIndex = (index + shift) % ALPHABET_SIZE;
                if (newIndex < 0) newIndex += ALPHABET_SIZE;
                result.append(ALPHABET.charAt(newIndex));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Caesar cipher decryption
    private static String caesarDecrypt(String text, int shift) {
        // Decrypt by applying the inverse shift
        return caesarEncrypt(text, -shift);
    }

    // Generate random substitution table
    private static char[] generateSubstitutionTable() {
        char[] table = ALPHABET.toCharArray();
        Random rand = new Random();
        for (int i = ALPHABET_SIZE - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            char temp = table[i];
            table[i] = table[j];
            table[j] = temp;
        }
        return table;
    }

    // Substitution cipher encryption
    private static String substitutionEncrypt(String text, char[] substitutionTable) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index != -1) {
                result.append(substitutionTable[index]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Substitution cipher decryption
    private static String substitutionDecrypt(String text, char[] substitutionTable) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            int index = -1;
            for (int i = 0; i < substitutionTable.length; i++) {
                if (substitutionTable[i] == c) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                result.append(ALPHABET.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Write encrypted text, shift, and substitution table to file
    public static void writeCipherOutput(String encryptedText, int shift, char[] substitutionTable) {
        StringBuilder output = new StringBuilder();
        output.append("Encrypted Text:\n");
        output.append(encryptedText);
        output.append("\n");
        output.append("Shift:\n");
        output.append(shift);
        output.append("\n");
        output.append("Substitution Table:\n");
        output.append(new String(substitutionTable));
        output.append("\n");
        try {
            FileManager.writeOutputFile(output.toString());
        } catch (IOException ignored) {}
    }

    // Read encrypted text, shift, and substitution table from file
    private static String[] readCipherOutput() throws IOException {
        StringBuilder encryptedText = new StringBuilder();
        String shift = "";
        String substitutionTable = "";
        boolean readingText = false;
        boolean readingShift = false;
        boolean readingTable = false;

        try (var reader = Files.newBufferedReader(Paths.get("src/main/resources/"+FileManager.OUTPUT_FILE), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("Encrypted Text:")) {
                    readingText = true;
                    continue;
                } else if (line.equals("Shift:")) {
                    readingText = false;
                    readingShift = true;
                    continue;
                } else if (line.equals("Substitution Table:")) {
                    readingShift = false;
                    readingTable = true;
                    continue;
                }

                if (readingText) {
                    encryptedText.append(line);
                } else if (readingShift) {
                    shift = line;
                } else if (readingTable) {
                    substitutionTable = line;
                }
            }
        }

        return new String[]{encryptedText.toString(), shift, substitutionTable};
    }
}