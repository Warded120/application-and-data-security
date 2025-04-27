package com.ivan.lab6;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RSASigner {
    // Private Modulus n = 33 (p * q, where p=3, q=11 for RSA)
    private static final int n = 33;
    // Public exponent e = 7 (coprime with (p-1)*(q-1) = 20)
    private static final int E = 7;

    // Alphabet of Ukrainian letters plus space (33 characters)
    private static final String[] ALPHABET = {
            "А", "Б", "В", "Г", "Д", "Е", "Є", "Ж", "З", "И", "І", "Ї", "Й",
            "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц",
            "Ч", "Ш", "Щ", "Ь", "Ю", "Я", " "
    };

    /**
     * Converts a character to its index in the ALPHABET array.
     * The algorithm maps each character to a unique code (0-32) for RSA encryption.
     *
     * @param ch the input character (single Ukrainian letter or space)
     * @return the index (0-32) or -1 if invalid
     */
    private static int charToCode(String ch) {
        // Search ALPHABET for matching character
        for (int i = 0; i < ALPHABET.length; i++) {
            if (ALPHABET[i].equals(ch)) {
                return i;
            }
        }
        // Return -1 for invalid characters
        return -1;
    }

    /**
     * Computes modular exponentiation (base^exp mod mod).
     * The algorithm uses the square-and-multiply method to efficiently calculate
     * the result, reducing intermediate values modulo mod to prevent overflow.
     *
     * @param base the base value
     * @param exp  the exponent
     * @param mod  the modulus
     * @return the result of base^exp mod mod
     */
    private static long modPow(long base, long exp, long mod) {
        long result = 1;
        // Normalize base to avoid negative values
        base %= mod;
        // Process each bit of exponent
        while (exp > 0) {
            // If exp is odd, multiply result by base (mod mod)
            if ((exp & 1) == 1) {
                result = (result * base) % mod;
            }
            // Square base and reduce mod
            base = (base * base) % mod;
            // Right-shift exp to process next bit
            exp >>= 1;
        }
        return result;
    }

    /**
     * Computes a hash of the message for signing.
     * The algorithm sums the ALPHABET indices of each character and reduces the sum
     * modulo n to produce a hash value suitable for RSA signature.
     *
     * @param message the input message
     * @return the hash value (sum of character codes mod n)
     */
    private static int computeHash(String message) {
        int sum = 0;
        for (int i = 0; i < message.length(); i++) {
            // Extract single character
            String ch = message.substring(i, i + 1);
            int code = charToCode(ch);
            if (code == -1) {
                throw new IllegalArgumentException("Invalid character: " + ch);
            }
            // Add character code to running sum
            sum += code;
        }
        // Reduce sum modulo n
        return sum % n;
    }

    public static void main(String[] args) {
        try {
            // Read and convert input to uppercase for consistency
            String message = FileManager.readInputFile(Locale.UK).replace(System.lineSeparator(), " ").toUpperCase();
            List<Integer> encrypted = new ArrayList<>();
            for (int i = 0; i < message.length(); i++) {
                // Process each character
                String ch = message.substring(i, i + 1);
                int code = charToCode(ch);
                if (code == -1) {
                    System.out.println("Error: Invalid character " + ch);
                    return;
                }
                // Encrypt code using RSA: code^E mod n
                long cipher = modPow(code, E, n);
                encrypted.add((int) cipher);
            }
            // Compute message hash
            int hash = computeHash(message);
            // Sign hash: hash^E mod n
            long signature = modPow(hash, E, n);
            StringBuilder output = new StringBuilder();
            // Build comma-separated list of encrypted codes
            for (int i = 0; i < encrypted.size(); i++) {
                output.append(encrypted.get(i));
                if (i < encrypted.size() - 1) {
                    output.append(",");
                }
            }
            // Append signature
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