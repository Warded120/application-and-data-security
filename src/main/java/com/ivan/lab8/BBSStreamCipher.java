package com.ivan.lab8;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class BBSStreamCipher {
    public static void main(String[] args) throws IOException {
        // Step 1: Select two prime numbers p and q, where p ≡ q ≡ 3 mod 4
        BigInteger p = BigInteger.valueOf(383); // Prime number, 383 ≡ 3 mod 4
        BigInteger q = BigInteger.valueOf(467); // Prime number, 467 ≡ 3 mod 4
        BigInteger n = p.multiply(q); // n = p * q = 178861
        System.out.println("------------------------------");
        System.out.println("Prime p: " + p);
        System.out.println("Prime q: " + q);
        System.out.println("Modulus n: " + n);

        // Step 2: Select a random seed x, coprime with n
        BigInteger x = BigInteger.valueOf(12345); // Random seed, coprime with n
        while (x.gcd(n).compareTo(BigInteger.ONE) != 0 || x.compareTo(n) >= 0) {
            x = new BigInteger(n.bitLength(), new Random()).mod(n);
        }
        System.out.println("------------------------------");
        System.out.println("Seed x: " + x);

        // Step 3: Initialize BBS generator
        BigInteger x0 = x.modPow(BigInteger.TWO, n); // x0 = x^2 mod n
        System.out.println("Initial state x0: " + x0);

        // Step 4: Encrypt a message
        String plaintext = FileManager.readInputFile(Locale.EN);
        System.out.println("------------------------------");
        System.out.println("Plaintext: " + plaintext);
        StringBuilder ciphertext = new StringBuilder();
        BigInteger currentState = x0;

        // Generate BBS sequence and encrypt character by character
        for (char c : plaintext.toCharArray()) {
            // Get next 8 bits from BBS for XOR
            StringBuilder keyBits = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                currentState = currentState.modPow(BigInteger.TWO, n); // xi = xi-1^2 mod n
                BigInteger bit = currentState.mod(BigInteger.TWO); // si = xi mod 2
                keyBits.append(bit);
            }
            // Convert character to binary and XOR with key
            String charBinary = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            StringBuilder cipherBits = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                int plainBit = charBinary.charAt(i) - '0';
                int keyBit = keyBits.charAt(i) - '0';
                cipherBits.append(plainBit ^ keyBit);
            }
            // Convert binary back to character
            int cipherChar = Integer.parseInt(cipherBits.toString(), 2);
            ciphertext.append((char) cipherChar);
        }
        FileManager.writeOutputFile(ciphertext.toString());

        System.out.println("------------------------------");
        System.out.println("Ciphertext:");
        System.out.print(ciphertext);

        // Step 5: Decrypt the message
        StringBuilder decrypted = new StringBuilder();
        currentState = x0; // Reset to initial state
        for (char c : ciphertext.toString().toCharArray()) {
            // Get next 8 bits from BBS for XOR
            StringBuilder keyBits = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                currentState = currentState.modPow(BigInteger.TWO, n);
                BigInteger bit = currentState.mod(BigInteger.TWO);
                keyBits.append(bit);
            }
            // Convert cipher character to binary and XOR with key
            String cipherBinary = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            StringBuilder plainBits = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                int cipherBit = cipherBinary.charAt(i) - '0';
                int keyBit = keyBits.charAt(i) - '0';
                plainBits.append(cipherBit ^ keyBit);
            }
            // Convert binary back to character
            int plainChar = Integer.parseInt(plainBits.toString(), 2);
            decrypted.append((char) plainChar);
        }
        System.out.println("------------------------------");
        System.out.println("Decrypted text: " + decrypted);
        System.out.println("------------------------------");
    }
}