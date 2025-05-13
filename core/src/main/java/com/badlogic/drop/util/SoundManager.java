package com.badlogic.drop.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Класс для управления настройками звука в игре
 */
public class SoundManager {
    // Константы для хранения ключей в настройках
    private static final String PREFS_NAME = "space_courier_sound_settings";
    private static final String MUSIC_ENABLED_KEY = "music_enabled";
    private static final String SFX_ENABLED_KEY = "sfx_enabled";
    
    // Preferences для хранения настроек
    private Preferences preferences;
    
    // Текущие настройки
    private boolean musicEnabled;
    private boolean sfxEnabled;
    
    public SoundManager() {
        // Инициализация Preferences
        preferences = Gdx.app.getPreferences(PREFS_NAME);
        
        // Загрузка настроек (по умолчанию всё включено)
        musicEnabled = preferences.getBoolean(MUSIC_ENABLED_KEY, true);
        sfxEnabled = preferences.getBoolean(SFX_ENABLED_KEY, true);
    }
    
    /**
     * Проверяет, включена ли музыка
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * Проверяет, включены ли звуковые эффекты
     */
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }
    
    /**
     * Включает или выключает музыку
     */
    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        preferences.putBoolean(MUSIC_ENABLED_KEY, enabled);
        preferences.flush(); // Сохраняем изменения сразу
    }
    
    /**
     * Включает или выключает звуковые эффекты
     */
    public void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
        preferences.putBoolean(SFX_ENABLED_KEY, enabled);
        preferences.flush(); // Сохраняем изменения сразу
    }
    
    /**
     * Переключает состояние музыки
     */
    public boolean toggleMusic() {
        setMusicEnabled(!musicEnabled);
        return musicEnabled;
    }
    
    /**
     * Переключает состояние звуковых эффектов
     */
    public boolean toggleSfx() {
        setSfxEnabled(!sfxEnabled);
        return sfxEnabled;
    }
} 