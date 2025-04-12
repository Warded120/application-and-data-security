package com.ivan.lab1;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.Scanner;

public class CaesarCipher {

    /**
     * Encrypts the input text using a Caesar cipher with the specified shift.
     * The Caesar cipher is a substitution cipher where each letter in the plaintext
     * is shifted by a fixed number of positions in the alphabet (a-z, A-Z), wrapping
     * around from 'z' to 'a' (or 'Z' to 'A'). Non-letter characters remain unchanged.
     *
     * @param text  the input text to encrypt
     * @param shift the number of positions to shift each letter (positive for forward)
     * @return the encrypted text with shifted letters
     */
    public static String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                // Set base to 'A' (ASCII 65) for uppercase or 'a' (ASCII 97) for lowercase
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                // Calculate encrypted character:
                // 1. (c - base): Convert to 0-based index (e.g., 'B' - 'A' = 1)
                // 2. + shift: Apply shift (e.g., shift = 3)
                // 3. % 26: Wrap around alphabet (modulo 26)
                // 4. + base: Convert back to ASCII (e.g., 1 + 'A' = 'B')
                result.append((char) ((c - base + shift) % 26 + base));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Decrypts the input text encrypted with a Caesar cipher using the specified shift.
     * The Caesar cipher shifts each letter by a fixed number of positions. Decryption
     * reverses this by shifting backward by the same number of positions, effectively
     * applying the cipher with a shift of (26 - shift) to wrap around the alphabet
     * (a-z, A-Z). Non-letter characters remain unchanged.
     *
     * @param text  the encrypted text to decrypt
     * @param shift the number of positions used for encryption
     * @return the decrypted text with letters shifted back
     */
    public static String decrypt(String text, int shift) {
        // Decrypt by encrypting with inverse shift:
        // Shift backward by 'shift' → (26 - shift) % 26 (e.g., shift=3 → 23)
        return encrypt(text, 26 - shift);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the offset for encryption:");
        int shift = scanner.nextInt();
        // Clear newline after nextInt() to prevent input issues
        scanner.nextLine();

        try {
            String inputText = FileManager.readInputFile(Locale.EN);
            String encryptedText = encrypt(inputText, shift);

            FileManager.writeOutputFile(encryptedText);
            System.out.println("Encrypted text is saved to encrypted.txt");

            System.out.println("\nDecrypting file: encrypted.txt...");
            String decryptedText = decrypt(encryptedText, shift);
            System.out.println("Decrypted text:");
            System.out.println(decryptedText);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}