package com.ivan.lab2;

import com.ivan.utils.FileManager;

import java.io.IOException;

public class AffineCipherDecrypt {

    private static int gcd(int a, int m) {
        while (m != 0) {
            int temp = m;
            m = a % m;
            a = temp;
        }
        return a;
    }

    private static int modInverse(int a, int m) {
        a = a % m;
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1) {
                return x;
            }
        }
        return -1;
    }

    private static char decryptChar(char c, int a, int b) {
        if (!Character.isLetter(c)) {
            return c;
        }
        char base = Character.isUpperCase(c) ? 'A' : 'a';
        int y = c - base;
        int aInverse = modInverse(a, 26);
        if (aInverse == -1) {
            throw new IllegalArgumentException("Cannot find modular inverse for a.");
        }
        int decrypted = (aInverse * (y - b + 26)) % 26;
        return (char) (base + decrypted);
    }

    public static void main(String[] args) {
        try {
            String cipherText = FileManager.readEncryptedFile();

            String[] lines = cipherText.split("\n");
            if (lines.length < 2) {
                System.out.println("Error: File does not contain keys.");
                return;
            }

            int a = Integer.parseInt(lines[0].substring(7).trim());
            int b = Integer.parseInt(lines[1].substring(7).trim());

            if (gcd(a, 26) != 1) {
                System.out.println("Error: a and 26 must be coprime.");
                return;
            }

            StringBuilder plainText = new StringBuilder();
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
