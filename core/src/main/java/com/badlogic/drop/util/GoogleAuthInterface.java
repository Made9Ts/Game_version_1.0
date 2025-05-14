package com.badlogic.drop.util;

/**
 * Интерфейс для взаимодействия с Google Auth из кода LibGDX.
 * Реализуется в платформо-зависимом коде (например, AndroidLauncher).
 */
public interface GoogleAuthInterface {
    /**
     * Выполняет вход через Google аккаунт
     */
    void signIn();
    
    /**
     * Выполняет выход из аккаунта Google
     */
    void signOut();
    
    /**
     * Проверяет, вошел ли пользователь
     * @return true, если пользователь вошел в аккаунт
     */
    boolean isSignedIn();
    
    /**
     * Получает имя пользователя
     * @return Имя пользователя или null, если пользователь не авторизован
     */
    String getUserName();
    
    /**
     * Получает email пользователя
     * @return Email пользователя или null, если пользователь не авторизован
     */
    String getUserEmail();
    
    /**
     * Получает URL фотографии пользователя
     * @return URL фотографии или null, если пользователь не авторизован или фото отсутствует
     */
    String getUserPhotoUrl();
} 