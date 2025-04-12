package com.ivan.lab3;

import com.ivan.utils.FileManager;

import java.io.IOException;
import java.util.Scanner;

public class VernamCipherDecrypt {
    private static final char[] ALPHABET = {
            'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Є', 'Ж', 'З', 'И', 'І', 'Ї', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П',
            'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ь', 'Ю', 'Я'
    };

    private static int charToIndex(char c) {
        for (int i = 0; i < ALPHABET.length; i++) {
            if (ALPHABET[i] == c) {
                return i;
            }
        }
        return -1;
    }

    private static int[] generateGamma(int[] key, int length) {
        int[] y = new int[length + 1];
        y[0] = key[0];
        y[1] = key[1];
        y[2] = key[2];
        for (int t = 3; t <= length; t++) {
            y[t] = (y[t - 1] + y[t - 3]) % 32;
        }
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

        for (int k : key) {
            if (k < 0 || k > 31) {
                System.out.println("Error: Key numbers must be in range 0-31.");
                return;
            }
        }

        try {
            String cipherText = FileManager.readEncryptedFile();
            StringBuilder plainText = new StringBuilder();
            int[] gamma = generateGamma(key, cipherText.length());

            for (int i = 0; i < cipherText.length(); i++) {
                char c = cipherText.charAt(i);
                int index = charToIndex(c);
                if (index == -1) {
                    System.out.println("Error: Invalid character in ciphertext.");
                    return;
                }
                int ti = (index + (32 - gamma[i])) % 32;
                plainText.append(ALPHABET[ti]);
            }

            System.out.println("Decrypted text:");
            System.out.println(plainText);

            System.out.println("Decrypted text with whitespaces:");
            System.out.println(plainText.toString().replace('Ф', ' '));
        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        }
    }
}