package com.ivan.lab3;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.Scanner;

public class VernamCipherEncrypt {
    // Alphabet for Ukrainian letters (32 characters, А to Я, no whitespace)
    private static final char[] ALPHABET = {
            'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Є', 'Ж', 'З', 'И', 'І', 'Ї', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П',
            'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ь', 'Ю', 'Я'
    };

    /**
     * Converts a character to its 0-based index in the ALPHABET array.
     * The Vernam cipher uses this index for modular arithmetic operations.
     *
     * @param c the character to convert
     * @return the index (0-31) or -1 if not found
     */
    private static int charToIndex(char c) {
        // Search ALPHABET for character, return index or -1 if invalid
        for (int i = 0; i < ALPHABET.length; i++) {
            if (ALPHABET[i] == c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Generates a pseudorandom gamma sequence for the Vernam cipher.
     * The algorithm initializes a sequence y with three key values, then computes
     * subsequent y[t] as (y[t-1] + y[t-3]) mod 32. The final gamma z[t] is
     * (y[t] + y[t+1]) mod 32, producing a sequence of length equal to the input text.
     *
     * @param key    array of three initial key values (0-31)
     * @param length desired length of the gamma sequence
     * @return the gamma sequence as an integer array
     */
    private static int[] generateGamma(int[] key, int length) {
        // Initialize y array with one extra slot for gamma calculation
        int[] y = new int[length + 1];
        y[0] = key[0];
        y[1] = key[1];
        y[2] = key[2];
        // Generate y[t] = (y[t-1] + y[t-3]) mod 32 for t >= 3
        // Combines recent and earlier values, wraps around 32
        for (int t = 3; t <= length; t++) {
            y[t] = (y[t - 1] + y[t - 3]) % 32;
        }
        // Compute gamma z[t] = (y[t] + y[t+1]) mod 32
        // Pairs adjacent y values for final sequence
        int[] z = new int[length];
        for (int t = 0; t < length; t++) {
            z[t] = (y[t] + y[t + 1]) % 32;
        }
        return z;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter three key numbers (0-31):");
        int[] key = new int[3];
        key[0] = scanner.nextInt();
        key[1] = scanner.nextInt();
        key[2] = scanner.nextInt();

        // Validate each key is in range 0-31
        for (int k : key) {
            if (k < 0 || k > 31) {
                System.out.println("Error: Key numbers must be in range 0-31.");
                return;
            }
        }

        try {
            // Read input, convert to uppercase, replace spaces with 'Ф' (index 24)
            String plainText = FileManager.readInputFile(Locale.UK).toUpperCase().replace(System.lineSeparator(), " ").replace(" ", "ФЯ");
            StringBuilder cipherText = new StringBuilder();
            // Generate gamma sequence matching plaintext length
            int[] gamma = generateGamma(key, plainText.length());

            // Encrypt each character
            for (int i = 0; i < plainText.length(); i++) {
                char c = plainText.charAt(i);
                int index = charToIndex(c);
                if (index == -1) {
                    System.out.println("Error: Invalid character in input: " + c);
                    return;
                }
                // Vernam encryption: (index + gamma[i]) mod 32
                // Adds gamma value to character index, wraps around 32
                int si = (index + gamma[i]) % 32;
                // Map result back to ALPHABET character
                cipherText.append(ALPHABET[si]);
            }

            FileManager.writeOutputFile(cipherText.toString());
            System.out.println("Ciphertext saved to resources/encrypted.txt");

        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        }
    }
}