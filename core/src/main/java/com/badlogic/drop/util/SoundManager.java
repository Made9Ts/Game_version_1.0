package com.badlogic.drop.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Класс для управления настройками звука и эффектов в игре
 */
public class SoundManager {
    // Константы для хранения ключей в настройках
    private static final String PREFS_NAME = "space_courier_sound_settings";
    private static final String MUSIC_ENABLED_KEY = "music_enabled";
    private static final String SFX_ENABLED_KEY = "sfx_enabled";
    private static final String MUSIC_VOLUME_KEY = "music_volume";
    private static final String SFX_VOLUME_KEY = "sfx_volume";
    
    // Настройки громкости по умолчанию
    private static final float DEFAULT_MUSIC_VOLUME = 0.7f;
    private static final float DEFAULT_SFX_VOLUME = 1.0f;
    
    // Preferences для хранения настроек
    private Preferences preferences;
    
    // Текущие настройки
    private boolean musicEnabled;
    private boolean sfxEnabled;
    private float musicVolume;
    private float sfxVolume;
    
    // Текущая музыка (для быстрого доступа)
    private Music currentMusic;
    
    /**
     * Инициализирует менеджер звука и загружает сохраненные настройки
     */
    public SoundManager() {
        preferences = Gdx.app.getPreferences(PREFS_NAME);
        loadSettings();
    }
    
    /**
     * Загружает настройки из Preferences
     */
    private void loadSettings() {
        musicEnabled = preferences.getBoolean(MUSIC_ENABLED_KEY, true);
        sfxEnabled = preferences.getBoolean(SFX_ENABLED_KEY, true);
        musicVolume = preferences.getFloat(MUSIC_VOLUME_KEY, DEFAULT_MUSIC_VOLUME);
        sfxVolume = preferences.getFloat(SFX_VOLUME_KEY, DEFAULT_SFX_VOLUME);
    }
    
    /**
     * Сохраняет настройки в Preferences
     */
    private void saveSettings() {
        preferences.flush();
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
        saveSettings();
        
        // Если музыка выключена, останавливаем текущую музыку
        if (!enabled && currentMusic != null) {
            currentMusic.pause();
        } else if (enabled && currentMusic != null) {
            currentMusic.play();
        }
    }
    
    /**
     * Включает или выключает звуковые эффекты
     */
    public void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
        preferences.putBoolean(SFX_ENABLED_KEY, enabled);
        saveSettings();
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
    
    /**
     * Устанавливает громкость музыки
     * @param volume Значение от 0.0 до 1.0
     */
    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0, Math.min(1, volume));
        preferences.putFloat(MUSIC_VOLUME_KEY, musicVolume);
        saveSettings();
        
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }
    
    /**
     * Возвращает текущую громкость музыки
     */
    public float getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Устанавливает громкость звуковых эффектов
     * @param volume Значение от 0.0 до 1.0
     */
    public void setSfxVolume(float volume) {
        sfxVolume = Math.max(0, Math.min(1, volume));
        preferences.putFloat(SFX_VOLUME_KEY, sfxVolume);
        saveSettings();
    }
    
    /**
     * Возвращает текущую громкость звуковых эффектов
     */
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Устанавливает текущую музыку и начинает ее проигрывать, если музыка включена
     */
    public void setMusic(Music music, boolean looping) {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
        
        currentMusic = music;
        
        if (currentMusic != null) {
            currentMusic.setLooping(looping);
            currentMusic.setVolume(musicVolume);
            
            if (musicEnabled) {
                currentMusic.play();
            }
        }
    }
    
    /**
     * Проигрывает звуковой эффект, если звуковые эффекты включены
     * @return ID звукового эффекта или -1, если звук не проигрывается
     */
    public long playSound(Sound sound) {
        if (sfxEnabled && sound != null) {
            return sound.play(sfxVolume);
        }
        return -1;
    }
    
    /**
     * Проигрывает звуковой эффект с пользовательскими настройками
     */
    public long playSound(Sound sound, float volume, float pitch, float pan) {
        if (sfxEnabled && sound != null) {
            return sound.play(sfxVolume * volume, pitch, pan);
        }
        return -1;
    }
    
    /**
     * Останавливает текущую музыку
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }
    
    /**
     * Приостанавливает текущую музыку
     */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }
    
    /**
     * Возобновляет проигрывание текущей музыки, если музыка включена
     */
    public void resumeMusic() {
        if (musicEnabled && currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }
} 