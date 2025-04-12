package com.ivan.lab2;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.Scanner;

public class AffineCipherEncrypt {

    private static int gcd(int a, int m) {
        while (m != 0) {
            int temp = m;
            m = a % m;
            a = temp;
        }
        return a;
    }

    private static char encryptChar(char c, int a, int b) {
        if (!Character.isLetter(c)) {
            return c;
        }
        char base = Character.isUpperCase(c) ? 'A' : 'a';
        int t = c - base;
        int encrypted = (a * t + b) % 26;
        return (char) (base + encrypted);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter key a (coprime with 26):");
        int a = scanner.nextInt();
        System.out.println("Enter key b (0 < b < 26):");
        int b = scanner.nextInt();

        if (gcd(a, 26) != 1) {
            System.out.println("Error: a and 26 must be coprime (GCD(a, 26) = 1).");
            return;
        }
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
