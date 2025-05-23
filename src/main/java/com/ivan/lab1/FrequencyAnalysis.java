package com.ivan.lab1;

import com.ivan.utils.FileManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FrequencyAnalysis {

    // English letters ordered by frequency (most to least, 'e' most common)
    private static final char[] ENGLISH_FREQUENCY = {
            'e', 't', 'a', 'o', 'i', 'n', 's', 'h', 'r', 'd',
            'l', 'u', 'c', 'm', 'w', 'f', 'g', 'y', 'p', 'b',
            'v', 'k', 'j', 'x', 'q', 'z'
    };

    public static void main(String[] args) {
        try {
            String encryptedText = FileManager.readEncryptedFile();

            // Clean text by:
            // 1. Converting to lowercase (e.g., 'A' → 'a')
            // 2. Filtering letters only (a-z)
            // 3. Converting codes to strings
            // 4. Joining into a single string
            String cleanText = encryptedText.toLowerCase()
                    .chars()
                    .filter(Character::isLetter)
                    .mapToObj(ch -> String.valueOf((char) ch))
                    .collect(Collectors.joining());

            Map<Character, Integer> frequencyMap = new HashMap<>();
            // Initialize map with all letters (a-z) set to 0
            for (char c = 'a'; c <= 'z'; c++) {
                frequencyMap.put(c, 0);
            }

            int totalChars = 0;
            for (char c : cleanText.toCharArray()) {
                frequencyMap.put(c, frequencyMap.get(c) + 1);
                totalChars++;
            }

            List<Map.Entry<Character, Integer>> sortedEntries = new LinkedList<>(frequencyMap.entrySet());
            // Sort by frequency descending (higher counts first)
            sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            System.out.println("Frequency analysis:");
            System.out.printf("%-10s%-10s%-10s%n", "Character", "Count", "Frequency (%)");
            System.out.println("-".repeat(30));
            for (Map.Entry<Character, Integer> entry : sortedEntries) {
                int count = entry.getValue();
                if (count > 0) {
                    // Calculate percentage: (count / total) * 100
                    double frequency = (double) count / totalChars * 100;
                    System.out.printf("%-10c%-10d%.2f%n", entry.getKey(), count, frequency);
                }
            }

            if (totalChars > 0 && !sortedEntries.isEmpty()) {
                char mostFrequentChar = sortedEntries.get(0).getKey();
                // Estimate shift:
                // 1. (mostFrequentChar - 'e'): Difference in ASCII (e.g., 'i' - 'e' = 4)
                // 2. + 26: Avoid negative values
                // 3. % 26: Wrap around alphabet
                int probableShift = (mostFrequentChar - ENGLISH_FREQUENCY[0] + 26) % 26;

                System.out.println("\nShift analysis:");
                System.out.println("Most frequent character in the text: " + mostFrequentChar);
                System.out.println("Most frequent character in English: " + ENGLISH_FREQUENCY[0]);
                System.out.println("Probable shift: " + probableShift);

                String decryptedText = CaesarCipher.decrypt(encryptedText, probableShift);
                System.out.println("\nDecrypted text:");
                System.out.println(decryptedText);
            } else {
                System.out.println("\nThe text does not contain letters for analysis.");
            }

        } catch (IOException e) {
            System.out.println("Error working with files: " + e.getMessage());
        }
    }
}