package com.ivan.lab4;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.Scanner;

public class SDESCipher {
    // Initial permutation table (maps 8-bit input positions to output)
    private static final int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    // Inverse initial permutation table
    private static final int[] IP_INV = {4, 1, 3, 5, 7, 2, 8, 6};
    // Expansion permutation table (expands 4 bits to 8, with repeats)
    private static final int[] EP = {4, 1, 2, 3, 2, 3, 4, 1};
    // Key permutation table (selects 10 bits from key)
    private static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    // Subkey permutation table (selects 8 bits from 10)
    private static final int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    // Permutation table for S-box output (4 bits)
    private static final int[] P4 = {2, 4, 3, 1};
    // S-box 0: Maps 4-bit input to 2-bit output (row: bits 1,4; col: bits 2,3)
    public static final int[][] S0 = {
            {1, 0, 3, 2}, {3, 2, 1, 0}, {0, 2, 1, 3}, {3, 1, 3, 2}
    };
    // S-box 1: Similar mapping for second 4-bit input
    public static final int[][] S1 = {
            {0, 1, 2, 3}, {2, 0, 1, 3}, {3, 0, 1, 2}, {2, 1, 0, 3}
    };

    /**
     * Applies a permutation to a boolean array based on a table.
     * The algorithm reorders input bits to output positions specified by the table.
     *
     * @param input the input bit array
     * @param table the permutation table (1-based indices)
     * @return the permuted bit array
     */
    private static boolean[] permute(boolean[] input, int[] table) {
        boolean[] output = new boolean[table.length];
        // Map each output position to input[table[i] - 1] (table uses 1-based indexing)
        for (int i = 0; i < table.length; i++) {
            output[i] = input[table[i] - 1];
        }
        return output;
    }

    /**
     * Performs a circular left shift on a boolean array.
     * The algorithm shifts bits left by the specified amount, wrapping around to the start.
     *
     * @param input the input bit array
     * @param shift the number of positions to shift
     * @return the shifted bit array
     */
    private static boolean[] leftShift(boolean[] input, int shift) {
        boolean[] output = new boolean[input.length];
        // Shift bits left, wrap around using modulo (e.g., shift=1, i=0 → input[1])
        for (int i = 0; i < input.length; i++) {
            output[i] = input[(i + shift) % input.length];
        }
        return output;
    }

    /**
     * Computes the XOR of two boolean arrays.
     * The algorithm performs a bitwise exclusive-or, where result[i] is true if inputs differ.
     *
     * @param a the first bit array
     * @param b the second bit array
     * @return the XOR result
     */
    private static boolean[] xor(boolean[] a, boolean[] b) {
        boolean[] result = new boolean[a.length];
        // XOR: true if a[i] != b[i], false if equal
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] != b[i];
        }
        return result;
    }

    /**
     * Applies an S-box transformation to a 4-bit input.
     * The algorithm uses bits 1 and 4 for the row, bits 2 and 3 for the column,
     * and maps to a 2-bit output via the provided S-box table.
     *
     * @param input the 4-bit input array
     * @param sBox  the S-box table (4x4)
     * @return the 2-bit output
     */
    private static boolean[] sBox(boolean[] input, int[][] sBox) {
        // Row: bit 1 (MSB, 2 if true) + bit 4 (LSB, 1 if true)
        int row = (input[0] ? 2 : 0) + (input[3] ? 1 : 0);
        // Column: bit 2 (2 if true) + bit 3 (1 if true)
        int col = (input[1] ? 2 : 0) + (input[2] ? 1 : 0);
        // Get S-box value (0-3)
        int value = sBox[row][col];
        // Convert to 2 bits: bit 0 = (value & 2), bit 1 = (value & 1)
        boolean[] output = new boolean[2];
        output[0] = (value & 2) != 0;
        output[1] = (value & 1) != 0;
        return output;
    }

    /**
     * Computes the f-function for S-DES.
     * The algorithm expands the 4-bit right half, XORs with a subkey, applies S0 and S1
     * S-boxes to produce 4 bits, and permutes the result with P4.
     *
     * @param right  the 4-bit right half of the block
     * @param subkey the 8-bit subkey
     * @return the 4-bit output
     */
    private static boolean[] fFunction(boolean[] right, boolean[] subkey) {
        // Expand right half from 4 to 8 bits using EP
        boolean[] expanded = permute(right, EP);
        // XOR expanded bits with subkey
        boolean[] xored = xor(expanded, subkey);
        // Split into two 4-bit inputs for S0 and S1
        boolean[] s0Input = {xored[0], xored[1], xored[2], xored[3]};
        boolean[] s1Input = {xored[4], xored[5], xored[6], xored[7]};
        boolean[] s0Output = sBox(s0Input, S0);
        boolean[] s1Output = sBox(s1Input, S1);
        // Combine S-box outputs (2 bits each) into 4 bits
        boolean[] combined = new boolean[4];
        System.arraycopy(s0Output, 0, combined, 0, 2);
        System.arraycopy(s1Output, 0, combined, 2, 2);
        // Permute combined output with P4
        return permute(combined, P4);
    }

    /**
     * Generates two 8-bit subkeys from a 10-bit key.
     * The algorithm permutes the key with P10, splits into two 5-bit halves, applies
     * left shifts (1 for k1, 2 more for k2), and selects 8 bits with P8 for each subkey.
     *
     * @param key the 10-bit key
     * @return array of two 8-bit subkeys
     */
    private static boolean[][] generateSubkeys(boolean[] key) {
        // Permute key with P10 (10 bits)
        boolean[] permuted = permute(key, P10);
        // Split into left and right 5-bit halves
        boolean[] left = new boolean[5];
        boolean[] right = new boolean[5];
        System.arraycopy(permuted, 0, left, 0, 5);
        System.arraycopy(permuted, 5, right, 0, 5);
        // Shift both halves left by 1 for k1
        left = leftShift(left, 1);
        right = leftShift(right, 1);
        // Combine shifted halves
        boolean[] combined1 = new boolean[10];
        System.arraycopy(left, 0, combined1, 0, 5);
        System.arraycopy(right, 0, combined1, 5, 5);
        // Select 8 bits for k1 with P8
        boolean[] k1 = permute(combined1, P8);
        // Shift both halves left by 2 more for k2
        left = leftShift(left, 2);
        right = leftShift(right, 2);
        // Combine again
        boolean[] combined2 = new boolean[10];
        System.arraycopy(left, 0, combined2, 0, 5);
        System.arraycopy(right, 0, combined2, 5, 5);
        // Select 8 bits for k2
        boolean[] k2 = permute(combined2, P8);
        return new boolean[][]{k1, k2};
    }

    /**
     * Encrypts an 8-bit block using S-DES.
     * The algorithm applies an initial permutation (IP), two Feistel rounds (f-function
     * with subkeys k1, k2, XOR, and swap), and an inverse permutation (IP_INV).
     *
     * @param block the 8-bit input block
     * @param key   the 10-bit key
     * @return the encrypted 8-bit block
     */
    public static boolean[] encryptBlock(boolean[] block, boolean[] key) {
        boolean[][] subkeys = generateSubkeys(key);
        // Apply initial permutation
        boolean[] ip = permute(block, IP);
        // Split into left and right 4-bit halves
        boolean[] left = new boolean[4];
        boolean[] right = new boolean[4];
        System.arraycopy(ip, 0, left, 0, 4);
        System.arraycopy(ip, 4, right, 0, 4);
        // Round 1: f(right, k1) XOR left
        boolean[] f = fFunction(right, subkeys[0]);
        boolean[] newLeft = xor(left, f);
        // Swap: right becomes old left, left becomes newLeft
        boolean[] temp = right;
        right = newLeft;
        left = temp;
        // Round 2: f(right, k2) XOR left
        f = fFunction(right, subkeys[1]);
        newLeft = xor(left, f);
        // Combine halves (no swap after round 2)
        boolean[] combined = new boolean[8];
        System.arraycopy(newLeft, 0, combined, 0, 4);
        System.arraycopy(right, 0, combined, 4, 4);
        // Apply inverse permutation
        return permute(combined, IP_INV);
    }

    /**
     * Decrypts an 8-bit block encrypted with S-DES.
     * The algorithm applies an initial permutation (IP), two Feistel rounds using
     * subkeys k2 then k1 (reverse order), XOR, swap, and an inverse permutation (IP_INV).
     *
     * @param block the 8-bit encrypted block
     * @param key   the 10-bit key
     * @return the decrypted 8-bit block
     */
    public static boolean[] decryptBlock(boolean[] block, boolean[] key) {
        boolean[][] subkeys = generateSubkeys(key);
        // Apply initial permutation
        boolean[] ip = permute(block, IP);
        // Split into left and right 4-bit halves
        boolean[] left = new boolean[4];
        boolean[] right = new boolean[4];
        System.arraycopy(ip, 0, left, 0, 4);
        System.arraycopy(ip, 4, right, 0, 4);
        // Round 1: f(right, k2) XOR left (reverse subkey order)
        boolean[] f = fFunction(right, subkeys[1]);
        boolean[] newLeft = xor(left, f);
        // Swap: right becomes old left, left becomes newLeft
        boolean[] temp = right;
        right = newLeft;
        left = temp;
        // Round 2: f(right, k1) XOR left
        f = fFunction(right, subkeys[0]);
        newLeft = xor(left, f);
        // Combine halves
        boolean[] combined = new boolean[8];
        System.arraycopy(newLeft, 0, combined, 0, 4);
        System.arraycopy(right, 0, combined, 4, 4);
        // Apply inverse permutation
        return permute(combined, IP_INV);
    }

    /**
     * Converts a byte to an 8-bit boolean array.
     * The algorithm extracts each bit from the byte, with bit 0 as MSB.
     *
     * @param b the input byte
     * @return the 8-bit boolean array
     */
    public static boolean[] byteToBits(byte b) {
        boolean[] bits = new boolean[8];
        // Extract bits: shift right by i, mask with 1 (e.g., b=5 → 00000101)
        for (int i = 0; i < 8; i++) {
            bits[7 - i] = ((b >> i) & 1) == 1;
        }
        return bits;
    }

    /**
     * Converts an 8-bit boolean array to a byte.
     * The algorithm sets bits in the byte based on the boolean values, with bit 0 as MSB.
     *
     * @param bits the 8-bit boolean array
     * @return the resulting byte
     */
    public static byte bitsToByte(boolean[] bits) {
        byte b = 0;
        // Build byte: set bit (7-i) if bits[i] is true (e.g., [true, false] → 10000000)
        for (int i = 0; i < 8; i++) {
            if (bits[i]) {
                b |= 1 << (7 - i);
            }
        }
        return b;
    }

    /**
     * Converts a binary string to a 10-bit key.
     * The algorithm interprets '1' as true, '0' as false, padding with false if short.
     *
     * @param keyStr the binary string (0s and 1s)
     * @return the 10-bit boolean key
     */
    public static boolean[] stringToKey(String keyStr) {
        boolean[] key = new boolean[10];
        // Convert '1' to true, '0' to false, stop at 10 or string length
        for (int i = 0; i < 10 && i < keyStr.length(); i++) {
            key[i] = keyStr.charAt(i) == '1';
        }
        return key;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 10-bit key (e.g., 1010000010):");
        String keyStr = scanner.nextLine();

        // Validate key: 10 characters, only 0s and 1s
        if (keyStr.length() != 10 || !keyStr.matches("[01]+")) {
            System.out.println("Error: Key must be 10 bits (0s and 1s).");
            return;
        }

        try {
            // Test 1: File input
            System.out.println("--------------------------------");
            String inputText = FileManager.readInputFile(Locale.EN);
            byte[] inputBytes = inputText.getBytes();
            byte[] inputEnc = new byte[inputBytes.length];
            // Encrypt each byte with fixed key "1100110011"
            for (int i = 0; i < inputBytes.length; i++) {
                inputEnc[i] = bitsToByte(encryptBlock(byteToBits(inputBytes[i]), stringToKey(keyStr)));
            }
            FileManager.writeOutputFile(inputEnc.toString());
            byte[] inputDec = new byte[inputEnc.length];
            // Decrypt each byte
            for (int i = 0; i < inputEnc.length; i++) {
                inputDec[i] = bitsToByte(decryptBlock(byteToBits(inputEnc[i]), stringToKey(keyStr)));
            }
            String inputResult = new String(inputDec);
            System.out.println("Test 1 decrypted: " + inputResult);
            System.out.println("Test 1 matches: " + inputText.equals(inputResult));

            // Test 2: Fixed string
            System.out.println("--------------------------------");
            String test1 = "BIMBABOMBA101010LALAAL";
            byte[] test1Bytes = test1.getBytes();
            byte[] test1Enc = new byte[test1Bytes.length];
            // Encrypt with same key
            for (int i = 0; i < test1Bytes.length; i++) {
                test1Enc[i] = bitsToByte(encryptBlock(byteToBits(test1Bytes[i]), stringToKey("1100110011")));
            }
            byte[] test1Dec = new byte[test1Enc.length];
            // Decrypt
            for (int i = 0; i < test1Enc.length; i++) {
                test1Dec[i] = bitsToByte(decryptBlock(byteToBits(test1Enc[i]), stringToKey("1100110011")));
            }
            String test1Result = new String(test1Dec);
            System.out.println("Test 1 decrypted: " + test1Result);
            System.out.println("Test 1 matches: " + test1.equals(test1Result));

            // Test 3: Another string with different key
            System.out.println("--------------------------------");
            String test2 = "HELLO0MY0NAME0IS0IVAN";
            byte[] test2Bytes = test2.getBytes();
            byte[] test2Enc = new byte[test2Bytes.length];
            // Encrypt with key "0011110000"
            for (int i = 0; i < test2Bytes.length; i++) {
                test2Enc[i] = bitsToByte(encryptBlock(byteToBits(test2Bytes[i]), stringToKey("0011110000")));
            }
            byte[] test2Dec = new byte[test2Enc.length];
            // Decrypt
            for (int i = 0; i < test2Enc.length; i++) {
                test2Dec[i] = bitsToByte(decryptBlock(byteToBits(test2Enc[i]), stringToKey("0011110000")));
            }
            String test2Result = new String(test2Dec);
            System.out.println("Test 2 decrypted: " + test2Result);
            System.out.println("Test 2 matches: " + test2.equals(test2Result));
            System.out.println("--------------------------------");

        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        }
    }
}