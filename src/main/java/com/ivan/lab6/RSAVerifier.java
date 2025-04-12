package com.ivan.lab6;

import com.ivan.utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSAVerifier {
    private static final int n = 33;
    private static final int D = 3;

    private static final String[] ALPHABET = {
            "А", "Б", "В", "Г", "Д", "Е", "Є", "Ж", "З", "И", "І", "Ї", "Й",
            "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц",
            "Ч", "Ш", "Щ", "Ь", "Ю", "Я", " "
    };

    private static String codeToChar(int code) {
        if (code >= 0 && code < ALPHABET.length) {
            return ALPHABET[code];
        }
        return "";
    }

    private static long modPow(long base, long exp, long mod) {
        long result = 1;
        base %= mod;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = (result * base) % mod;
            }
            base = (base * base) % mod;
            exp >>= 1;
        }
        return result;
    }

    private static int computeHash(String message) {
        int sum = 0;
        for (int i = 0; i < message.length(); i++) {
            String ch = message.substring(i, i + 1);
            int code = -1;
            for (int j = 0; j < ALPHABET.length; j++) {
                if (ALPHABET[j].equals(ch)) {
                    code = j;
                    break;
                }
            }
            if (code == -1) {
                throw new IllegalArgumentException("Invalid character: " + ch);
            }
            sum += code;
        }
        return sum % n;
    }

    private static void verifySignature(String input, boolean tamper) {
        try {
            String[] parts = input.split(",");
            List<Integer> encrypted = new ArrayList<>();
            int signature;
            if (!tamper) {
                for (int i = 0; i < parts.length - 1; i++) {
                    encrypted.add(Integer.parseInt(parts[i].trim()));
                }
                signature = Integer.parseInt(parts[parts.length - 1].trim());
            } else {
                for (int i = 0; i < parts.length - 1; i++) {
                    encrypted.add(Integer.parseInt(parts[i].trim()));
                }
                signature = new Random().nextInt(33);
            }
            StringBuilder decrypted = new StringBuilder();
            for (int cipher : encrypted) {
                long code = modPow(cipher, D, n);
                String ch = codeToChar((int) code);
                if (ch.isEmpty()) {
                    System.out.println("Error: Invalid decrypted code " + code);
                    return;
                }
                decrypted.append(ch);
            }
            String message = decrypted.toString();
            int computedHash = computeHash(message);
            long verifiedHash = modPow(signature, D, n);
            boolean isValid = computedHash == verifiedHash;
            System.out.println("Decrypted message: " + message);
            System.out.println("Computed hash: " + computedHash);
            System.out.println("Verified hash: " + verifiedHash);
            System.out.println("Signature valid: " + isValid);
            if (tamper) {
                System.out.println("Tampered signature detected: " + !isValid);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid file format");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            String input = FileManager.readEncryptedFile();
            System.out.println("Original signature verification:");
            verifySignature(input, false);
            System.out.println("\nTampered signature verification:");
            verifySignature(input, true);
        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        }
    }
}