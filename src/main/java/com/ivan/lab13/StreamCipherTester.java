package com.ivan.lab13;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Головний клас для тестування потокових шифрів з різними генераторами
 */
public class StreamCipherTester {
    // Константи
    private static final int MIN_FILE_SIZE_MB = 13; // мінімум 12.5MB для NIST STS тестування
    private static final int BUFFER_SIZE = 8192;    // розмір буфера для файлових операцій

    // Різні конфігурації правил для клітинних автоматів
    private static final int[] RULE_SET_1 = {150, 30, 90, 22};  // Базовий набір правил
    private static final int[] RULE_SET_2 = {22, 30, 54, 86, 150, 158}; // Розширений набір
    private static final int[] RULE_SET_3 = {90, 105, 150, 165}; // Додатковий набір правил

    /**
     * Створює файл із випадковими даними заданого розміру
     */
    private static void createRandomFile(String filePath, long size) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            SecureRandom random = new SecureRandom();
            byte[] buffer = new byte[BUFFER_SIZE];

            long bytesWritten = 0;
            while (bytesWritten < size) {
                random.nextBytes(buffer);
                int bytesToWrite = (int) Math.min(buffer.length, size - bytesWritten);
                fos.write(buffer, 0, bytesToWrite);
                bytesWritten += bytesToWrite;

                // Виводимо прогрес
                if (bytesWritten % (5 * 1024 * 1024) == 0) {
                    System.out.println("Записано " + formatFileSize(bytesWritten));
                }
            }
        }
    }

    /**
     * Шифрує файл з використанням SHA-1 як генератора ключового потоку
     */
    private static void encryptWithSHA(String inputFile, String outputFile, String password)
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
            long totalBytesProcessed = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                // Генерація ключового потоку з SHA-1
                generateSHAKeyStream(sha, counter, keyStream, bytesRead);
                counter++;

                // XOR для шифрування
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) (buffer[i] ^ keyStream[i]);
                }

                fos.write(buffer, 0, bytesRead);

                // Виводимо прогрес
                totalBytesProcessed += bytesRead;
                if (totalBytesProcessed % (5 * 1024 * 1024) == 0) {
                    System.out.println("Оброблено " + formatFileSize(totalBytesProcessed));
                }
            }
        }
    }

    /**
     * Генерує ключовий потік на основі SHA-1
     */
    private static void generateSHAKeyStream(MessageDigest sha, long counter, byte[] keyStream, int length)
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
     * Шифрує файл з використанням простого клітинного автомата
     */
    private static void encryptWithSimpleCA(String inputFile, String outputFile, String password)
            throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Створюємо простий КА з 256 клітинами
            SimpleCA ca = new SimpleCA(256, password);

            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] keyStream = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesProcessed = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                // Генерація ключового потоку
                ca.generateKeyStream(keyStream, bytesRead);

                // XOR для шифрування
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) (buffer[i] ^ keyStream[i]);
                }

                fos.write(buffer, 0, bytesRead);

                // Виводимо прогрес
                totalBytesProcessed += bytesRead;
                if (totalBytesProcessed % (5 * 1024 * 1024) == 0) {
                    System.out.println("Оброблено " + formatFileSize(totalBytesProcessed));
                }
            }
        }
    }

    /**
     * Порівнює два файли побайтово
     */
    private static boolean compareFiles(String file1, String file2) throws IOException {
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
     * Форматує розмір файлу у людиночитабельний вигляд
     */
    private static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Конвертує long в массив байтів
     */
    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== Демонстрація потокових шифрів ===");

            // Крок 1: Створення тестового файлу
            System.out.println("\nСтворення тестового файлу...");
            String inputFilePath = "input.dat";
            createRandomFile(inputFilePath, MIN_FILE_SIZE_MB * 1024 * 1024);
            System.out.println("Тестовий файл створено: " + inputFilePath);
            System.out.println("Розмір: " + formatFileSize(new File(inputFilePath).length()));

            // Задаємо пароль для всіх шифрів
            String password = "MySecretPassword123!";

            // Крок 2: Шифрування з використанням стандартного генератора (SHA-1)
            System.out.println("\n=== Шифрування з використанням SHA-1 генератора ===");
            String shaEncryptedFilePath = "sha_encrypted.dat";
            long shaStartTime = System.currentTimeMillis();
            encryptWithSHA(inputFilePath, shaEncryptedFilePath, password);
            long shaEndTime = System.currentTimeMillis();
            System.out.println("Файл зашифровано за допомогою SHA-1: " + shaEncryptedFilePath);
            System.out.println("Час шифрування: " + (shaEndTime - shaStartTime) + " мс");

            // Крок 3: Шифрування з використанням простого генератора на основі КА
            System.out.println("\n=== Шифрування з використанням простого КА ===");
            String caEncryptedFilePath = "ca_encrypted.dat";
            long caStartTime = System.currentTimeMillis();
            encryptWithSimpleCA(inputFilePath, caEncryptedFilePath, password);
            long caEndTime = System.currentTimeMillis();
            System.out.println("Файл зашифровано за допомогою простого КА: " + caEncryptedFilePath);
            System.out.println("Час шифрування: " + (caEndTime - caStartTime) + " мс");

            // Крок 4: Шифрування з використанням розширеного КА (різні набори правил)
            System.out.println("\n=== Шифрування з використанням розширеного КА (набір 1) ===");
            String advCaEncryptedFilePath1 = "adv_ca_encrypted_set1.dat";
            long advCaStartTime1 = System.currentTimeMillis();
            AdvancedCellularAutomata.encryptFile(inputFilePath, advCaEncryptedFilePath1, password, RULE_SET_1);
            long advCaEndTime1 = System.currentTimeMillis();
            System.out.println("Файл зашифровано: " + advCaEncryptedFilePath1);
            System.out.println("Час шифрування: " + (advCaEndTime1 - advCaStartTime1) + " мс");

            System.out.println("\n=== Шифрування з використанням розширеного КА (набір 2) ===");
            String advCaEncryptedFilePath2 = "adv_ca_encrypted_set2.dat";
            long advCaStartTime2 = System.currentTimeMillis();
            AdvancedCellularAutomata.encryptFile(inputFilePath, advCaEncryptedFilePath2, password, RULE_SET_2);
            long advCaEndTime2 = System.currentTimeMillis();
            System.out.println("Файл зашифровано: " + advCaEncryptedFilePath2);
            System.out.println("Час шифрування: " + (advCaEndTime2 - advCaStartTime2) + " мс");

            System.out.println("\n=== Шифрування з використанням розширеного КА (набір 3) ===");
            String advCaEncryptedFilePath3 = "adv_ca_encrypted_set3.dat";
            long advCaStartTime3 = System.currentTimeMillis();
            AdvancedCellularAutomata.encryptFile(inputFilePath, advCaEncryptedFilePath3, password, RULE_SET_3);
            long advCaEndTime3 = System.currentTimeMillis();
            System.out.println("Файл зашифровано: " + advCaEncryptedFilePath3);
            System.out.println("Час шифрування: " + (advCaEndTime3 - advCaStartTime3) + " мс");

            // Додатково: перевірка коректності шифрування через розшифрування
            System.out.println("\n=== Перевірка коректності шифрування ===");

            // Перевіряємо SHA шифр
            String shaDecryptedFilePath = "sha_decrypted.dat";
            encryptWithSHA(shaEncryptedFilePath, shaDecryptedFilePath, password);
            boolean shaSame = compareFiles(inputFilePath, shaDecryptedFilePath);
            System.out.println("SHA розшифрування: " + (shaSame ? "Успішно" : "Помилка"));

            // Перевіряємо простий КА шифр
            String caDecryptedFilePath = "ca_decrypted.dat";
            encryptWithSimpleCA(caEncryptedFilePath, caDecryptedFilePath, password);
            boolean caSame = compareFiles(inputFilePath, caDecryptedFilePath);
            System.out.println("Простий КА розшифрування: " + (caSame ? "Успішно" : "Помилка"));

            // Перевіряємо розширений КА шифр (набір 1)
            String advCaDecryptedFilePath1 = "adv_ca_decrypted_set1.dat";
            AdvancedCellularAutomata.encryptFile(advCaEncryptedFilePath1, advCaDecryptedFilePath1, password, RULE_SET_1);
            boolean advCaSame1 = compareFiles(inputFilePath, advCaDecryptedFilePath1);
            System.out.println("Розширений КА (набір 1) розшифрування: " + (advCaSame1 ? "Успішно" : "Помилка"));

            System.out.println("\n=== Завершено ===");
            System.out.println("Тепер файли можуть бути проаналізовані за допомогою NIST STS:");
            System.out.println("1. " + shaEncryptedFilePath);
            System.out.println("2. " + caEncryptedFilePath);
            System.out.println("3. " + advCaEncryptedFilePath1);
            System.out.println("4. " + advCaEncryptedFilePath2);
            System.out.println("5. " + advCaEncryptedFilePath3);

        } catch (Exception e) {
            System.err.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}