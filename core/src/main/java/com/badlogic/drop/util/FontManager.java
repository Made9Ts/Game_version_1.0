package com.badlogic.drop.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

/**
 * Менеджер шрифтов для улучшенного качества текста в игре
 * Поддерживает как стандартные BitmapFont, так и генерацию из TTF через FreeType
 */
public class FontManager implements Disposable {
    // Шрифты
    private BitmapFont titleFont;
    private BitmapFont uiFont;
    private BitmapFont gameFont;
    private BitmapFont smallFont;
    
    // Генератор шрифтов (используется, если доступна библиотека FreeType)
    private FreeTypeFontGenerator fontGenerator;
    private boolean freeTypeAvailable;
    
    /**
     * Создает менеджер шрифтов и инициализирует базовые шрифты
     */
    public FontManager() {
        // Проверяем доступность FreeType
        try {
            // Пробуем создать генератор - если не получается, 
            // будем использовать стандартные шрифты
            Class.forName("com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator");
            freeTypeAvailable = true;
        } catch (ClassNotFoundException e) {
            freeTypeAvailable = false;
        }
        
        initializeFonts();
    }
    
    /**
     * Инициализация шрифтов
     */
    private void initializeFonts() {
        if (freeTypeAvailable) {
            initializeWithFreeType();
        } else {
            initializeWithBitmapFonts();
        }
    }
    
    /**
     * Инициализирует шрифты с использованием FreeType (если доступен)
     */
    private void initializeWithFreeType() {
        try {
            // Создаем внутренний шрифт, который встроен в LibGDX
            fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/roboto.ttf"));
            
            // Параметры для заголовков
            FreeTypeFontParameter titleParams = new FreeTypeFontParameter();
            titleParams.size = 72;
            titleParams.color = Color.WHITE;
            titleParams.borderWidth = 2;
            titleParams.borderColor = Color.BLACK;
            titleParams.shadowOffsetX = 3;
            titleParams.shadowOffsetY = 3;
            titleParams.shadowColor = new Color(0, 0, 0, 0.5f);
            titleParams.minFilter = Texture.TextureFilter.Linear;
            titleParams.magFilter = Texture.TextureFilter.Linear;
            titleParams.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_+-=[]{}|;:,.<>/?\"'\\";
            
            // Параметры для UI шрифта
            FreeTypeFontParameter uiParams = new FreeTypeFontParameter();
            uiParams.size = 36;
            uiParams.color = Color.WHITE;
            uiParams.borderWidth = 1.5f;
            uiParams.borderColor = Color.BLACK;
            uiParams.minFilter = Texture.TextureFilter.Linear;
            uiParams.magFilter = Texture.TextureFilter.Linear;
            uiParams.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_+-=[]{}|;:,.<>/?\"'\\";
            
            // Параметры для игрового шрифта
            FreeTypeFontParameter gameParams = new FreeTypeFontParameter();
            gameParams.size = 28;
            gameParams.color = Color.WHITE;
            gameParams.borderWidth = 1;
            gameParams.borderColor = Color.BLACK;
            gameParams.minFilter = Texture.TextureFilter.Linear;
            gameParams.magFilter = Texture.TextureFilter.Linear;
            gameParams.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_+-=[]{}|;:,.<>/?\"'\\";
            
            // Параметры для маленького шрифта
            FreeTypeFontParameter smallParams = new FreeTypeFontParameter();
            smallParams.size = 18;
            smallParams.color = Color.WHITE;
            smallParams.borderWidth = 0.5f;
            smallParams.borderColor = Color.BLACK;
            smallParams.minFilter = Texture.TextureFilter.Linear;
            smallParams.magFilter = Texture.TextureFilter.Linear;
            smallParams.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_+-=[]{}|;:,.<>/?\"'\\";
            
            // Генерируем шрифты
            titleFont = fontGenerator.generateFont(titleParams);
            uiFont = fontGenerator.generateFont(uiParams);
            gameFont = fontGenerator.generateFont(gameParams);
            smallFont = fontGenerator.generateFont(smallParams);
            
        } catch (Exception e) {
            // В случае ошибки, переходим на стандартные шрифты
            Gdx.app.error("FontManager", "Error initializing FreeType fonts", e);
            initializeWithBitmapFonts();
        }
    }
    
    /**
     * Инициализирует стандартные шрифты LibGDX с улучшенным качеством
     */
    private void initializeWithBitmapFonts() {
        // Создаем шрифты из стандартного BitmapFont
        titleFont = new BitmapFont();
        uiFont = new BitmapFont();
        gameFont = new BitmapFont();
        smallFont = new BitmapFont();
        
        // Устанавливаем масштаб для каждого шрифта
        titleFont.getData().setScale(3.0f);
        uiFont.getData().setScale(2.5f);
        gameFont.getData().setScale(2.0f);
        smallFont.getData().setScale(1.5f);
        
        // Применяем линейную фильтрацию для сглаживания
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        uiFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        gameFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        smallFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Отключаем привязку к целым пикселям для более гладкого рендеринга
        titleFont.setUseIntegerPositions(false);
        uiFont.setUseIntegerPositions(false);
        gameFont.setUseIntegerPositions(false);
        smallFont.setUseIntegerPositions(false);
    }
    
    /**
     * Получить шрифт для заголовков
     */
    public BitmapFont getTitleFont() {
        return titleFont;
    }
    
    /**
     * Получить шрифт для UI элементов
     */
    public BitmapFont getUIFont() {
        return uiFont;
    }
    
    /**
     * Получить шрифт для игрового текста
     */
    public BitmapFont getGameFont() {
        return gameFont;
    }
    
    /**
     * Получить мелкий шрифт
     */
    public BitmapFont getSmallFont() {
        return smallFont;
    }
    
    /**
     * Расчет ширины текста с использованием указанного шрифта
     */
    public float getTextWidth(BitmapFont font, String text) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);
        return layout.width;
    }
    
    /**
     * Расчет высоты текста с использованием указанного шрифта
     */
    public float getTextHeight(BitmapFont font, String text) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);
        return layout.height;
    }
    
    /**
     * Сбрасывает цвета всех шрифтов на белый
     * Полезно вызывать при переходе между экранами
     */
    public void resetFontColors() {
        if (titleFont != null) titleFont.setColor(Color.WHITE);
        if (uiFont != null) uiFont.setColor(Color.WHITE);
        if (gameFont != null) gameFont.setColor(Color.WHITE);
        if (smallFont != null) smallFont.setColor(Color.WHITE);
    }
    
    /**
     * Освобождение ресурсов
     */
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (uiFont != null) uiFont.dispose();
        if (gameFont != null) gameFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
    }
} 