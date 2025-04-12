package com.ivan.lab4;

import com.ivan.utils.FileManager;
import com.ivan.utils.Locale;

import java.io.IOException;
import java.util.Scanner;

public class SDESCipher {
    private static final int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    private static final int[] IP_INV = {4, 1, 3, 5, 7, 2, 8, 6};
    private static final int[] EP = {4, 1, 2, 3, 2, 3, 4, 1};
    private static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    private static final int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    private static final int[] P4 = {2, 4, 3, 1};
    public static final int[][] S0 = {
        {1, 0, 3, 2}, {3, 2, 1, 0}, {0, 2, 1, 3}, {3, 1, 3, 2}
    };
    public static final int[][] S1 = {
        {0, 1, 2, 3}, {2, 0, 1, 3}, {3, 0, 1, 2}, {2, 1, 0, 3}
    };

    private static boolean[] permute(boolean[] input, int[] table) {
        boolean[] output = new boolean[table.length];
        for (int i = 0; i < table.length; i++) {
            output[i] = input[table[i] - 1];
        }
        return output;
    }

    private static boolean[] leftShift(boolean[] input, int shift) {
        boolean[] output = new boolean[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[(i + shift) % input.length];
        }
        return output;
    }

    private static boolean[] xor(boolean[] a, boolean[] b) {
        boolean[] result = new boolean[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] != b[i];
        }
        return result;
    }

    private static boolean[] sBox(boolean[] input, int[][] sBox) {
        int row = (input[0] ? 2 : 0) + (input[3] ? 1 : 0);
        int col = (input[1] ? 2 : 0) + (input[2] ? 1 : 0);
        int value = sBox[row][col];
        boolean[] output = new boolean[2];
        output[0] = (value & 2) != 0;
        output[1] = (value & 1) != 0;
        return output;
    }

    private static boolean[] fFunction(boolean[] right, boolean[] subkey) {
        boolean[] expanded = permute(right, EP);
        boolean[] xored = xor(expanded, subkey);
        boolean[] s0Input = {xored[0], xored[1], xored[2], xored[3]};
        boolean[] s1Input = {xored[4], xored[5], xored[6], xored[7]};
        boolean[] s0Output = sBox(s0Input, S0);
        boolean[] s1Output = sBox(s1Input, S1);
        boolean[] combined = new boolean[4];
        System.arraycopy(s0Output, 0, combined, 0, 2);
        System.arraycopy(s1Output, 0, combined, 2, 2);
        return permute(combined, P4);
    }

    private static boolean[][] generateSubkeys(boolean[] key) {
        boolean[] permuted = permute(key, P10);
        boolean[] left = new boolean[5];
        boolean[] right = new boolean[5];
        System.arraycopy(permuted, 0, left, 0, 5);
        System.arraycopy(permuted, 5, right, 0, 5);
        left = leftShift(left, 1);
        right = leftShift(right, 1);
        boolean[] combined1 = new boolean[10];
        System.arraycopy(left, 0, combined1, 0, 5);
        System.arraycopy(right, 0, combined1, 5, 5);
        boolean[] k1 = permute(combined1, P8);
        left = leftShift(left, 2);
        right = leftShift(right, 2);
        boolean[] combined2 = new boolean[10];
        System.arraycopy(left, 0, combined2, 0, 5);
        System.arraycopy(right, 0, combined2, 5, 5);
        boolean[] k2 = permute(combined2, P8);
        return new boolean[][]{k1, k2};
    }

    public static boolean[] encryptBlock(boolean[] block, boolean[] key) {
        boolean[][] subkeys = generateSubkeys(key);
        boolean[] ip = permute(block, IP);
        boolean[] left = new boolean[4];
        boolean[] right = new boolean[4];
        System.arraycopy(ip, 0, left, 0, 4);
        System.arraycopy(ip, 4, right, 0, 4);
        boolean[] f = fFunction(right, subkeys[0]);
        boolean[] newLeft = xor(left, f);
        boolean[] temp = right;
        right = newLeft;
        left = temp;
        f = fFunction(right, subkeys[1]);
        newLeft = xor(left, f);
        boolean[] combined = new boolean[8];
        System.arraycopy(newLeft, 0, combined, 0, 4);
        System.arraycopy(right, 0, combined, 4, 4);
        return permute(combined, IP_INV);
    }

    public static boolean[] decryptBlock(boolean[] block, boolean[] key) {
        boolean[][] subkeys = generateSubkeys(key);
        boolean[] ip = permute(block, IP);
        boolean[] left = new boolean[4];
        boolean[] right = new boolean[4];
        System.arraycopy(ip, 0, left, 0, 4);
        System.arraycopy(ip, 4, right, 0, 4);
        boolean[] f = fFunction(right, subkeys[1]);
        boolean[] newLeft = xor(left, f);
        boolean[] temp = right;
        right = newLeft;
        left = temp;
        f = fFunction(right, subkeys[0]);
        newLeft = xor(left, f);
        boolean[] combined = new boolean[8];
        System.arraycopy(newLeft, 0, combined, 0, 4);
        System.arraycopy(right, 0, combined, 4, 4);
        return permute(combined, IP_INV);
    }

    public static boolean[] byteToBits(byte b) {
        boolean[] bits = new boolean[8];
        for (int i = 0; i < 8; i++) {
            bits[7 - i] = ((b >> i) & 1) == 1;
        }
        return bits;
    }

    public static byte bitsToByte(boolean[] bits) {
        byte b = 0;
        for (int i = 0; i < 8; i++) {
            if (bits[i]) {
                b |= 1 << (7 - i);
            }
        }
        return b;
    }

    public static boolean[] stringToKey(String keyStr) {
        boolean[] key = new boolean[10];
        for (int i = 0; i < 10 && i < keyStr.length(); i++) {
            key[i] = keyStr.charAt(i) == '1';
        }
        return key;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 10-bit key (e.g., 1010000010):");
        String keyStr = scanner.nextLine();

        if (keyStr.length() != 10 || !keyStr.matches("[01]+")) {
            System.out.println("Error: Key must be 10 bits (0s and 1s).");
            return;
        }

        try {
            //test1
            String inputText = FileManager.readInputFile(Locale.EN);
            byte[] inputBytes = inputText.getBytes();
            byte[] inputEnc = new byte[inputBytes.length];
            for (int i = 0; i < inputBytes.length; i++) {
                inputEnc[i] = bitsToByte(encryptBlock(byteToBits(inputBytes[i]), stringToKey("1100110011")));
            }
            FileManager.writeOutputFile(inputEnc.toString());
            byte[] inputDec = new byte[inputEnc.length];
            for (int i = 0; i < inputEnc.length; i++) {
                inputDec[i] = bitsToByte(decryptBlock(byteToBits(inputEnc[i]), stringToKey("1100110011")));
            }
            String inputResult = new String(inputDec);
            System.out.println("Test 1 decrypted: " + inputResult);
            System.out.println("Test 1 matches: " + inputText.equals(inputResult));

            //test2
            String test1 = "BIMBABOMBA101010LALAAL";
            byte[] test1Bytes = test1.getBytes();
            byte[] test1Enc = new byte[test1Bytes.length];
            for (int i = 0; i < test1Bytes.length; i++) {
                test1Enc[i] = bitsToByte(encryptBlock(byteToBits(test1Bytes[i]), stringToKey("1100110011")));
            }
            byte[] test1Dec = new byte[test1Enc.length];
            for (int i = 0; i < test1Enc.length; i++) {
                test1Dec[i] = bitsToByte(decryptBlock(byteToBits(test1Enc[i]), stringToKey("1100110011")));
            }
            String test1Result = new String(test1Dec);
            System.out.println("Test 1 decrypted: " + test1Result);
            System.out.println("Test 1 matches: " + test1.equals(test1Result));

            //test3
            String test2 = "HELLO0MY0NAME0IS0IVAN";
            byte[] test2Bytes = test2.getBytes();
            byte[] test2Enc = new byte[test2Bytes.length];
            for (int i = 0; i < test2Bytes.length; i++) {
                test2Enc[i] = bitsToByte(encryptBlock(byteToBits(test2Bytes[i]), stringToKey("0011110000")));
            }
            byte[] test2Dec = new byte[test2Enc.length];
            for (int i = 0; i < test2Enc.length; i++) {
                test2Dec[i] = bitsToByte(decryptBlock(byteToBits(test2Enc[i]), stringToKey("0011110000")));
            }
            String test2Result = new String(test2Dec);
            System.out.println("Test 2 decrypted: " + test2Result);
            System.out.println("Test 2 matches: " + test2.equals(test2Result));

        } catch (IOException e) {
            System.out.println("File operation error: " + e.getMessage());
        }
    }
}