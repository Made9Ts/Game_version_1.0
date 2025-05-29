package com.badlogic.drop.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * Фабричный класс для создания UI элементов с единым стилем по всему приложению
 */
public class UIFactory {
    
    // Радиус скругления кнопок (в пикселях)
    private static final int BUTTON_CORNER_RADIUS = 20;
    
    /**
     * Создает стиль для кнопок с закругленными углами
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @return TextButtonStyle со скругленными углами
     */
    public static TextButtonStyle createRoundedButtonStyle(Skin skin, BitmapFont font) {
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        
        // Улучшенные цвета для текста кнопок
        style.fontColor = new Color(1, 1, 1, 1);
        style.downFontColor = new Color(0.95f, 0.98f, 1f, 1);     // Ярко-белый при нажатии
        style.overFontColor = new Color(0.9f, 0.97f, 1f, 1);     // Легкое свечение при наведении
        style.disabledFontColor = new Color(0.6f, 0.6f, 0.6f, 0.7f);
        
        // Плоские цвета для фонов кнопок без эффекта выпуклости
        Color upColor = new Color(0.3f, 0.55f, 0.95f, 0.85f);      // Яркий синий
        Color downColor = new Color(0.2f, 0.4f, 0.75f, 0.9f);      // Более темный синий
        Color overColor = new Color(0.4f, 0.6f, 0.98f, 0.85f);     // Светло-синий
        
        // Создаем drawable со скругленными углами для разных состояний кнопки
        // Убираем все эффекты внутренней тени, оставляем только внешнее свечение
        style.up = new RoundedDrawable(upColor, BUTTON_CORNER_RADIUS, false, 0.2f);
        style.down = new RoundedDrawable(downColor, BUTTON_CORNER_RADIUS, false, 0.1f);
        style.over = new RoundedDrawable(overColor, BUTTON_CORNER_RADIUS, false, 0.4f);
        
        return style;
    }
    
    /**
     * Создает стиль для sci-fi кнопок прямоугольной формы
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @return TextButtonStyle в sci-fi стиле
     */
    public static TextButtonStyle createSciFiButtonStyle(Skin skin, BitmapFont font) {
        return createSciFiButtonStyle(skin, font, SciFiDrawable.ButtonShape.RECTANGULAR, new Color(0.2f, 0.6f, 0.9f, 1f));
    }
    
    /**
     * Создает стиль для sci-fi кнопок выбранной формы и цвета
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @param shape Форма кнопки
     * @param color Цвет кнопки
     * @return TextButtonStyle в sci-fi стиле
     */
    public static TextButtonStyle createSciFiButtonStyle(Skin skin, BitmapFont font, 
                                                        SciFiDrawable.ButtonShape shape, 
                                                        Color color) {
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        
        // Настраиваем цвета текста для sci-fi стиля
        style.fontColor = new Color(0.9f, 1f, 1f, 1f);        // Голографический светлый
        style.downFontColor = new Color(1f, 1f, 1f, 1f);      // Яркий белый при нажатии
        style.overFontColor = new Color(1f, 1f, 1f, 1f);      // Яркий белый при наведении
        style.disabledFontColor = new Color(0.5f, 0.7f, 0.9f, 0.5f);
        
        // Создаем drawables для разных состояний кнопки
        Color upColor = new Color(color);
        Color downColor = new Color(
            color.r * 0.7f,
            color.g * 0.7f,
            color.b * 0.7f,
            color.a
        );
        Color overColor = new Color(
            Math.min(color.r * 1.3f, 1.0f),
            Math.min(color.g * 1.3f, 1.0f),
            Math.min(color.b * 1.3f, 1.0f),
            color.a
        );
        
        // Создаем sci-fi drawable для разных состояний
        style.up = new SciFiDrawable(upColor, shape);
        style.down = new SciFiDrawable(downColor, shape);
        style.over = new SciFiDrawable(overColor, shape);
        
        return style;
    }
    
    /**
     * Создает стиль для шестиугольных sci-fi кнопок
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @return TextButtonStyle в sci-fi стиле с шестиугольной формой
     */
    public static TextButtonStyle createSciFiHexagonalButtonStyle(Skin skin, BitmapFont font) {
        return createSciFiButtonStyle(skin, font, SciFiDrawable.ButtonShape.HEXAGONAL, new Color(0.2f, 0.8f, 0.4f, 1f));
    }
    
    /**
     * Создает стиль для sci-fi кнопок со скосом слева
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @return TextButtonStyle в sci-fi стиле со скосом слева
     */
    public static TextButtonStyle createSciFiAngularLeftButtonStyle(Skin skin, BitmapFont font) {
        return createSciFiButtonStyle(skin, font, SciFiDrawable.ButtonShape.ANGULAR_LEFT, new Color(0.15f, 0.4f, 0.8f, 1f));
    }
    
    /**
     * Создает стиль для sci-fi кнопок со скосом справа
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @return TextButtonStyle в sci-fi стиле со скосом справа
     */
    public static TextButtonStyle createSciFiAngularRightButtonStyle(Skin skin, BitmapFont font) {
        return createSciFiButtonStyle(skin, font, SciFiDrawable.ButtonShape.ANGULAR_RIGHT, new Color(0.15f, 0.4f, 0.8f, 1f));
    }
    
    /**
     * Создает стиль для кнопок с закругленными углами в космическом стиле
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @return TextButtonStyle со скругленными углами в космическом стиле
     */
    public static TextButtonStyle createSpaceButtonStyle(Skin skin, BitmapFont font) {
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        
        // Яркие цвета для текста кнопок с космоса
        style.fontColor = new Color(0.95f, 0.98f, 1.0f, 1);         // Светло-голубой для звёздного неба
        style.downFontColor = new Color(0.7f, 0.9f, 1.0f, 1);      // Голубое свечение при нажатии
        style.overFontColor = new Color(1.0f, 1.0f, 1.0f, 1);      // Яркий белый при наведении
        style.disabledFontColor = new Color(0.5f, 0.5f, 0.7f, 0.7f);
        
        // Космические цвета для фонов кнопок
        Color upColor = new Color(0.05f, 0.1f, 0.3f, 0.9f);        // Глубокий космос
        Color downColor = new Color(0.02f, 0.05f, 0.2f, 0.95f);    // Тёмный космос
        Color overColor = new Color(0.1f, 0.2f, 0.4f, 0.9f);       // Освещенный космос
        
        // Создаем drawable с космическим стилем для разных состояний кнопки
        style.up = new RoundedDrawable(upColor, BUTTON_CORNER_RADIUS, false, 0.6f, true);
        style.down = new RoundedDrawable(downColor, BUTTON_CORNER_RADIUS, false, 0.4f, true);
        style.over = new RoundedDrawable(overColor, BUTTON_CORNER_RADIUS, false, 1.0f, true);
        
        return style;
    }
    
    /**
     * Создает стиль для кнопок с особым цветом космического стиля
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @param baseColor Базовый цвет туманности
     * @return TextButtonStyle со скругленными углами и космическим цветом
     */
    public static TextButtonStyle createColoredSpaceButtonStyle(Skin skin, BitmapFont font, Color baseColor) {
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        
        // Яркие цвета для текста кнопок
        style.fontColor = new Color(0.95f, 0.98f, 1.0f, 1);
        style.downFontColor = new Color(0.7f, 0.9f, 1.0f, 1);
        style.overFontColor = new Color(1.0f, 1.0f, 1.0f, 1);
        
        // Базовые тёмные цвета космоса
        Color upColor = new Color(0.05f, 0.1f, 0.3f, 0.9f);
        Color downColor = new Color(0.02f, 0.05f, 0.2f, 0.95f);
        Color overColor = new Color(0.1f, 0.2f, 0.4f, 0.9f);
        
        // Подмешиваем базовый цвет к цвету космоса
        upColor.r = upColor.r * 0.7f + baseColor.r * 0.3f;
        upColor.g = upColor.g * 0.7f + baseColor.g * 0.3f;
        upColor.b = upColor.b * 0.7f + baseColor.b * 0.3f;
        
        downColor.r = downColor.r * 0.8f + baseColor.r * 0.2f;
        downColor.g = downColor.g * 0.8f + baseColor.g * 0.2f;
        downColor.b = downColor.b * 0.8f + baseColor.b * 0.2f;
        
        overColor.r = overColor.r * 0.6f + baseColor.r * 0.4f;
        overColor.g = overColor.g * 0.6f + baseColor.g * 0.4f;
        overColor.b = overColor.b * 0.6f + baseColor.b * 0.4f;
        
        // Создаем drawable с космическим стилем и цветом туманности
        style.up = new RoundedDrawable(upColor, BUTTON_CORNER_RADIUS, false, 0.6f, true);
        style.down = new RoundedDrawable(downColor, BUTTON_CORNER_RADIUS, false, 0.4f, true);
        style.over = new RoundedDrawable(overColor, BUTTON_CORNER_RADIUS, false, 1.0f, true);
        
        return style;
    }
    
    /**
     * Создает стиль для кнопок с особым цветом
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @param baseColor Базовый цвет кнопки
     * @return TextButtonStyle со скругленными углами и указанным цветом
     */
    public static TextButtonStyle createColoredButtonStyle(Skin skin, BitmapFont font, Color baseColor) {
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        
        // Цвета для текста кнопок
        style.fontColor = new Color(1, 1, 1, 1);
        style.downFontColor = new Color(0.95f, 0.98f, 1, 1);
        style.overFontColor = new Color(0.9f, 0.97f, 1, 1);
        
        // Вычисляем цвета для других состояний на основе базового цвета
        Color upColor = baseColor;
        Color downColor = baseColor.cpy().mul(0.8f, 0.8f, 0.8f, 1f);  // Немного темнее
        Color overColor = baseColor.cpy().mul(1.15f, 1.15f, 1.15f, 1f); // Немного ярче
        
        // Создаем drawable со скругленными углами для разных состояний кнопки
        style.up = new RoundedDrawable(upColor, BUTTON_CORNER_RADIUS, false, 0.2f);
        style.down = new RoundedDrawable(downColor, BUTTON_CORNER_RADIUS, false, 0.1f);
        style.over = new RoundedDrawable(overColor, BUTTON_CORNER_RADIUS, false, 0.4f);
        
        return style;
    }
    
    /**
     * Обновляет существующий стиль кнопок, делая их скругленными
     * 
     * @param style Существующий стиль кнопок
     */
    public static void makeButtonStyleRounded(TextButtonStyle style) {
        // Плоские цвета для фонов кнопок без эффекта выпуклости
        Color upColor = new Color(0.3f, 0.55f, 0.95f, 0.85f);      // Яркий синий
        Color downColor = new Color(0.2f, 0.4f, 0.75f, 0.9f);      // Более темный синий
        Color overColor = new Color(0.4f, 0.6f, 0.98f, 0.85f);     // Светло-синий
        
        // Заменяем drawable на скругленные варианты без внутренней тени
        style.up = new RoundedDrawable(upColor, BUTTON_CORNER_RADIUS, false, 0.2f);
        style.down = new RoundedDrawable(downColor, BUTTON_CORNER_RADIUS, false, 0.1f);
        style.over = new RoundedDrawable(overColor, BUTTON_CORNER_RADIUS, false, 0.4f);
    }
    
    /**
     * Обновляет существующий стиль кнопок, применяя космический стиль
     * 
     * @param style Существующий стиль кнопок
     */
    public static void makeButtonStyleSpace(TextButtonStyle style) {
        // Космические цвета для фонов кнопок
        Color upColor = new Color(0.05f, 0.1f, 0.3f, 0.9f);        // Глубокий космос
        Color downColor = new Color(0.02f, 0.05f, 0.2f, 0.95f);    // Тёмный космос
        Color overColor = new Color(0.1f, 0.2f, 0.4f, 0.9f);       // Освещенный космос
        
        // Заменяем drawable на космические варианты
        style.up = new RoundedDrawable(upColor, BUTTON_CORNER_RADIUS, false, 0.6f, true);
        style.down = new RoundedDrawable(downColor, BUTTON_CORNER_RADIUS, false, 0.4f, true);
        style.over = new RoundedDrawable(overColor, BUTTON_CORNER_RADIUS, false, 1.0f, true);
        
        // Обновляем цвета текста для космического стиля
        style.fontColor = new Color(0.95f, 0.98f, 1.0f, 1);
        style.downFontColor = new Color(0.7f, 0.9f, 1.0f, 1);
        style.overFontColor = new Color(1.0f, 1.0f, 1.0f, 1);
    }
    
    /**
     * Обновляет существующий стиль кнопок, применяя sci-fi стиль
     * 
     * @param style Существующий стиль кнопок
     * @param shape Форма sci-fi кнопки
     * @param color Цвет sci-fi кнопки
     */
    public static void makeButtonStyleSciFi(TextButtonStyle style, SciFiDrawable.ButtonShape shape, Color color) {
        // Создаем sci-fi drawable для разных состояний
        Color upColor = new Color(color);
        Color downColor = new Color(
            color.r * 0.7f,
            color.g * 0.7f,
            color.b * 0.7f,
            color.a
        );
        Color overColor = new Color(
            Math.min(color.r * 1.3f, 1.0f),
            Math.min(color.g * 1.3f, 1.0f),
            Math.min(color.b * 1.3f, 1.0f),
            color.a
        );
        
        // Заменяем drawable на sci-fi варианты
        style.up = new SciFiDrawable(upColor, shape);
        style.down = new SciFiDrawable(downColor, shape);
        style.over = new SciFiDrawable(overColor, shape);
        
        // Обновляем цвета текста для sci-fi стиля
        style.fontColor = new Color(0.9f, 1f, 1f, 1f);         // Голографический светлый
        style.downFontColor = new Color(1f, 1f, 1f, 1f);       // Яркий белый при нажатии
        style.overFontColor = new Color(1f, 1f, 1f, 1f);       // Яркий белый при наведении
    }
    
    /**
     * Создает стиль для кнопок в sci-fi стиле с прямоугольной формой и голубым цветом
     * (как на скриншоте главного меню)
     * 
     * @param skin Skin, в который добавится стиль
     * @param font Шрифт для текста кнопок
     * @return TextButtonStyle в sci-fi стиле с прямоугольной формой и голубым цветом
     */
    public static TextButtonStyle createSciFiBlueButtonStyle(Skin skin, BitmapFont font) {
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        
        // Настраиваем цвета текста для sci-fi стиля
        style.fontColor = new Color(0.9f, 1f, 1f, 1f);        // Голографический светлый
        style.downFontColor = new Color(1f, 1f, 1f, 1f);      // Яркий белый при нажатии
        style.overFontColor = new Color(1f, 1f, 1f, 1f);      // Яркий белый при наведении
        style.disabledFontColor = new Color(0.5f, 0.7f, 0.9f, 0.5f);
        
        // Более насыщенный темно-голубой цвет, как на скриншоте
        Color upColor = new Color(0.1f, 0.4f, 0.7f, 1f);
        Color downColor = new Color(
            upColor.r * 0.7f,
            upColor.g * 0.7f,
            upColor.b * 0.7f,
            upColor.a
        );
        Color overColor = new Color(
            Math.min(upColor.r * 1.2f, 1.0f),
            Math.min(upColor.g * 1.2f, 1.0f),
            Math.min(upColor.b * 1.2f, 1.0f),
            upColor.a
        );
        
        // Создаем sci-fi drawable для разных состояний
        style.up = new SciFiDrawable(upColor, SciFiDrawable.ButtonShape.RECTANGULAR);
        style.down = new SciFiDrawable(downColor, SciFiDrawable.ButtonShape.RECTANGULAR);
        style.over = new SciFiDrawable(overColor, SciFiDrawable.ButtonShape.RECTANGULAR);
        
        return style;
    }
} 