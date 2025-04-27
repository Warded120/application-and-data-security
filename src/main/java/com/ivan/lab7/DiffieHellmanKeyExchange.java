package com.ivan.lab7;

import java.math.BigInteger;
import java.util.Random;

public class DiffieHellmanKeyExchange {
    public static void main(String[] args) {
        // Step 1: Select a large prime number p (at least 4 digits)
        BigInteger p = BigInteger.valueOf(10007); // A 5-digit prime number
        System.out.println("Selected prime number p: " + p);

        // Step 2: Find a primitive root a for p
        BigInteger a = findPrimitiveRoot(p);
        System.out.println("Selected primitive root a: " + a);

        // Step 3: Perform Diffie-Hellman key exchange
        // Party A generates private key x
        Random rand = new Random();
        BigInteger x = new BigInteger(p.bitLength(), rand).mod(p.subtract(BigInteger.ONE)).add(BigInteger.ONE);
        BigInteger X = a.modPow(x, p); // A computes X = a^x mod p
        System.out.println("A's public key X: " + X);

        // Party B generates private key y
        BigInteger y = new BigInteger(p.bitLength(), rand).mod(p.subtract(BigInteger.ONE)).add(BigInteger.ONE);
        BigInteger Y = a.modPow(y, p); // B computes Y = a^y mod p
        System.out.println("B's public key Y: " + Y);

        // A computes shared key k = Y^x mod p
        BigInteger k = Y.modPow(x, p);
        System.out.println("A's computed key k: " + k);

        // B computes shared key k' = X^y mod p
        BigInteger kPrime = X.modPow(y, p);
        System.out.println("B's computed key k': " + kPrime);

        // Step 4: Verify if k equals k'
        boolean keysMatch = k.equals(kPrime);
        System.out.println("Do the keys match? " + keysMatch);
    }

    // Function to find a primitive root modulo p
    private static BigInteger findPrimitiveRoot(BigInteger p) {
        BigInteger phi = p.subtract(BigInteger.ONE); // φ(p) = p-1 for prime p
        BigInteger candidate = BigInteger.TWO;

        while (candidate.compareTo(p) < 0) {
            // Check if candidate is a primitive root
            // By Fermat's Little Theorem, a^(p-1) ≡ 1 (mod p)
            // We need a number whose powers mod p generate all numbers from 1 to p-1
            boolean isPrimitive = true;
            // Test if a^((p-1)/q) ≠ 1 for all prime factors q of p-1
            // For simplicity, test a few small powers
            for (BigInteger i = BigInteger.ONE; i.compareTo(phi) < 0; i = i.add(BigInteger.ONE)) {
                BigInteger power = phi.divide(i);
                if (phi.mod(power).equals(BigInteger.ZERO)) {
                    BigInteger test = candidate.modPow(power, p);
                    if (test.equals(BigInteger.ONE) && power.compareTo(phi) < 0) {
                        isPrimitive = false;
                        break;
                    }
                }
            }
            if (isPrimitive) {
                return candidate;
            }
            candidate = candidate.add(BigInteger.ONE);
        }
        throw new RuntimeException("No primitive root found for p = " + p);
    }

    // Function to check if a number is prime
    private static boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }
}