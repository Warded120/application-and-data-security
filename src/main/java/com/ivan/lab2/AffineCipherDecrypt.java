package com.ivan.lab2;

import com.ivan.utils.FileManager;

import java.io.IOException;

public class AffineCipherDecrypt {

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
     * Finds the modular multiplicative inverse of a modulo m.
     * The inverse x satisfies (a * x) mod m = 1. The algorithm tests values of x from 1 to m-1
     * to find the one that meets this condition, assuming a and m are coprime.
     *
     * @param a the number to find the inverse for
     * @param m the modulus
     * @return the modular inverse, or -1 if none exists
     */
    private static int modInverse(int a, int m) {
        // Normalize a to ensure it’s positive and less than m
        a = a % m;
        // Test x from 1 to m-1 to find (a * x) mod m = 1
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1) {
                return x;
            }
        }
        // Return -1 if no inverse exists (a and m not coprime)
        return -1;
    }

    /**
     * Decrypts a single character encrypted with an affine cipher using keys a and b.
     * The affine cipher’s encryption is y = (ax + b) mod 26. Decryption reverses this by
     * computing x = a^(-1) * (y - b) mod 26, where a^(-1) is the modular inverse of a mod 26.
     * Non-letter characters remain unchanged.
     *
     * @param c the encrypted character
     * @param a the multiplicative key used for encryption
     * @param b the additive key used for encryption
     * @return the decrypted character
     */
    private static char decryptChar(char c, int a, int b) {
        if (!Character.isLetter(c)) {
            return c;
        }
        // Set base to 'A' (ASCII 65) for uppercase or 'a' (ASCII 97) for lowercase
        char base = Character.isUpperCase(c) ? 'A' : 'a';
        // Convert encrypted character to 0-based index (e.g., 'B' - 'A' = 1)
        int y = c - base;
        // Find modular inverse of a mod 26
        int aInverse = modInverse(a, 26);
        if (aInverse == -1) {
            throw new IllegalArgumentException("Cannot find modular inverse for a.");
        }
        // Decrypt: a^(-1) * (y - b) mod 26
        // 1. (y - b + 26): Subtract b, add 26 to avoid negative values
        // 2. * aInverse: Multiply by inverse of a
        // 3. % 26: Wrap around alphabet
        int decrypted = (aInverse * (y - b + 26)) % 26;
        // Convert back to ASCII (e.g., 0 + 'A' = 'A')
        return (char) (base + decrypted);
    }

    public static void main(String[] args) {
        try {
            String cipherText = FileManager.readEncryptedFile();

            // Split file into lines (expects "Key a: X", "Key b: Y", ciphertext)
            String[] lines = cipherText.split("\n");
            if (lines.length < 2) {
                System.out.println("Error: File does not contain keys.");
                return;
            }

            // Parse a from first line, skipping "Key a: " prefix
            int a = Integer.parseInt(lines[0].substring(7).trim());
            // Parse b from second line, skipping "Key b: " prefix
            int b = Integer.parseInt(lines[1].substring(7).trim());

            // Verify a is coprime with 26 (GCD must be 1 for inverse)
            if (gcd(a, 26) != 1) {
                System.out.println("Error: a and 26 must be coprime.");
                return;
            }

            StringBuilder plainText = new StringBuilder();
            // Process ciphertext, skipping key lines (length + 2 for newlines)
            for (char c : cipherText.substring(lines[0].length() + lines[1].length() + 2).toCharArray()) {
                plainText.append(decryptChar(c, a, b));
            }

            System.out.println("Decrypted text:");
            System.out.println(plainText);

        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}