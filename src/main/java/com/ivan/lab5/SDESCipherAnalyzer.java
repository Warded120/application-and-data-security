package com.ivan.lab5;

import com.ivan.lab4.SDESCipher;
import com.ivan.utils.FileManager;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class SDESCipherAnalyzer {

    /**
     * Converts a binary string to a boolean array.
     * The algorithm interprets '1' as true and '0' as false for each character.
     *
     * @param str the binary string (0s and 1s)
     * @return the boolean array representing the bits
     */
    private static boolean[] stringToBits(String str) {
        boolean[] bits = new boolean[str.length()];
        // Map '1' to true, '0' to false
        for (int i = 0; i < str.length(); i++) {
            bits[i] = str.charAt(i) == '1';
        }
        return bits;
    }

    /**
     * Converts a boolean array to a binary string.
     * The algorithm maps true to '1' and false to '0'.
     *
     * @param bits the boolean array
     * @return the binary string
     */
    private static String bitsToString(boolean[] bits) {
        StringBuilder sb = new StringBuilder();
        for (boolean b : bits) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }

    /**
     * Counts the number of differing bits between two boolean arrays.
     * The algorithm compares each position and increments a counter for mismatches.
     *
     * @param a the first boolean array
     * @param b the second boolean array
     * @return the number of differing bits
     */
    private static int countBitDifferences(boolean[] a, boolean[] b) {
        int count = 0;
        // Increment count when bits differ (a[i] != b[i])
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Analyzes key diffusion in S-DES encryption.
     * The algorithm encrypts a fixed text block with a base key, then flips each key bit
     * one at a time, re-encrypts, and counts the resulting ciphertext bit differences
     * to assess how key changes affect the output.
     *
     * @param text     the input text (uses first byte)
     * @param baseKey  the base key as a binary string
     * @param testName the name of the test for output
     */
    private static void analyzeKeyDiffusion(String text, String baseKey, String testName) {
        // Convert key to boolean array (10 bits)
        boolean[] key = stringToBits(baseKey);
        // Use first byte of text as 8-bit block
        boolean[] block = SDESCipher.byteToBits(text.getBytes()[0]);
        // Encrypt with base key
        boolean[] baseCipher = SDESCipher.encryptBlock(block, key);
        System.out.println("-----------------------------------");
        System.out.println(testName + " base ciphertext: " + bitsToString(baseCipher));
        int[] bitChanges = new int[10];
        int minChanges = Integer.MAX_VALUE;
        int maxChanges = 0;
        int minBit = -1;
        int maxBit = -1;

        // Test effect of flipping each key bit
        for (int i = 0; i < 10; i++) {
            // Clone key and flip bit i
            boolean[] modKey = key.clone();
            modKey[i] = !modKey[i];
            // Encrypt with modified key
            boolean[] cipher = SDESCipher.encryptBlock(block, modKey);
            // Count differing bits in ciphertext
            bitChanges[i] = countBitDifferences(baseCipher, cipher);
            // Track minimum changes
            if (bitChanges[i] < minChanges) {
                minChanges = bitChanges[i];
                minBit = i;
            }
            // Track maximum changes
            if (bitChanges[i] > maxChanges) {
                maxChanges = bitChanges[i];
                maxBit = i;
            }
        }

        System.out.println(testName + " bit changes per key bit flip:");
        for (int i = 0; i < 10; i++) {
            System.out.println("Bit " + i + ": " + bitChanges[i] + " bits changed");
        }
        System.out.println(testName + " min changes: " + minChanges + " (bit " + minBit + ")");
        System.out.println(testName + " max changes: " + maxChanges + " (bit " + maxBit + ")");
    }

    /**
     * Analyzes text diffusion in S-DES encryption.
     * The algorithm encrypts a fixed 8-bit text block with a key, then flips each text bit
     * one at a time, re-encrypts, and counts the resulting ciphertext bit differences
     * to assess how input changes affect the output.
     *
     * @param text     the 8-bit binary text
     * @param key      the key as a binary string
     * @param testName the name of the test for output
     */
    private static void analyzeTextDiffusion(String text, String key, String testName) {
        // Convert text to 8-bit block
        boolean[] block = stringToBits(text);
        // Convert key to 10-bit array
        boolean[] baseKey = stringToBits(key);
        // Encrypt with base block and key
        boolean[] baseCipher = SDESCipher.encryptBlock(block, baseKey);
        System.out.println("-----------------------------------");
        System.out.println(testName + " ciphertext: " + bitsToString(baseCipher));
        int[] bitChanges = new int[8];
        int minChanges = Integer.MAX_VALUE;
        int maxChanges = 0;
        int minBit = -1;
        int maxBit = -1;

        // Test effect of flipping each text bit
        for (int i = 0; i < 8; i++) {
            // Clone block and flip bit i
            boolean[] modBlock = block.clone();
            modBlock[i] = !modBlock[i];
            // Encrypt with modified block
            boolean[] cipher = SDESCipher.encryptBlock(modBlock, baseKey);
            // Count differing bits in ciphertext
            bitChanges[i] = countBitDifferences(baseCipher, cipher);
            // Track minimum changes
            if (bitChanges[i] < minChanges) {
                minChanges = bitChanges[i];
                minBit = i;
            }
            // Track maximum changes
            if (bitChanges[i] > maxChanges) {
                maxChanges = bitChanges[i];
                maxBit = i;
            }
        }

        System.out.println(testName + " bit changes per text bit flip:");
        for (int i = 0; i < 8; i++) {
            System.out.println("Bit " + i + ": " + bitChanges[i] + " bits changed");
        }
        System.out.println(testName + " min changes: " + minChanges + " (bit " + minBit + ")");
        System.out.println(testName + " max changes: " + maxChanges + " (bit " + maxBit + ")");
    }

    /**
     * Modifies a single entry in the S0 S-box of SDESCipher.
     * The algorithm updates the specified row and column with a new value if valid.
     *
     * @param row   the row index (0-3)
     * @param col   the column index (0-3)
     * @param value the new value (0-3)
     */
    private static void modifySBox(int row, int col, int value) {
        // Validate indices and value before modifying S0
        if (row < 4 && col < 4 && value >= 0 && value <= 3) {
            SDESCipher.S0[row][col] = value;
        }
    }

    /**
     * Analyzes S-box diffusion in S-DES encryption.
     * The algorithm encrypts a fixed text block, then modifies each S0 S-box entry to
     * different values, re-encrypts, and counts ciphertext bit differences to assess
     * the impact of S-box changes.
     *
     * @param text     the input text (uses first byte)
     * @param key      the key as a binary string
     * @param testName the name of the test for output
     */
    private static void analyzeSBoxDiffusion(String text, String key, String testName) {
        // Use first byte of text as 8-bit block
        boolean[] block = SDESCipher.byteToBits(text.getBytes()[0]);
        // Convert key to 10-bit array
        boolean[] baseKey = stringToBits(key);
        // Encrypt with original S-box
        boolean[] baseCipher = SDESCipher.encryptBlock(block, baseKey);
        // Backup original S0
        int[][] originalS0 = new int[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(SDESCipher.S0[i], 0, originalS0[i], 0, 4);
        }
        int minChanges = Integer.MAX_VALUE;
        int maxChanges = 0;
        String minChangePos = "";
        String maxChangePos = "";
        int totalChanges = 0;
        int count = 0;

        // Test all possible S-box modifications
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                for (int val = 0; val < 4; val++) {
                    // Skip original value to avoid no-op
                    if (val != originalS0[row][col]) {
                        // Modify S-box entry
                        modifySBox(row, col, val);
                        // Re-encrypt with modified S-box
                        boolean[] cipher = SDESCipher.encryptBlock(block, baseKey);
                        // Count differing bits
                        int changes = countBitDifferences(baseCipher, cipher);
                        totalChanges += changes;
                        count++;
                        // Track minimum changes
                        if (changes < minChanges) {
                            minChanges = changes;
                            minChangePos = "row " + row + ", col " + col + ", val " + val;
                        }
                        // Track maximum changes
                        if (changes > maxChanges) {
                            maxChanges = changes;
                            maxChangePos = "row " + row + ", col " + col + ", val " + val;
                        }
                        // Restore original S-box
                        SDESCipher.S0[row][col] = originalS0[row][col];
                    }
                }
            }
        }

        System.out.println("-----------------------------------");
        System.out.println(testName + " S-box diffusion:");
        System.out.println("Min changes: " + minChanges + " (" + minChangePos + ")");
        System.out.println("Max changes: " + maxChanges + " (" + maxChangePos + ")");
        // Compute average bit changes
        System.out.println("Average changes: " + (totalChanges / (double) count));
    }

    /**
     * Simulates a brute-force attack on S-DES.
     * The algorithm tries all 1024 possible 10-bit keys to find one that produces
     * the target ciphertext for a given text block, measuring time and keys tested.
     *
     * @param text   the input text (uses first byte)
     * @param cipher the target ciphertext as a binary string
     */
    private static void bruteForceAttack(String text, String cipher) {
        // Use first byte of text as 8-bit block
        boolean[] block = SDESCipher.byteToBits(text.getBytes()[0]);
        // Convert target ciphertext to bits
        boolean[] targetCipher = stringToBits(cipher);
        // Record start time in nanoseconds
        long startTime = System.nanoTime();
        int keysTried = 0;
        int successKey = -1;
        boolean found = false;

        // Test all 10-bit keys (0 to 1023)
        for (int i = 0; i < 1024; i++) {
            // Generate key as 10-bit binary string, padded with zeros
            String keyStr = String.format("%10s", Integer.toBinaryString(i)).replace(' ', '0');
            boolean[] key = stringToBits(keyStr);
            // Encrypt block with current key
            boolean[] cipherBits = SDESCipher.encryptBlock(block, key);
            keysTried++;
            // Check if ciphertext matches target
            if (bitsToString(cipherBits).equals(bitsToString(targetCipher))) {
                successKey = i;
                found = true;
                break;
            }
        }

        // Calculate time in milliseconds
        long endTime = System.nanoTime();
        double timeMs = (endTime - startTime) / 1_000_000.0;
        System.out.println("-----------------------------------");
        System.out.println("Brute-force attack:");
        System.out.println("Total keys: 1024");
        System.out.println("Keys tried: " + keysTried);
        // Format success key as 10-bit string if found
        System.out.println("Success key: " + (found ? String.format("%10s", Integer.toBinaryString(successKey)).replace(' ', '0') : "none"));
        System.out.println("Time taken: " + timeMs + " ms");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter input text (at least 1 char):");
        String inputText = scanner.nextLine();
        if (inputText.isEmpty()) {
            System.out.println("Error: Input text cannot be empty.");
            return;
        }

        System.out.println("-----------------------------------");
        System.out.println("Key diffusion analysis:");
        // Analyze with all-0s key
        analyzeKeyDiffusion(inputText, "0000000000", "Zero key");
        // Analyze with all-1s key
        analyzeKeyDiffusion(inputText, "1111111111", "One key");
        // Generate random 10-bit key
        Random rand = new Random();
        String randomKey = "";
        for (int i = 0; i < 10; i++) {
            randomKey += rand.nextInt(2);
        }
        analyzeKeyDiffusion(inputText, randomKey, "Random key (" + randomKey + ")");

        System.out.println("-----------------------------------");
        System.out.println("Text diffusion analysis:");
        // Analyze with all-0s text
        analyzeTextDiffusion("00000000", randomKey, "Zero text");
        // Analyze with all-1s text
        analyzeTextDiffusion("11111111", randomKey, "One text");
        // Generate random 8-bit text
        String randomText = "";
        for (int i = 0; i < 8; i++) {
            randomText += rand.nextInt(2);
        }
        analyzeTextDiffusion(randomText, randomKey, "Random text (" + randomText + ")");

        System.out.println("-----------------------------------");
        System.out.println("S-box diffusion analysis:");
        analyzeSBoxDiffusion(inputText, randomKey, "Random key (" + randomKey + ")");

        System.out.println("-----------------------------------");
        System.out.println("Brute-force attack simulation:");
        // Use input text and random key to generate ciphertext
        boolean[] block = SDESCipher.byteToBits(inputText.getBytes()[0]);
        boolean[] cipher = SDESCipher.encryptBlock(block, stringToBits(randomKey));
        bruteForceAttack(inputText, bitsToString(cipher));
    }
}