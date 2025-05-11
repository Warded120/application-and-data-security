package com.ivan.lab13;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * Клас, що реалізує простий клітинний автомат (КА) для генерації ключового потоку
 */
public class SimpleCA {
    private boolean[] cells;
    private int size;
    private int[] rules = {150, 30, 90, 22}; // Використовуємо декілька правил
    private int currentRule = 0;
    
    /**
     * Створює КА заданого розміру та ініціалізує його за допомогою пароля
     * 
     * @param size розмір КА
     * @param seed пароль для ініціалізації
     */
    public SimpleCA(int size, String seed) {
        this.size = size;
        this.cells = new boolean[size];
        
        // Ініціалізація клітин на основі пароля
        try {
            // Використовуємо SHA-256 для отримання стійкого початкового стану
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes());
            
            // Розподіляємо біти хешу по клітинам
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
        
        // Виконуємо декілька початкових ітерацій для кращого перемішування
        for (int i = 0; i < 20; i++) {
            evolve();
        }
    }
    
    /**
     * Застосовує правило Вольфрама до клітин КА
     * 
     * @param left ліва клітина
     * @param center центральна клітина
     * @param right права клітина
     * @param rule номер правила за класифікацією Вольфрама
     * @return нове значення для центральної клітини
     */
    private boolean applyRule(boolean left, boolean center, boolean right, int rule) {
        // Обчислюємо індекс правила на основі стану сусідства
        int index = (left ? 4 : 0) | (center ? 2 : 0) | (right ? 1 : 0);
        
        // Перевіряємо відповідний біт у правилі
        return (rule & (1 << index)) != 0;
    }
    
    /**
     * Виконує одну ітерацію еволюції КА
     */
    public void evolve() {
        boolean[] newCells = new boolean[size];
        int rule = rules[currentRule];
        
        for (int i = 0; i < size; i++) {
            // Визначаємо сусідні клітини (з урахуванням циклічної топології)
            boolean left = cells[(i - 1 + size) % size];
            boolean center = cells[i];
            boolean right = cells[(i + 1) % size];
            
            // Обчислюємо новий стан клітини за правилом
            newCells[i] = applyRule(left, center, right, rule);
        }
        
        // Оновлюємо стан КА
        cells = newCells;
        
        // Переходимо до наступного правила для більшої ентропії
        currentRule = (currentRule + 1) % rules.length;
    }
    
    /**
     * Генерує ключовий потік заданої довжини
     * 
     * @param keyStream масив для запису ключового потоку
     * @param length необхідна довжина ключового потоку в байтах
     */
    public void generateKeyStream(byte[] keyStream, int length) {
        Arrays.fill(keyStream, (byte) 0);
        
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            // Одна еволюція КА для кожного байта
            evolve();
            
            // Формуємо один байт з клітин КА
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                // Вибираємо біти з різних місць КА для кращої випадковості
                int cellIndex = (13 * byteIndex + 7 * bitIndex) % size;
                
                if (cells[cellIndex]) {
                    keyStream[byteIndex] |= (1 << bitIndex);
                }
            }
        }
    }
}