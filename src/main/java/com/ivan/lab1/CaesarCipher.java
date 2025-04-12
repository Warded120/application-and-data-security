package com.ivan.lab1;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.Scanner;

public class CaesarCipher {

    public static String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                result.append((char) ((c - base + shift) % 26 + base));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String decrypt(String text, int shift) {
        return encrypt(text, 26 - shift);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the offset for encryption:");
        int shift = scanner.nextInt();
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
