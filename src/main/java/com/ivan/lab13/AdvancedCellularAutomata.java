package com.ivan.lab13;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * Клас, що реалізує розширену версію клітинного автомата з більш складними правилами
 * та додатковими перемішуваннями для покращення криптографічних властивостей
 */
public class AdvancedCellularAutomata {
    private boolean[] cells;
    private int size;
    private final int[] rules;
    private int ruleIndex;
    private int[] outputIndices;
    private final int NUM_ITERATIONS = 8; // Зменшено кількість ітерацій для підвищення продуктивності
    private final Random random;

    // Константа для розміру буфера при файлових операціях
    private static final int BUFFER_SIZE = 8192;

    /**
     * Створює розширений КА з більш складними правилами
     *
     * @param size розмір КА
     * @param seed пароль для ініціалізації
     * @param customRules конкретні правила для використання (номери за Вольфрамом)
     */
    public AdvancedCellularAutomata(int size, String seed, int[] customRules) {
        this.size = size;
        this.cells = new boolean[size];
        this.rules = (customRules != null && customRules.length > 0) ?
                customRules : new int[]{150, 30, 90, 22, 54, 86, 158, 135};
        this.ruleIndex = 0;
        this.random = new Random(seed.hashCode()); // Детермінований випадковий генератор для підвищення надійності

        // Ініціалізація КА на основі пароля
        initializeFromSeed(seed);

        // Підготовка індексів для виводу ключового потоку
        initializeOutputIndices(seed);

        // Початкові ітерації для кращого перемішування (зменшено для підвищення продуктивності)
        for (int i = 0; i < 50; i++) {
            evolve();
        }
    }

    /**
     * Ініціалізує клітини КА на основі пароля
     */
    private void initializeFromSeed(String seed) {
        try {
            // Використання SHA-256 для отримання початкового стану з пароля
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes());

            // Заповнення клітинного автомата
            for (int i = 0; i < size; i++) {
                cells[i] = (hash[i % hash.length] & (1 << (i % 8))) != 0;
            }

            // Додаткове перемішування (зменшено кількість ітерацій)
            for (int i = 0; i < 3; i++) {
                md.reset();
                md.update(hash);
                hash = md.digest();

                // Перемішування бітів
                for (int j = 0; j < hash.length && j < size / 8; j++) {
                    for (int k = 0; k < 8 && j * 8 + k < size; k++) {
                        cells[j * 8 + k] ^= ((hash[j] >> k) & 1) == 1;
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            // Запасний варіант ініціалізації
            for (int i = 0; i < size; i++) {
                cells[i] = random.nextBoolean();
            }
        }
    }

    /**
     * Ініціалізація індексів для витягання бітів ключового потоку
     */
    private void initializeOutputIndices(String seed) {
        // Вибираємо біти з різних частин КА для більшої криптостійкості
        outputIndices = new int[8];

        // Генеруємо індекси, які добре розподілені по масиву
        for (int i = 0; i < outputIndices.length; i++) {
            outputIndices[i] = (size / outputIndices.length) * i + random.nextInt(size / outputIndices.length);
        }
    }

    /**
     * Застосовує правило Вольфрама до конкретної клітини
     */
    private boolean applyRule(boolean left, boolean center, boolean right, int rule) {
        // Обчислення індексу правила (кодування стану сусідства)
        int index = (left ? 4 : 0) | (center ? 2 : 0) | (right ? 1 : 0);

        // Перевірка відповідного біта у правилі
        return (rule & (1 << index)) != 0;
    }

    /**
     * Застосовує спеціальну логічну форму для конкретного правила
     * замість використання арифметичної форми
     */
    private boolean applyRuleLogic(boolean a, boolean b, boolean c, int ruleNumber) {
        switch (ruleNumber) {
            case 22: // b' = a⊕a∧b∧c⊕b⊕c
                return a ^ (a && b && c) ^ b ^ c;

            case 30: // b' = a⊕(b∨c)
                return a ^ (b || c);

            case 54: // b' = (a∨c)⊕b
                return (a || c) ^ b;

            case 86: // b' = (a∨b)⊕c
                return (a || b) ^ c;

            case 90: // b' = a⊕c
                return a ^ c;

            case 135: // b' = 1∨a⊕b∨c
                return true ^ a ^ b ^ c;

            case 150: // b' = a⊕b⊕c
                return a ^ b ^ c;

            case 158: // b' = a⊕b⊕c∨b∧c
                return a ^ b ^ c || (b && c);

            default: // Для невідомих правил використовуємо стандартний метод
                return applyRule(a, b, c, ruleNumber);
        }
    }

    /**
     * Виконує одну ітерацію еволюції КА
     */
    public void evolve() {
        boolean[] newCells = new boolean[size];
        int rule = rules[ruleIndex];

        for (int i = 0; i < size; i++) {
            boolean left = cells[(i - 1 + size) % size];    // Зліва
            boolean center = cells[i];                      // Поточна клітина
            boolean right = cells[(i + 1) % size];          // Справа

            // Застосовуємо конкретне правило
            newCells[i] = applyRuleLogic(left, center, right, rule);
        }

        // Оновлення стану КА
        cells = newCells;

        // Перехід до наступного правила для більшої ентропії
        ruleIndex = (ruleIndex + 1) % rules.length;
    }

    /**
     * Додаткова операція для підвищення ентропії КА
     */
    private void enhanceEntropy() {
        // Використовуємо детермінований random замість Math.random()

        // Додаткове збурення для уникнення циклічних патернів
        if (random.nextFloat() < 0.05) { // 5% ймовірність
            int flipIndex = random.nextInt(size);
            cells[flipIndex] = !cells[flipIndex];
        }

        // Додаткове перемішування: XOR між віддаленими клітинами (зменшено кількість операцій)
        for (int i = 0; i < size / 4; i++) {
            int index1 = random.nextInt(size);
            int index2 = (index1 + size/2) % size; // Протилежний бік
            cells[index1] = cells[index1] ^ cells[index2];
        }
    }

    /**
     * Генерує один байт ключового потоку
     */
    public byte generateByte() {
        // Виконуємо кілька ітерацій для кращого перемішування
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            evolve();
        }

        // Іноді підвищуємо ентропію
        if (random.nextFloat() < 0.1) { // 10% ймовірність
            enhanceEntropy();
        }

        // Формуємо байт, збираючи біти з різних частин КА
        byte result = 0;
        for (int i = 0; i < 8; i++) {
            if (cells[outputIndices[i]]) {
                result |= (1 << i);
            }
        }

        return result;
    }

    /**
     * Генерує масив байтів для використання як ключовий потік
     */
    public void generateKeyStream(byte[] keyStream, int length) {
        for (int i = 0; i < length; i++) {
            keyStream[i] = generateByte();
        }
    }

    /**
     * Допоміжна функція для шифрування/дешифрування файлу
     */
    public static void encryptFile(String inputFile, String outputFile, String password, int[] rules)
            throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Використовуємо менший розмір КА для підвищення продуктивності
            AdvancedCellularAutomata ca = new AdvancedCellularAutomata(512, password, rules);

            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] keyStream = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesProcessed = 0;
            long fileSize = new File(inputFile).length();

            System.out.println("Початок шифрування файлу: " + inputFile);
            System.out.println("Загальний розмір файлу: " + formatFileSize(fileSize));

            while ((bytesRead = fis.read(buffer)) != -1) {
                // Генерація ключового потоку
                ca.generateKeyStream(keyStream, bytesRead);

                // XOR для шифрування/дешифрування
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) (buffer[i] ^ keyStream[i]);
                }

                fos.write(buffer, 0, bytesRead);

                // Виводимо прогрес
                totalBytesProcessed += bytesRead;
                if (totalBytesProcessed % (1024 * 1024) == 0) { // Показуємо прогрес кожен мегабайт
                    double progress = (double) totalBytesProcessed / fileSize * 100;
                    System.out.printf("Оброблено: %s (%.2f%%)\n",
                            formatFileSize(totalBytesProcessed), progress);
                }
            }

            System.out.println("Шифрування завершено: " + outputFile);
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
}