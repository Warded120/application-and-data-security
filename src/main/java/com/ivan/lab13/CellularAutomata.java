package com.ivan.lab13;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * Клас, що реалізує одновимірний клітинний автомат
 */
class CellularAutomata {
    private boolean[] cells;
    private int size;
    private int[] rules = {150, 30, 86, 22}; // Використовуємо кілька правил з таблиці
    private int currentRule = 0;
    
    /**
     * Створює КА заданого розміру та ініціалізує його за допомогою пароля
     */
    public CellularAutomata(int size, String seed) {
        this.size = size;
        this.cells = new boolean[size];
        
        // Ініціалізація клітин на основі пароля
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes());
            
            for (int i = 0; i < size; i++) {
                cells[i] = (hash[i % hash.length] & (1 << (i % 8))) != 0;
            }
        } catch (NoSuchAlgorithmException e) {
            // У випадку помилки використовуємо простішу ініціалізацію
            Random random = new Random(seed.hashCode());
            for (int i = 0; i < size; i++) {
                cells[i] = random.nextBoolean();
            }
        }
    }
    
    /**
     * Застосовує правило за Вольфрамом до клітин КА
     */
    private boolean applyRule(boolean left, boolean center, boolean right, int rule) {
        int index = (left ? 4 : 0) | (center ? 2 : 0) | (right ? 1 : 0);
        return (rule & (1 << index)) != 0;
    }
    
    /**
     * Виконує одну ітерацію еволюції КА
     */
    public void evolve() {
        boolean[] newCells = new boolean[size];
        int rule = rules[currentRule];
        
        for (int i = 0; i < size; i++) {
            boolean left = cells[(i - 1 + size) % size];
            boolean center = cells[i];
            boolean right = cells[(i + 1) % size];
            
            newCells[i] = applyRule(left, center, right, rule);
        }
        
        cells = newCells;
        currentRule = (currentRule + 1) % rules.length; // Міняємо правило для наступної ітерації
    }
    
    /**
     * Генерує ключовий потік заданої довжини
     */
    public void generateKeyStream(byte[] keyStream, int length) {
        Arrays.fill(keyStream, (byte) 0);
        
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            // Одна еволюція КА для кожного байта
            evolve();
            
            // Збираємо 8 бітів для формування байта
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                // Вибираємо біти з різних місць КА для кращої випадковості
                int cellIndex = (17 * byteIndex + 23 * bitIndex) % size;
                
                if (cells[cellIndex]) {
                    keyStream[byteIndex] |= (1 << bitIndex);
                }
            }
        }
    }
}