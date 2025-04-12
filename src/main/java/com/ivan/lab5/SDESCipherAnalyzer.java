package com.ivan.lab5;

import com.ivan.lab4.SDESCipher;
import com.ivan.utils.FileManager;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class SDESCipherAnalyzer {
    private static boolean[] stringToBits(String str) {
        boolean[] bits = new boolean[str.length()];
        for (int i = 0; i < str.length(); i++) {
            bits[i] = str.charAt(i) == '1';
        }
        return bits;
    }

    private static String bitsToString(boolean[] bits) {
        StringBuilder sb = new StringBuilder();
        for (boolean b : bits) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }

    private static int countBitDifferences(boolean[] a, boolean[] b) {
        int count = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                count++;
            }
        }
        return count;
    }

    private static void analyzeKeyDiffusion(String text, String baseKey, String testName) {
        boolean[] key = stringToBits(baseKey);
        boolean[] block = SDESCipher.byteToBits(text.getBytes()[0]);
        boolean[] baseCipher = SDESCipher.encryptBlock(block, key);
        System.out.println("-----------------------------------");
        System.out.println(testName + " base ciphertext: " + bitsToString(baseCipher));
        int[] bitChanges = new int[10];
        int minChanges = Integer.MAX_VALUE;
        int maxChanges = 0;
        int minBit = -1;
        int maxBit = -1;

        for (int i = 0; i < 10; i++) {
            boolean[] modKey = key.clone();
            modKey[i] = !modKey[i];
            boolean[] cipher = SDESCipher.encryptBlock(block, modKey);
            bitChanges[i] = countBitDifferences(baseCipher, cipher);
            if (bitChanges[i] < minChanges) {
                minChanges = bitChanges[i];
                minBit = i;
            }
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

    private static void analyzeTextDiffusion(String text, String key, String testName) {
        boolean[] block = stringToBits(text);
        boolean[] baseKey = stringToBits(key);
        boolean[] baseCipher = SDESCipher.encryptBlock(block, baseKey);
        System.out.println("-----------------------------------");
        System.out.println(testName + " ciphertext: " + bitsToString(baseCipher));
        int[] bitChanges = new int[8];
        int minChanges = Integer.MAX_VALUE;
        int maxChanges = 0;
        int minBit = -1;
        int maxBit = -1;

        for (int i = 0; i < 8; i++) {
            boolean[] modBlock = block.clone();
            modBlock[i] = !modBlock[i];
            boolean[] cipher = SDESCipher.encryptBlock(modBlock, baseKey);
            bitChanges[i] = countBitDifferences(baseCipher, cipher);
            if (bitChanges[i] < minChanges) {
                minChanges = bitChanges[i];
                minBit = i;
            }
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

    private static void modifySBox(int row, int col, int value) {
        if (row < 4 && col < 4 && value >= 0 && value <= 3) {
            SDESCipher.S0[row][col] = value;
        }
    }

    private static void analyzeSBoxDiffusion(String text, String key, String testName) {
        boolean[] block = SDESCipher.byteToBits(text.getBytes()[0]);
        boolean[] baseKey = stringToBits(key);
        boolean[] baseCipher = SDESCipher.encryptBlock(block, baseKey);
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

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                for (int val = 0; val < 4; val++) {
                    if (val != originalS0[row][col]) {
                        modifySBox(row, col, val);
                        boolean[] cipher = SDESCipher.encryptBlock(block, baseKey);
                        int changes = countBitDifferences(baseCipher, cipher);
                        totalChanges += changes;
                        count++;
                        if (changes < minChanges) {
                            minChanges = changes;
                            minChangePos = "row " + row + ", col " + col + ", val " + val;
                        }
                        if (changes > maxChanges) {
                            maxChanges = changes;
                            maxChangePos = "row " + row + ", col " + col + ", val " + val;
                        }
                        SDESCipher.S0[row][col] = originalS0[row][col];
                    }
                }
            }
        }

        System.out.println("-----------------------------------");
        System.out.println(testName + " S-box diffusion:");
        System.out.println("Min changes: " + minChanges + " (" + minChangePos + ")");
        System.out.println("Max changes: " + maxChanges + " (" + maxChangePos + ")");
        System.out.println("Average changes: " + (totalChanges / (double) count));
    }

    private static void bruteForceAttack(String text, String cipher) {
        boolean[] block = SDESCipher.byteToBits(text.getBytes()[0]);
        boolean[] targetCipher = stringToBits(cipher);
        long startTime = System.nanoTime();
        int keysTried = 0;
        int successKey = -1;
        boolean found = false;

        for (int i = 0; i < 1024; i++) {
            String keyStr = String.format("%10s", Integer.toBinaryString(i)).replace(' ', '0');
            boolean[] key = stringToBits(keyStr);
            boolean[] cipherBits = SDESCipher.encryptBlock(block, key);
            keysTried++;
            if (bitsToString(cipherBits).equals(bitsToString(targetCipher))) {
                successKey = i;
                found = true;
                break;
            }
        }

        long endTime = System.nanoTime();
        double timeMs = (endTime - startTime) / 1_000_000.0;
        System.out.println("-----------------------------------");
        System.out.println("Brute-force attack:");
        System.out.println("Total keys: 1024");
        System.out.println("Keys tried: " + keysTried);
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
        analyzeKeyDiffusion(inputText, "0000000000", "Zero key");
        analyzeKeyDiffusion(inputText, "1111111111", "One key");
        Random rand = new Random();
        String randomKey = "";
        for (int i = 0; i < 10; i++) {
            randomKey += rand.nextInt(2);
        }
        analyzeKeyDiffusion(inputText, randomKey, "Random key (" + randomKey + ")");

        System.out.println("-----------------------------------");
        System.out.println("Text diffusion analysis:");
        analyzeTextDiffusion("00000000", randomKey, "Zero text");
        analyzeTextDiffusion("11111111", randomKey, "One text");
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
        boolean[] block = SDESCipher.byteToBits(inputText.getBytes()[0]);
        boolean[] cipher = SDESCipher.encryptBlock(block, stringToBits(randomKey));
        bruteForceAttack(inputText, bitsToString(cipher));

    }
}