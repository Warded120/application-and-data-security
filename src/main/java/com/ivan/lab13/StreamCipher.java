package com.ivan.lab13;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class StreamCipher {

    // Константи для розміру файлу та генерації даних
    private static final int MIN_FILE_SIZE_MB = 13; // мінімум 12.5MB
    private static final int BUFFER_SIZE = 8192; // розмір буфера для файлових операцій

    public static void main(String[] args) {
        try {
            // Крок 1: Створення тестового файлу
            System.out.println("Створення тестового файлу...");
            String inputFilePath = "input.dat";
            createRandomFile(inputFilePath, MIN_FILE_SIZE_MB * 1024 * 1024);
            System.out.println("Тестовий файл створено: " + inputFilePath);

            // Крок 2: Шифрування з використанням стандартного генератора (SHA-1)
            System.out.println("\nШифрування з використанням SHA-1 генератора...");
            String shasEncryptedFilePath = "sha_encrypted.dat";
            encryptWithSHA(inputFilePath, shasEncryptedFilePath, "password123");
            System.out.println("Файл зашифровано за допомогою SHA-1: " + shasEncryptedFilePath);

            // Крок 3: Шифрування з використанням генератора на основі КА
            System.out.println("\nШифрування з використанням генератора на основі КА...");
            String caEncryptedFilePath = "ca_encrypted.dat";
            encryptWithCA(inputFilePath, caEncryptedFilePath, "password123");
            System.out.println("Файл зашифровано за допомогою генератора КА: " + caEncryptedFilePath);

            // Додатково: перевірка правильності роботи шифрування через розшифрування
            System.out.println("\nПеревірка коректності шифрування через розшифрування...");
            String shaDecryptedFilePath = "sha_decrypted.dat";
            encryptWithSHA(shasEncryptedFilePath, shaDecryptedFilePath, "password123"); // XOR з тим самим ключем призводить до розшифрування
            
            String caDecryptedFilePath = "ca_decrypted.dat";
            encryptWithCA(caEncryptedFilePath, caDecryptedFilePath, "password123");
            
            // Перевірка ідентичності розшифрованих даних з оригіналом
            boolean shaSame = compareFiles(inputFilePath, shaDecryptedFilePath);
            boolean caSame = compareFiles(inputFilePath, caDecryptedFilePath);
            
            System.out.println("Результат перевірки SHA шифрування: " + (shaSame ? "Успішно" : "Помилка"));
            System.out.println("Результат перевірки КА шифрування: " + (caSame ? "Успішно" : "Помилка"));
            
        } catch (Exception e) {
            System.err.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Створює файл із випадковими даними заданого розміру
     */
    static void createRandomFile(String filePath, long size) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            SecureRandom random = new SecureRandom();
            byte[] buffer = new byte[BUFFER_SIZE];
            
            long bytesWritten = 0;
            while (bytesWritten < size) {
                random.nextBytes(buffer);
                int bytesToWrite = (int) Math.min(buffer.length, size - bytesWritten);
                fos.write(buffer, 0, bytesToWrite);
                bytesWritten += bytesToWrite;
                
                if (bytesWritten % (1024 * 1024) == 0) {
                    System.out.println("Записано " + (bytesWritten / (1024 * 1024)) + " MB");
                }
            }
        }
    }

    /**
     * Шифрує файл з використанням SHA-1 як генератора ключового потоку
     */
    static void encryptWithSHA(String inputFile, String outputFile, String password)
            throws IOException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = password.getBytes();
            sha.update(key);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] keyStream = new byte[BUFFER_SIZE];
            int bytesRead;
            long counter = 0;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                // Генерація ключового потоку з SHA-1
                generateSHAKeyStream(sha, counter, keyStream, bytesRead);
                counter++;
                
                // XOR для шифрування
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) (buffer[i] ^ keyStream[i]);
                }
                
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     * Генерує ключовий потік на основі SHA-1
     */
    static void generateSHAKeyStream(MessageDigest sha, long counter, byte[] keyStream, int length)
            throws NoSuchAlgorithmException {
        int offset = 0;
        while (offset < length) {
            byte[] counterBytes = longToBytes(counter);
            sha.reset();
            sha.update(counterBytes);
            byte[] hash = sha.digest();
            
            int bytesToCopy = Math.min(hash.length, length - offset);
            System.arraycopy(hash, 0, keyStream, offset, bytesToCopy);
            
            offset += bytesToCopy;
            counter++;
        }
    }
    
    /**
     * Шифрує файл з використанням генератора на основі одновимірного клітинного автомата
     */
    static void encryptWithCA(String inputFile, String outputFile, String password)
            throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            // Ініціалізація КА за допомогою пароля
            CellularAutomata ca = new CellularAutomata(256, password);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] keyStream = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                // Генерація ключового потоку з КА
                ca.generateKeyStream(keyStream, bytesRead);
                
                // XOR для шифрування
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) (buffer[i] ^ keyStream[i]);
                }
                
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     * Порівнює два файли побайтово
     */
    static boolean compareFiles(String file1, String file2) throws IOException {
        try (FileInputStream fis1 = new FileInputStream(file1);
             FileInputStream fis2 = new FileInputStream(file2)) {
            
            byte[] buffer1 = new byte[BUFFER_SIZE];
            byte[] buffer2 = new byte[BUFFER_SIZE];
            int bytesRead1, bytesRead2;
            
            while ((bytesRead1 = fis1.read(buffer1)) != -1) {
                bytesRead2 = fis2.read(buffer2);
                
                if (bytesRead1 != bytesRead2) {
                    return false;
                }
                
                for (int i = 0; i < bytesRead1; i++) {
                    if (buffer1[i] != buffer2[i]) {
                        return false;
                    }
                }
            }
            
            return fis2.read() == -1; // Перевірка, що обидва файли закінчуються
        }
    }
    
    /**
     * Конвертує long в массив байтів
     */
    static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }
}