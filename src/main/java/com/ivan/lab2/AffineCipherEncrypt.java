package com.ivan.lab2;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.Scanner;

public class AffineCipherEncrypt {

    /**
     * Computes the greatest common divisor (GCD) of two integers using the Euclidean algorithm.
     * The algorithm iteratively reduces the problem by replacing the larger number with the remainder
     * of dividing it by the smaller number until the remainder is zero. The GCD is the last non-zero
     * remainder.
     *
     * @param a the first integer
     * @param m the second integer
     * @return the GCD of a and m
     */
    private static int gcd(int a, int m) {
        // Euclidean algorithm: Repeatedly compute remainder until m becomes 0
        // 'temp' stores m before updating to avoid losing its value
        while (m != 0) {
            int temp = m;
            m = a % m;
            a = temp;
        }
        return a;
    }

    /**
     * Encrypts a single character using the affine cipher with keys a and b.
     * The affine cipher transforms a letter x (0-based index in a-z) to (ax + b) mod 26,
     * preserving case (a-z or A-Z). Non-letter characters remain unchanged.
     *
     * @param c the character to encrypt
     * @param a the multiplicative key (must be coprime with 26)
     * @param b the additive key
     * @return the encrypted character
     */
    private static char encryptChar(char c, int a, int b) {
        if (!Character.isLetter(c)) {
            return c;
        }
        // Set base to 'A' (ASCII 65) for uppercase or 'a' (ASCII 97) for lowercase
        char base = Character.isUpperCase(c) ? 'A' : 'a';
        // Convert character to 0-based index (e.g., 'B' - 'A' = 1)
        int t = c - base;
        // Apply affine transformation: (a * t + b) mod 26
        // Multiplies by a, adds b, wraps around alphabet
        int encrypted = (a * t + b) % 26;
        // Convert back to ASCII (e.g., 1 + 'A' = 'B')
        return (char) (base + encrypted);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter key a (coprime with 26):");
        int a = scanner.nextInt();
        System.out.println("Enter key b (0 < b < 26):");
        int b = scanner.nextInt();

        // Check if a is coprime with 26 (GCD must be 1 for modular inverse to exist)
        if (gcd(a, 26) != 1) {
            System.out.println("Error: a and 26 must be coprime (GCD(a, 26) = 1).");
            return;
        }
        // Validate b is in range (0 < b < 26)
        if (b <= 0 || b >= 26) {
            System.out.println("Error: b must be in range 0 < b < 26.");
            return;
        }

        try {
            String plainText = FileManager.readInputFile(Locale.EN);

            StringBuilder cipherText = new StringBuilder();
            for (char c : plainText.toCharArray()) {
                cipherText.append(encryptChar(c, a, b));
            }

            // Format output with keys followed by ciphertext
            String output = "Key a: " + a + "\n" +
                    "Key b: " + b + "\n" +
                    cipherText;

            FileManager.writeOutputFile(output);
            System.out.println("Ciphertext saved to encrypted.txt with keys.");

        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        }
    }
}