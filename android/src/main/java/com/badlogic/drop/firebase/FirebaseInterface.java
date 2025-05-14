package com.badlogic.drop.firebase;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * Интерфейс для взаимодействия с Firebase.
 * Используется для сохранения и загрузки данных пользователя,
 * включая достижения и другую игровую статистику.
 */
public interface FirebaseInterface {

    /**
     * Callback для операций загрузки достижений из Firebase
     */
    interface AchievementsCallback {
        /**
         * Вызывается при успешной загрузке достижений
         * @param achievements Карта с достижениями пользователя
         */
        void onAchievementsLoaded(ObjectMap<String, Object> achievements);
        
        /**
         * Вызывается при ошибке загрузки
         * @param error Текст ошибки
         */
        void onError(String error);
    }
    
    /**
     * Callback для операций завершения (сохранения)
     */
    interface CompletionCallback {
        /**
         * Вызывается при успешном завершении операции
         */
        void onSuccess();
        
        /**
         * Вызывается при ошибке операции
         * @param error Текст ошибки
         */
        void onError(String error);
    }
    
    /**
     * Проверяет, авторизован ли пользователь
     * @return true, если пользователь авторизован
     */
    boolean isUserSignedIn();
    
    /**
     * Возвращает ID текущего пользователя
     * @return ID пользователя или null, если пользователь не авторизован
     */
    String getCurrentUserId();
    
    /**
     * Возвращает имя текущего пользователя
     * @return Имя пользователя или null, если пользователь не авторизован
     */
    String getCurrentUserName();
    
    /**
     * Возвращает email текущего пользователя
     * @return Email пользователя или null, если пользователь не авторизован
     */
    String getCurrentUserEmail();
    
    /**
     * Загружает достижения пользователя из Firebase
     * @param userId ID пользователя
     * @param callback Callback для обработки результата
     */
    void getAchievements(String userId, AchievementsCallback callback);
    
    /**
     * Сохраняет достижения пользователя в Firebase
     * @param userId ID пользователя
     * @param achievements Карта с достижениями для сохранения
     * @param callback Callback для обработки результата
     */
    void saveAchievements(String userId, ObjectMap<String, Object> achievements, CompletionCallback callback);
    
    /**
     * Сохраняет игровую статистику пользователя
     * @param userId ID пользователя
     * @param statistics Карта со статистикой
     * @param callback Callback для обработки результата
     */
    void saveGameStatistics(String userId, ObjectMap<String, Object> statistics, CompletionCallback callback);
    
    /**
     * Загружает игровую статистику пользователя
     * @param userId ID пользователя
     * @param callback Callback для обработки результата
     */
    void getGameStatistics(String userId, AchievementsCallback callback);
} 