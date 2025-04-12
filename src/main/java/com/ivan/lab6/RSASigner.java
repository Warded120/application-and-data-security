package com.ivan.lab6;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RSASigner {
    private static final int n = 33;
    private static final int E = 7;

    private static final String[] ALPHABET = {
            "А", "Б", "В", "Г", "Д", "Е", "Є", "Ж", "З", "И", "І", "Ї", "Й",
            "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц",
            "Ч", "Ш", "Щ", "Ь", "Ю", "Я", " "
    };


    private static int charToCode(String ch) {
        for (int i = 0; i < ALPHABET.length; i++) {
            if (ALPHABET[i].equals(ch)) {
                return i;
            }
        }
        return -1;
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
            int code = charToCode(ch);
            if (code == -1) {
                throw new IllegalArgumentException("Invalid character: " + ch);
            }
            sum += code;
        }
        return sum % n;
    }

    public static void main(String[] args) {
        try {
            String message = FileManager.readInputFile(Locale.UK).toUpperCase();
            List<Integer> encrypted = new ArrayList<>();
            for (int i = 0; i < message.length(); i++) {
                String ch = message.substring(i, i + 1);
                int code = charToCode(ch);
                if (code == -1) {
                    System.out.println("Error: Invalid character " + ch);
                    return;
                }
                long cipher = modPow(code, E, n);
                encrypted.add((int) cipher);
            }
            int hash = computeHash(message);
            long signature = modPow(hash, E, n);
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < encrypted.size(); i++) {
                output.append(encrypted.get(i));
                if (i < encrypted.size() - 1) {
                    output.append(",");
                }
            }
            output.append(",").append(signature);
            FileManager.writeOutputFile(output.toString());
            System.out.println("Encrypted message: " + output.toString());
            System.out.println("Hash: " + hash);
            System.out.println("Signature: " + signature);
        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}