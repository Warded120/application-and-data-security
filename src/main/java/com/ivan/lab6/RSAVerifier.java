package com.ivan.lab6;

import com.ivan.utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSAVerifier {
    // Modulus n = 33 (p * q, where p=3, q=11 for RSA)
    private static final int n = 33;
    // Private exponent d = 3 (satisfies e*d ≡ 1 mod (p-1)*(q-1), where e=7)
    private static final int D = 3;

    // Alphabet of Ukrainian letters plus space (33 characters)
    private static final String[] ALPHABET = {
            "А", "Б", "В", "Г", "Д", "Е", "Є", "Ж", "З", "И", "І", "Ї", "Й",
            "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц",
            "Ч", "Ш", "Щ", "Ь", "Ю", "Я", " "
    };

    /**
     * Converts a numeric code to its corresponding character in the ALPHABET array.
     * The algorithm maps decrypted RSA codes back to characters for message recovery.
     *
     * @param code the numeric code (0-32)
     * @return the corresponding character or empty string if invalid
     */
    private static String codeToChar(int code) {
        // Check if code is valid index in ALPHABET
        if (code >= 0 && code < ALPHABET.length) {
            return ALPHABET[code];
        }
        // Return empty string for invalid codes
        return "";
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
     * Computes a hash of the message for verification.
     * The algorithm sums the ALPHABET indices of each character and reduces the sum
     * modulo n to produce a hash value for RSA signature comparison.
     *
     * @param message the input message
     * @return the hash value (sum of character codes mod n)
     */
    private static int computeHash(String message) {
        int sum = 0;
        for (int i = 0; i < message.length(); i++) {
            // Extract single character
            String ch = message.substring(i, i + 1);
            int code = -1;
            // Search ALPHABET for character index
            for (int j = 0; j < ALPHABET.length; j++) {
                if (ALPHABET[j].equals(ch)) {
                    code = j;
                    break;
                }
            }
            if (code == -1) {
                throw new IllegalArgumentException("Invalid character: " + ch);
            }
            // Add character code to running sum
            sum += code;
        }
        // Reduce sum modulo n
        return sum % n;
    }

    /**
     * Verifies an RSA signature for an encrypted message.
     * The algorithm decrypts the message using the private exponent d, computes the
     * message hash, and compares it to the decrypted signature hash. If tamper is true,
     * a random signature is used to simulate an invalid signature.
     *
     * @param input  the input string (comma-separated encrypted codes and signature)
     * @param tamper whether to use a random signature for tampering simulation
     */
    private static void verifySignature(String input, boolean tamper) {
        try {
            // Split input into encrypted codes and signature
            String[] parts = input.split(",");
            List<Integer> encrypted = new ArrayList<>();
            int signature;
            if (!tamper) {
                // Parse all but last part as encrypted codes
                for (int i = 0; i < parts.length - 1; i++) {
                    encrypted.add(Integer.parseInt(parts[i].trim()));
                }
                // Parse last part as signature
                signature = Integer.parseInt(parts[parts.length - 1].trim());
            } else {
                // Use original codes but random signature (0-32)
                for (int i = 0; i < parts.length - 1; i++) {
                    encrypted.add(Integer.parseInt(parts[i].trim()));
                }
                // Simulate tampering with random value
                signature = new Random().nextInt(33);
            }
            StringBuilder decrypted = new StringBuilder();
            // Decrypt each code
            for (int cipher : encrypted) {
                // RSA decryption: cipher^D mod n
                long code = modPow(cipher, D, n);
                String ch = codeToChar((int) code);
                if (ch.isEmpty()) {
                    System.out.println("Error: Invalid decrypted code " + code);
                    return;
                }
                decrypted.append(ch);
            }
            String message = decrypted.toString();
            // Compute hash of decrypted message
            int computedHash = computeHash(message);
            // Decrypt signature to verify hash: signature^D mod n
            long verifiedHash = modPow(signature, D, n);
            // Check if computed hash matches verified hash
            boolean isValid = computedHash == verifiedHash;
            System.out.println("Decrypted message: " + message);
            System.out.println("Computed hash: " + computedHash);
            System.out.println("Verified hash: " + verifiedHash);
            System.out.println("Signature valid: " + isValid);
            if (tamper) {
                // Confirm tampering detection (invalid signature should fail)
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
            // Read encrypted file (codes and signature)
            String input = FileManager.readEncryptedFile();
            System.out.println("Original signature verification:");
            // Verify with original signature
            verifySignature(input, false);
            System.out.println("\nTampered signature verification:");
            // Verify with tampered signature
            verifySignature(input, true);
        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        }
    }
}