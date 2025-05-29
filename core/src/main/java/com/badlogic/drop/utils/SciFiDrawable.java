package com.badlogic.drop.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

/**
 * Drawable для создания кнопок и UI элементов в sci-fi стиле с металлическим контуром и свечением.
 */
public class SciFiDrawable extends BaseDrawable {
    private final Texture texture;
    private final Color baseColor;
    private final int borderThickness;
    private final ButtonShape shape;
    
    /**
     * Форма кнопок
     */
    public enum ButtonShape {
        RECTANGULAR,    // Прямоугольная
        HEXAGONAL,      // Шестиугольная
        ANGULAR_LEFT,   // Со скосом слева
        ANGULAR_RIGHT   // Со скосом справа
    }
    
    /**
     * Создает новый SciFiDrawable с указанным цветом и прямоугольной формой
     * 
     * @param color Цвет кнопки
     */
    public SciFiDrawable(Color color) {
        this(color, ButtonShape.RECTANGULAR);
    }
    
    /**
     * Создает новый SciFiDrawable с указанным цветом и формой
     * 
     * @param color Цвет кнопки
     * @param shape Форма кнопки
     */
    public SciFiDrawable(Color color, ButtonShape shape) {
        this(color, shape, 3);
    }
    
    /**
     * Создает новый SciFiDrawable с указанным цветом, формой и толщиной контура
     * 
     * @param color Цвет кнопки
     * @param shape Форма кнопки
     * @param borderThickness Толщина металлического контура
     */
    public SciFiDrawable(Color color, ButtonShape shape, int borderThickness) {
        this.baseColor = new Color(color);
        this.shape = shape;
        this.borderThickness = borderThickness;
        
        // Создаем Pixmap с sci-fi кнопкой
        Pixmap pixmap = createSciFiButton(200, 80);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Создает Pixmap с sci-fi кнопкой
     */
    private Pixmap createSciFiButton(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        
        // Очищаем pixmap (полная прозрачность)
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        
        // Сначала отрисовываем внешнее свечение
        drawOuterGlow(pixmap, width, height);
        
        // Затем рисуем металлический контур
        drawMetalBorder(pixmap, width, height);
        
        // И наконец, рисуем внутреннюю часть с голографическим эффектом
        drawInnerHologram(pixmap, width, height);
        
        return pixmap;
    }
    
    /**
     * Рисует внешнее свечение
     */
    private void drawOuterGlow(Pixmap pixmap, int width, int height) {
        // Создаем яркий цвет свечения на основе базового цвета
        Color glowColor = new Color(baseColor);
        glowColor.r = Math.min(glowColor.r * 1.5f, 1.0f);
        glowColor.g = Math.min(glowColor.g * 1.5f, 1.0f);
        glowColor.b = Math.min(glowColor.b * 1.5f, 1.0f);
        
        int glowSize = 6; // Размер свечения
        
        for (int i = glowSize; i > 0; i--) {
            // Уменьшаем прозрачность к внешней части свечения
            Color currentGlowColor = new Color(glowColor);
            currentGlowColor.a = 0.1f * (float)i / glowSize;
            pixmap.setColor(currentGlowColor);
            
            // Рисуем контур с учетом формы
            drawButtonShape(pixmap, width, height, borderThickness + i, false);
        }
    }
    
    /**
     * Рисует металлический контур
     */
    private void drawMetalBorder(Pixmap pixmap, int width, int height) {
        // Создаем металлический эффект с градиентом
        Color topBorderColor = new Color(0.9f, 0.9f, 0.95f, 1.0f); // Светлый металлик сверху
        Color bottomBorderColor = new Color(0.6f, 0.6f, 0.65f, 1.0f); // Темный металлик снизу
        Color sideBorderColor = new Color(0.75f, 0.75f, 0.8f, 1.0f); // Средний металлик по бокам
        
        // Рисуем внешнюю часть контура
        pixmap.setColor(sideBorderColor);
        drawButtonShape(pixmap, width, height, borderThickness, false);
        
        // Рисуем верхнюю часть контура (светлая)
        for (int y = 0; y < height / 2; y++) {
            // Плавно переходим от верхнего цвета к среднему
            float t = (float)y / (height / 2);
            Color currentColor = new Color(
                topBorderColor.r * (1 - t) + sideBorderColor.r * t,
                topBorderColor.g * (1 - t) + sideBorderColor.g * t,
                topBorderColor.b * (1 - t) + sideBorderColor.b * t,
                1.0f
            );
            pixmap.setColor(currentColor);
            
            // Рисуем только линию контура
            drawBorderLine(pixmap, width, height, y, borderThickness);
        }
        
        // Рисуем нижнюю часть контура (темная)
        for (int y = height / 2; y < height; y++) {
            // Плавно переходим от среднего цвета к нижнему
            float t = (float)(y - height / 2) / (height / 2);
            Color currentColor = new Color(
                sideBorderColor.r * (1 - t) + bottomBorderColor.r * t,
                sideBorderColor.g * (1 - t) + bottomBorderColor.g * t,
                sideBorderColor.b * (1 - t) + bottomBorderColor.b * t,
                1.0f
            );
            pixmap.setColor(currentColor);
            
            // Рисуем только линию контура
            drawBorderLine(pixmap, width, height, y, borderThickness);
        }
        
        // Добавляем цветные полосы по контуру
        Color highlightColor = new Color(baseColor);
        highlightColor.a = 0.8f;
        pixmap.setColor(highlightColor);
        
        // Верхняя и нижняя цветные полосы
        for (int i = 0; i < 2; i++) {
            drawAccentLine(pixmap, width, height, i, borderThickness);
            drawAccentLine(pixmap, width, height, height - i - 1, borderThickness);
        }
    }
    
    /**
     * Рисует внутреннюю часть кнопки с голографическим эффектом
     */
    private void drawInnerHologram(Pixmap pixmap, int width, int height) {
        // Внутренний отступ для внутренней области
        int innerPadding = borderThickness;
        
        // Создаем прозрачный цвет на основе базового для голографического эффекта
        Color hologramBaseColor = new Color(baseColor);
        hologramBaseColor.a = 0.7f; // Полупрозрачность
        
        // Создаем градиент для голографического эффекта
        Color topHoloColor = new Color(
            Math.min(hologramBaseColor.r * 1.3f, 1.0f),
            Math.min(hologramBaseColor.g * 1.3f, 1.0f),
            Math.min(hologramBaseColor.b * 1.3f, 1.0f),
            hologramBaseColor.a * 0.9f
        );
        
        Color bottomHoloColor = new Color(
            hologramBaseColor.r * 0.7f,
            hologramBaseColor.g * 0.7f,
            hologramBaseColor.b * 0.7f,
            hologramBaseColor.a * 0.7f
        );
        
        // Заливаем внутреннюю часть с градиентом
        for (int y = innerPadding; y < height - innerPadding; y++) {
            float t = (float)(y - innerPadding) / (height - innerPadding * 2);
            Color currentColor = new Color(
                topHoloColor.r * (1 - t) + bottomHoloColor.r * t,
                topHoloColor.g * (1 - t) + bottomHoloColor.g * t,
                topHoloColor.b * (1 - t) + bottomHoloColor.b * t,
                topHoloColor.a * (1 - t) + bottomHoloColor.a * t
            );
            
            pixmap.setColor(currentColor);
            drawFillLine(pixmap, width, height, y, innerPadding);
        }
        
        // Добавляем сетку (паттерн) внутри голограммы
        drawGrid(pixmap, width, height, innerPadding);
    }
    
    /**
     * Рисует сетку внутри голограммы
     */
    private void drawGrid(Pixmap pixmap, int width, int height, int padding) {
        Color gridColor = new Color(1, 1, 1, 0.1f);
        pixmap.setColor(gridColor);
        
        // Горизонтальные линии сетки
        int gridSpacingY = 6; // Расстояние между линиями по Y
        for (int y = padding; y < height - padding; y += gridSpacingY) {
            for (int x = padding; x < width - padding; x += 2) { // Пунктирная линия
                if (isInsideShape(x, y, width, height, padding))
                    pixmap.drawPixel(x, y);
            }
        }
        
        // Вертикальные линии сетки
        int gridSpacingX = 10; // Расстояние между линиями по X
        for (int x = padding; x < width - padding; x += gridSpacingX) {
            for (int y = padding; y < height - padding; y += 2) { // Пунктирная линия
                if (isInsideShape(x, y, width, height, padding))
                    pixmap.drawPixel(x, y);
            }
        }
    }
    
    /**
     * Проверяет, находится ли точка внутри формы кнопки
     */
    private boolean isInsideShape(int x, int y, int width, int height, int padding) {
        switch (shape) {
            case RECTANGULAR:
                return x >= padding && x < width - padding && y >= padding && y < height - padding;
                
            case HEXAGONAL:
                // Для шестиугольника проверяем скосы по краям
                float centerX = width / 2f;
                float ratio = 0.3f; // Насколько сильно скошены края (0.3 = 30% от ширины)
                int cornerWidth = Math.round(width * ratio);
                
                if (x < padding + cornerWidth) {
                    // Левый скос
                    float progress = (float)(x - padding) / cornerWidth;
                    int minY = padding + (int)((1 - progress) * (height / 4));
                    int maxY = height - padding - (int)((1 - progress) * (height / 4));
                    return y >= minY && y < maxY;
                } 
                else if (x >= width - padding - cornerWidth) {
                    // Правый скос
                    float progress = (float)(width - padding - x) / cornerWidth;
                    int minY = padding + (int)((1 - progress) * (height / 4));
                    int maxY = height - padding - (int)((1 - progress) * (height / 4));
                    return y >= minY && y < maxY;
                }
                // Центральная часть
                return y >= padding && y < height - padding;
                
            case ANGULAR_LEFT:
                // Скос слева
                if (x < padding + height / 3) {
                    float progress = (float)(x - padding) / (height / 3);
                    int minY = padding + (int)((1 - progress) * (height / 3));
                    return y >= minY && y < height - padding;
                }
                return x >= padding && x < width - padding && y >= padding && y < height - padding;
                
            case ANGULAR_RIGHT:
                // Скос справа
                if (x >= width - padding - height / 3) {
                    float progress = (float)(width - padding - x) / (height / 3);
                    int minY = padding + (int)((1 - progress) * (height / 3));
                    return y >= padding && y < height - minY;
                }
                return x >= padding && x < width - padding && y >= padding && y < height - padding;
                
            default:
                return true;
        }
    }
    
    /**
     * Рисует линию контура
     */
    private void drawBorderLine(Pixmap pixmap, int width, int height, int y, int thickness) {
        switch (shape) {
            case RECTANGULAR:
                // Для прямоугольника просто рисуем боковые линии
                if (y >= 0 && y < height) {
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(i, y); // Левый край
                        pixmap.drawPixel(width - i - 1, y); // Правый край
                    }
                }
                break;
                
            case HEXAGONAL:
                // Для шестиугольника учитываем скосы
                float ratio = 0.3f; // Насколько сильно скошены края (0.3 = 30% от ширины)
                int cornerWidth = Math.round(width * ratio);
                
                if (y < height / 4) {
                    // Верхняя часть с левым скосом
                    float progress = (float)y / (height / 4);
                    int leftX = (int)((1 - progress) * cornerWidth);
                    
                    // Левая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(leftX + i, y);
                    }
                    
                    // Правая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(width - leftX - i - 1, y);
                    }
                } 
                else if (y >= height - height / 4) {
                    // Нижняя часть с левым скосом
                    float progress = (float)(height - y - 1) / (height / 4);
                    int leftX = (int)((1 - progress) * cornerWidth);
                    
                    // Левая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(leftX + i, y);
                    }
                    
                    // Правая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(width - leftX - i - 1, y);
                    }
                } 
                else {
                    // Середина
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(i, y); // Левый край
                        pixmap.drawPixel(width - i - 1, y); // Правый край
                    }
                }
                break;
                
            case ANGULAR_LEFT:
                // Скос слева
                if (y < height / 3) {
                    // Верхняя часть с левым скосом
                    float progress = (float)y / (height / 3);
                    int leftX = (int)((1 - progress) * (height / 3));
                    
                    // Левая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(leftX + i, y);
                    }
                    
                    // Правая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(width - i - 1, y);
                    }
                } 
                else {
                    // Середина и низ
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(i, y); // Левый край
                        pixmap.drawPixel(width - i - 1, y); // Правый край
                    }
                }
                break;
                
            case ANGULAR_RIGHT:
                // Скос справа
                if (y < height / 3) {
                    // Верхняя часть с правым скосом
                    float progress = (float)y / (height / 3);
                    int rightX = (int)((1 - progress) * (height / 3));
                    
                    // Левая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(i, y);
                    }
                    
                    // Правая сторона
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(width - rightX - i - 1, y);
                    }
                } 
                else {
                    // Середина и низ
                    for (int i = 0; i < thickness; i++) {
                        pixmap.drawPixel(i, y); // Левый край
                        pixmap.drawPixel(width - i - 1, y); // Правый край
                    }
                }
                break;
        }
    }
    
    /**
     * Рисует линию заполнения
     */
    private void drawFillLine(Pixmap pixmap, int width, int height, int y, int padding) {
        switch (shape) {
            case RECTANGULAR:
                // Для прямоугольника просто рисуем горизонтальную линию
                pixmap.drawLine(padding, y, width - padding - 1, y);
                break;
                
            case HEXAGONAL:
                // Для шестиугольника учитываем скосы
                float ratio = 0.3f;
                int cornerWidth = Math.round(width * ratio);
                
                if (y < height / 4 + padding) {
                    // Верхняя часть с левым скосом
                    float progress = (float)(y - padding) / (height / 4);
                    int leftX = (int)((1 - progress) * cornerWidth) + padding;
                    int rightX = width - leftX - 1;
                    pixmap.drawLine(leftX, y, rightX, y);
                } 
                else if (y >= height - height / 4 - padding) {
                    // Нижняя часть с левым скосом
                    float progress = (float)(height - y - 1 - padding) / (height / 4);
                    int leftX = (int)((1 - progress) * cornerWidth) + padding;
                    int rightX = width - leftX - 1;
                    pixmap.drawLine(leftX, y, rightX, y);
                } 
                else {
                    // Середина
                    pixmap.drawLine(padding, y, width - padding - 1, y);
                }
                break;
                
            case ANGULAR_LEFT:
                // Скос слева
                if (y < height / 3 + padding) {
                    // Верхняя часть с левым скосом
                    float progress = (float)(y - padding) / (height / 3);
                    int leftX = (int)((1 - progress) * (height / 3)) + padding;
                    pixmap.drawLine(leftX, y, width - padding - 1, y);
                } 
                else {
                    // Середина и низ
                    pixmap.drawLine(padding, y, width - padding - 1, y);
                }
                break;
                
            case ANGULAR_RIGHT:
                // Скос справа
                if (y < height / 3 + padding) {
                    // Верхняя часть с правым скосом
                    float progress = (float)(y - padding) / (height / 3);
                    int rightX = width - (int)((1 - progress) * (height / 3)) - padding - 1;
                    pixmap.drawLine(padding, y, rightX, y);
                } 
                else {
                    // Середина и низ
                    pixmap.drawLine(padding, y, width - padding - 1, y);
                }
                break;
        }
    }
    
    /**
     * Рисует форму кнопки
     */
    private void drawButtonShape(Pixmap pixmap, int width, int height, int thickness, boolean fill) {
        // Если заливка, то нарисуем заполненную форму
        if (fill) {
            for (int y = 0; y < height; y++) {
                drawFillLine(pixmap, width, height, y, 0);
            }
            return;
        }
        
        // Иначе рисуем только контур
        for (int y = 0; y < height; y++) {
            drawBorderLine(pixmap, width, height, y, thickness);
        }
    }
    
    /**
     * Рисует акцентную линию заданного цвета
     */
    private void drawAccentLine(Pixmap pixmap, int width, int height, int y, int thickness) {
        if (y < 0 || y >= height) return;
        
        switch (shape) {
            case RECTANGULAR:
                pixmap.drawLine(thickness, y, width - thickness - 1, y);
                break;
                
            case HEXAGONAL:
                float ratio = 0.3f;
                int cornerWidth = Math.round(width * ratio);
                
                if (y < height / 4) {
                    // Верхняя часть
                    float progress = (float)y / (height / 4);
                    int leftX = (int)((1 - progress) * cornerWidth) + thickness;
                    int rightX = width - leftX - 1;
                    pixmap.drawLine(leftX, y, rightX, y);
                } 
                else if (y >= height - height / 4) {
                    // Нижняя часть
                    float progress = (float)(height - y - 1) / (height / 4);
                    int leftX = (int)((1 - progress) * cornerWidth) + thickness;
                    int rightX = width - leftX - 1;
                    pixmap.drawLine(leftX, y, rightX, y);
                } 
                else {
                    // Середина
                    pixmap.drawLine(thickness, y, width - thickness - 1, y);
                }
                break;
                
            case ANGULAR_LEFT:
                if (y < height / 3) {
                    // Верхняя часть
                    float progress = (float)y / (height / 3);
                    int leftX = (int)((1 - progress) * (height / 3)) + thickness;
                    pixmap.drawLine(leftX, y, width - thickness - 1, y);
                } 
                else {
                    pixmap.drawLine(thickness, y, width - thickness - 1, y);
                }
                break;
                
            case ANGULAR_RIGHT:
                if (y < height / 3) {
                    // Верхняя часть
                    float progress = (float)y / (height / 3);
                    int rightX = width - (int)((1 - progress) * (height / 3)) - thickness - 1;
                    pixmap.drawLine(thickness, y, rightX, y);
                } 
                else {
                    pixmap.drawLine(thickness, y, width - thickness - 1, y);
                }
                break;
        }
    }
    
    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        Color oldColor = batch.getColor();
        batch.setColor(Color.WHITE); // Рисуем текстуру как есть, без изменения цвета
        batch.draw(texture, x, y, width, height);
        batch.setColor(oldColor);
    }
    
    /**
     * Освобождает ресурсы
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
} 