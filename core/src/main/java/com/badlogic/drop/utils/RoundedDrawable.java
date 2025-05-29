package com.badlogic.drop.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.math.MathUtils;

/**
 * Drawable для создания кнопок и других UI элементов с закругленными углами в космическом стиле.
 */
public class RoundedDrawable extends BaseDrawable {
    private final Texture texture;
    private final Color color;
    private final int cornerRadius;
    private final boolean withInnerShadow;
    private final float glowStrength;
    private final boolean spaceStyle;
    
    /**
     * Создает новый RoundedDrawable с указанным цветом и радиусом закругления
     * 
     * @param color Цвет drawable
     * @param cornerRadius Радиус скругления углов (в пикселях)
     */
    public RoundedDrawable(Color color, int cornerRadius) {
        this(color, cornerRadius, true);
    }
    
    /**
     * Создает новый RoundedDrawable с указанным цветом, радиусом закругления и настройкой внутренней тени
     * 
     * @param color Цвет drawable
     * @param cornerRadius Радиус скругления углов (в пикселях)
     * @param withInnerShadow Включить эффект внутренней тени
     */
    public RoundedDrawable(Color color, int cornerRadius, boolean withInnerShadow) {
        this(color, cornerRadius, withInnerShadow, 0f);
    }
    
    /**
     * Создает новый RoundedDrawable с указанным цветом, радиусом закругления, настройкой внутренней тени и силой свечения
     * 
     * @param color Цвет drawable
     * @param cornerRadius Радиус скругления углов (в пикселях)
     * @param withInnerShadow Включить эффект внутренней тени
     * @param glowStrength Сила внешнего свечения (0 - без свечения)
     */
    public RoundedDrawable(Color color, int cornerRadius, boolean withInnerShadow, float glowStrength) {
        this(color, cornerRadius, withInnerShadow, glowStrength, false);
    }
    
    /**
     * Создает новый RoundedDrawable с указанным цветом, радиусом закругления, настройкой внутренней тени,
     * силой свечения и стилем "безграничный космос"
     * 
     * @param color Цвет drawable
     * @param cornerRadius Радиус скругления углов (в пикселях)
     * @param withInnerShadow Включить эффект внутренней тени
     * @param glowStrength Сила внешнего свечения (0 - без свечения)
     * @param spaceStyle Использовать стиль "безграничный космос"
     */
    public RoundedDrawable(Color color, int cornerRadius, boolean withInnerShadow, float glowStrength, boolean spaceStyle) {
        this.color = new Color(color);
        this.cornerRadius = cornerRadius;
        this.withInnerShadow = withInnerShadow;
        this.glowStrength = glowStrength;
        this.spaceStyle = spaceStyle;
        
        // Создаем Pixmap с закругленными углами и космическими эффектами
        Pixmap pixmap;
        if (spaceStyle) {
            pixmap = createSpaceRectangle(128, 64, cornerRadius, color, glowStrength);
        } else {
            pixmap = createRoundedRectangle(128, 64, cornerRadius, color, withInnerShadow, glowStrength);
        }
        texture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Создает новый Pixmap с закругленными углами в стиле космического пространства
     */
    private Pixmap createSpaceRectangle(int width, int height, int cornerRadius, Color baseColor, float glowStrength) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        
        // Очищаем pixmap
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        
        // Сначала рисуем космическое свечение за границами кнопки
        addCosmicGlow(pixmap, width, height, cornerRadius, baseColor, glowStrength);
        
        // Рисуем основную форму с космическим фоном
        createCosmicBackground(pixmap, width, height, cornerRadius, baseColor);
        
        // Добавляем звёзды разных размеров
        addStars(pixmap, width, height, cornerRadius);
        
        // Добавляем туманности
        addNebula(pixmap, width, height, cornerRadius, baseColor);
        
        return pixmap;
    }
    
    /**
     * Создает космический фон с градиентом для кнопки
     */
    private void createCosmicBackground(Pixmap pixmap, int width, int height, int cornerRadius, Color baseColor) {
        // Используем глубокий космический градиент
        
        // Глубокий космический цвет для фона
        Color deepSpaceColor = new Color(0.03f, 0.05f, 0.15f, 0.95f);
        // Добавляем оттенок главного цвета в фон
        deepSpaceColor.r = deepSpaceColor.r * 0.7f + baseColor.r * 0.3f;
        deepSpaceColor.g = deepSpaceColor.g * 0.7f + baseColor.g * 0.3f;
        deepSpaceColor.b = deepSpaceColor.b * 0.7f + baseColor.b * 0.3f;
        
        // Слегка голубоватый цвет для верхней части
        Color topSpaceColor = new Color(0.05f, 0.1f, 0.25f, 0.95f);
        // Подмешиваем базовый цвет
        topSpaceColor.r = topSpaceColor.r * 0.6f + baseColor.r * 0.4f;
        topSpaceColor.g = topSpaceColor.g * 0.6f + baseColor.g * 0.4f;
        topSpaceColor.b = topSpaceColor.b * 0.6f + baseColor.b * 0.4f;
        
        // Применяем вертикальный градиент для космического эффекта
        float step = 1.0f / height;
        for (int y = 0; y < height; y++) {
            float t = y * step; // 0.0 (верх) до 1.0 (низ)
            
            // Создаем нелинейный переход для более естественного космического градиента
            t = smoothStep(t);
            
            Color currentColor = new Color();
            currentColor.r = topSpaceColor.r * (1 - t) + deepSpaceColor.r * t;
            currentColor.g = topSpaceColor.g * (1 - t) + deepSpaceColor.g * t;
            currentColor.b = topSpaceColor.b * (1 - t) + deepSpaceColor.b * t;
            currentColor.a = 0.95f;
            
            pixmap.setColor(currentColor);
            
            // Рисуем горизонтальные линии с учетом закругленных углов
            if (y < cornerRadius) {
                // Верхняя часть с закругленными углами
                int xOffset = calculateSmoothCorner(y, cornerRadius);
                pixmap.drawLine(xOffset, y, width - xOffset, y);
            } else if (y >= height - cornerRadius) {
                // Нижняя часть с закругленными углами
                int xOffset = calculateSmoothCorner(height - y - 1, cornerRadius);
                pixmap.drawLine(xOffset, y, width - xOffset, y);
            } else {
                // Средняя часть (прямоугольная)
                pixmap.drawLine(0, y, width, y);
            }
        }
    }
    
    /**
     * Добавляет звезды разных размеров и яркости
     */
    private void addStars(Pixmap pixmap, int width, int height, int cornerRadius) {
        // Генерируем случайные звезды
        int starCount = (width * height) / 100; // Количество звезд зависит от размера кнопки
        
        for (int i = 0; i < starCount; i++) {
            // Координаты звезды
            int x = MathUtils.random(width);
            int y = MathUtils.random(height);
            
            // Проверяем, что звезда находится внутри скругленных углов
            if (isInsideRoundedRectangle(x, y, width, height, cornerRadius)) {
                // Размер и яркость звезды
                int starSize = MathUtils.random(1, 2);
                float brightness = MathUtils.random(0.7f, 1.0f);
                
                // Устанавливаем цвет звезды
                if (MathUtils.randomBoolean(0.2f)) {
                    // Редкие голубоватые звезды
                    pixmap.setColor(new Color(0.8f * brightness, 0.9f * brightness, 1.0f * brightness, 0.9f));
                } else if (MathUtils.randomBoolean(0.15f)) {
                    // Редкие красноватые звезды
                    pixmap.setColor(new Color(1.0f * brightness, 0.8f * brightness, 0.8f * brightness, 0.9f));
                } else {
                    // Обычные белые звезды
                    pixmap.setColor(new Color(brightness, brightness, brightness, 0.9f));
                }
                
                // Рисуем звезду
                if (starSize == 1) {
                    pixmap.drawPixel(x, y);
                } else {
                    // Звезда побольше
                    pixmap.drawPixel(x, y);
                    if (x > 0) pixmap.drawPixel(x - 1, y);
                    if (x < width - 1) pixmap.drawPixel(x + 1, y);
                    if (y > 0) pixmap.drawPixel(x, y - 1);
                    if (y < height - 1) pixmap.drawPixel(x, y + 1);
                }
                
                // Для очень ярких звезд добавляем свечение
                if (brightness > 0.95f && MathUtils.randomBoolean(0.4f)) {
                    Color glowColor = new Color(brightness, brightness, brightness, 0.4f);
                    pixmap.setColor(glowColor);
                    
                    // Добавляем свечение вокруг яркой звезды
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dy = -2; dy <= 2; dy++) {
                            if (dx == 0 && dy == 0) continue; // Центр уже нарисован
                            if (Math.abs(dx) + Math.abs(dy) > 3) continue; // Ограничиваем свечение

                            int gx = x + dx;
                            int gy = y + dy;
                            
                            if (gx >= 0 && gx < width && gy >= 0 && gy < height &&
                                isInsideRoundedRectangle(gx, gy, width, height, cornerRadius)) {
                                pixmap.drawPixel(gx, gy);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Проверяет, находится ли точка внутри скругленного прямоугольника
     */
    private boolean isInsideRoundedRectangle(int x, int y, int width, int height, int radius) {
        // Проверка для углов
        if (x < radius && y < radius) {
            // Верхний левый угол
            return ((x - radius) * (x - radius) + (y - radius) * (y - radius)) <= radius * radius;
        }
        else if (x > width - radius && y < radius) {
            // Верхний правый угол
            return ((x - (width - radius)) * (x - (width - radius)) + (y - radius) * (y - radius)) <= radius * radius;
        }
        else if (x < radius && y > height - radius) {
            // Нижний левый угол
            return ((x - radius) * (x - radius) + (y - (height - radius)) * (y - (height - radius))) <= radius * radius;
        }
        else if (x > width - radius && y > height - radius) {
            // Нижний правый угол
            return ((x - (width - radius)) * (x - (width - radius)) + (y - (height - radius)) * (y - (height - radius))) <= radius * radius;
        }
        
        // Для центральной части и прямых сторон
        return (x >= 0 && x < width && y >= 0 && y < height);
    }
    
    /**
     * Добавляет эффект космического свечения
     */
    private void addCosmicGlow(Pixmap pixmap, int width, int height, int cornerRadius, Color baseColor, float strength) {
        // Усиливаем яркость базового цвета для свечения
        Color cosmicGlowColor = new Color(baseColor);
        // Делаем более насыщенное свечение
        cosmicGlowColor.r = Math.min(cosmicGlowColor.r * 1.4f, 1.0f);
        cosmicGlowColor.g = Math.min(cosmicGlowColor.g * 1.4f, 1.0f);
        cosmicGlowColor.b = Math.min(cosmicGlowColor.b * 1.4f, 1.0f);
        
        // Усиливаем свечение для космического стиля
        float cosmicStrength = strength * 1.5f;
        int glowSize = Math.round(4 * cosmicStrength);
        
        // Создаем свечение с бо́льшим радиусом для космического эффекта
        for (int i = 1; i <= glowSize; i++) {
            // Экспоненциальное затухание для более естественного свечения
            float alpha = 0.6f * cosmicStrength * (float)Math.pow(0.8, i);
            Color currentGlowColor = new Color(cosmicGlowColor);
            currentGlowColor.a = alpha;
            
            pixmap.setColor(currentGlowColor);
            
            // Рисуем внешнее свечение с плавным переходом
            drawSoftOutline(pixmap, -i, -i, width + i * 2, height + i * 2, cornerRadius + i);
        }
    }
    
    /**
     * Добавляет эффект космических туманностей
     */
    private void addNebula(Pixmap pixmap, int width, int height, int cornerRadius, Color baseColor) {
        // Выбираем 1-3 положения для туманностей
        int nebulaCount = MathUtils.random(1, 2);
        
        for (int n = 0; n < nebulaCount; n++) {
            // Центр туманности
            int centerX = MathUtils.random(width * 1/4, width * 3/4);
            int centerY = MathUtils.random(height * 1/4, height * 3/4);
            
            // Размер туманности
            int nebulaRadius = MathUtils.random(width / 6, width / 4);
            
            // Цвет туманности на основе базового цвета, но более разбавленный
            Color nebulaColor = new Color(baseColor);
            nebulaColor.r = Math.min(nebulaColor.r * 1.2f, 1.0f);
            nebulaColor.g = Math.min(nebulaColor.g * 1.2f, 1.0f);
            nebulaColor.b = Math.min(nebulaColor.b * 1.2f, 1.0f);
            
            // Рисуем туманность с постепенным уменьшением прозрачности
            for (int i = 0; i < nebulaRadius; i++) {
                float alpha = 0.03f * (1.0f - (float)i / nebulaRadius);
                Color currentColor = new Color(nebulaColor);
                currentColor.a = alpha;
                
                pixmap.setColor(currentColor);
                
                // Рисуем круги разных радиусов для создания туманности
                for (int y = -i; y <= i; y++) {
                    for (int x = -i; x <= i; x++) {
                        if (x*x + y*y <= i*i) { // Точка внутри круга
                            int px = centerX + x;
                            int py = centerY + y;
                            
                            // Убедимся, что точка внутри прямоугольника и скругленных углов
                            if (px >= 0 && px < width && py >= 0 && py < height && 
                                isInsideRoundedRectangle(px, py, width, height, cornerRadius)) {
                                pixmap.drawPixel(px, py);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Создает новый Pixmap с закругленными углами
     */
    private Pixmap createRoundedRectangle(int width, int height, int cornerRadius, Color color, 
                                         boolean withInnerShadow, float glowStrength) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        
        // Очищаем pixmap (полная прозрачность)
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        
        // Добавляем эффект внешнего свечения если включен, но делаем его более мягким
        if (glowStrength > 0) {
            addGlowEffect(pixmap, width, height, cornerRadius, color, glowStrength);
        }
        
        // Создаем градиентные цвета с более мягким переходом
        Color topColor = color.cpy().add(0.08f, 0.08f, 0.08f, 0f);
        Color bottomColor = color.cpy().sub(0.08f, 0.08f, 0.08f, 0f);
        
        // Применяем мягкий градиент без резких переходов
        float step = 1.0f / height;
        for (int y = 0; y < height; y++) {
            float t = y * step; // 0.0 (верх) до 1.0 (низ)
            
            // Смягченный переход для устранения резких линий
            t = smoothStep(t);
            
            Color currentColor = new Color();
            currentColor.r = topColor.r * (1 - t) + bottomColor.r * t;
            currentColor.g = topColor.g * (1 - t) + bottomColor.g * t;
            currentColor.b = topColor.b * (1 - t) + bottomColor.b * t;
            
            // Минимальный эффект объема без выступов
            float distFromCenter = Math.abs(t - 0.5f) * 2; // 0 в центре, 1 на краях
            // Очень слабое осветление в центре
            float lightFactor = 0.02f; // Уменьшили с 0.05f
            currentColor.r += (1 - distFromCenter) * lightFactor;
            currentColor.g += (1 - distFromCenter) * lightFactor;
            currentColor.b += (1 - distFromCenter) * lightFactor;
            
            currentColor.a = color.a;
            pixmap.setColor(currentColor);
            
            // Рисуем горизонтальные линии с улучшенным алгоритмом
            if (y < cornerRadius) {
                // Верхняя часть с закругленными углами
                int xOffset = calculateSmoothCorner(y, cornerRadius);
                pixmap.drawLine(xOffset, y, width - xOffset, y);
            } else if (y >= height - cornerRadius) {
                // Нижняя часть с закругленными углами
                int xOffset = calculateSmoothCorner(height - y - 1, cornerRadius);
                pixmap.drawLine(xOffset, y, width - xOffset, y);
            } else {
                // Средняя часть (прямоугольная)
                pixmap.drawLine(0, y, width, y);
            }
        }
        
        // Добавляем эффект внутренней тени если включено, но делаем его более мягким
        if (withInnerShadow) {
            addSoftInnerShadow(pixmap, width, height, cornerRadius, color);
        }
        
        return pixmap;
    }
    
    /**
     * Рассчитывает более плавный край для скругления
     */
    private int calculateSmoothCorner(int y, int radius) {
        // Используем полуэллиптическую формулу вместо круговой для более сглаженных углов
        // Квадратный корень смягчаем для более гладкого эффекта
        float relY = (float)y / radius;
        float factor = 1.0f - relY;
        float smooth = (float)Math.pow(factor, 1.8); // Более плавная степенная функция
        return radius - (int)(radius * Math.sqrt(smooth));
    }
    
    /**
     * Функция плавного перехода для устранения резких границ
     */
    private float smoothStep(float t) {
        // Полиномиальное сглаживание вместо линейного перехода
        return t * t * (3 - 2 * t);
    }
    
    /**
     * Добавляет эффект внешнего свечения
     */
    private void addGlowEffect(Pixmap pixmap, int width, int height, int cornerRadius, Color color, float strength) {
        // Создаем цвет свечения на основе базового цвета
        Color glowColor = color.cpy().mul(1.2f); // Менее яркое свечение
        // Уменьшаем альфа-канал для более мягкого свечения
        glowColor.a = Math.min(0.5f * strength, 0.8f);
        
        // Толщина свечения зависит от силы
        int glowSize = Math.round(2 * strength);
        
        // Свечение будет очень плавно уменьшать прозрачность
        for (int i = 1; i <= glowSize; i++) {
            // Экспоненциальное затухание альфа-канала
            float alpha = glowColor.a * (float)Math.pow(0.7, i);
            Color currentGlowColor = new Color(glowColor);
            currentGlowColor.a = alpha;
            
            pixmap.setColor(currentGlowColor);
            
            // Рисуем внешнее свечение с плавным переходом
            drawSoftOutline(pixmap, -i, -i, width + i * 2, height + i * 2, cornerRadius + i);
        }
    }
    
    /**
     * Добавляет мягкую внутреннюю тень
     */
    private void addSoftInnerShadow(Pixmap pixmap, int width, int height, int cornerRadius, Color color) {
        // Устанавливаем цвет для внутренней тени (очень мягкий)
        Color shadowColor = color.cpy().mul(0.85f);
        shadowColor.a = 0.15f; // Очень низкая непрозрачность
        pixmap.setColor(shadowColor);
        
        // Рисуем мягкую внутреннюю тень
        for (int i = 0; i < 2; i++) { // Уменьшаем количество слоев
            drawSoftOutline(pixmap, i, i, width - i * 2, height - i * 2, cornerRadius - i);
        }
        
        // Добавляем очень тонкую подсветку внизу и справа
        Color highlightColor = color.cpy().mul(1.1f); // Менее яркая подсветка
        highlightColor.a = 0.2f; // Более прозрачная
        pixmap.setColor(highlightColor);
        
        // Только один слой подсветки
        drawSoftBottomRightHighlight(pixmap, 0, width, height, cornerRadius);
    }
    
    /**
     * Рисует мягкий контур закругленного прямоугольника
     */
    private void drawSoftOutline(Pixmap pixmap, int x, int y, int width, int height, int radius) {
        if (radius <= 0) radius = 1;
        
        // Горизонтальные линии с плавным переходом
        // Верхняя часть с закругленными углами
        for (int i = y; i < y + 2; i++) {
            if (i < 0 || i >= pixmap.getHeight()) continue;
            int xOffset = calculateSmoothCorner(Math.max(0, i - y), radius);
            pixmap.drawLine(x + xOffset, i, x + width - xOffset, i);
        }
        
        // Нижняя часть с закругленными углами
        for (int i = y + height - 2; i < y + height; i++) {
            if (i < 0 || i >= pixmap.getHeight()) continue;
            int xOffset = calculateSmoothCorner(Math.max(0, y + height - i - 1), radius);
            pixmap.drawLine(x + xOffset, i, x + width - xOffset, i);
        }
        
        // Вертикальные линии с плавным переходом
        // Левая сторона
        for (int i = x; i < x + 2; i++) {
            if (i < 0 || i >= pixmap.getWidth()) continue;
            pixmap.drawLine(i, y + radius, i, y + height - radius);
        }
        
        // Правая сторона
        for (int i = x + width - 2; i < x + width; i++) {
            if (i < 0 || i >= pixmap.getWidth()) continue;
            pixmap.drawLine(i, y + radius, i, y + height - radius);
        }
    }
    
    /**
     * Рисует мягкую подсветку в правом нижнем углу
     */
    private void drawSoftBottomRightHighlight(Pixmap pixmap, int offset, int width, int height, int radius) {
        // Нижняя горизонтальная подсветка
        int y = height - 2 - offset;
        if (y >= 0 && y < pixmap.getHeight()) {
            pixmap.drawLine(radius + offset, y, width - offset, y);
        }
        
        // Правая вертикальная подсветка
        int x = width - 2 - offset;
        if (x >= 0 && x < pixmap.getWidth()) {
            pixmap.drawLine(x, offset + radius, x, height - offset - radius);
        }
    }
    
    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        Color oldColor = batch.getColor();
        batch.setColor(color);
        
        // Рисуем текстуру с сохранением пропорций, но растягивая ее на всю область
        batch.draw(texture, x, y, width, height);
        
        batch.setColor(oldColor);
    }
    
    @Override
    public void setMinWidth(float minWidth) {
        super.setMinWidth(minWidth);
    }
    
    @Override
    public void setMinHeight(float minHeight) {
        super.setMinHeight(minHeight);
    }
    
    @Override
    public float getMinWidth() {
        return super.getMinWidth();
    }
    
    @Override
    public float getMinHeight() {
        return super.getMinHeight();
    }
    
    /**
     * Освобождает ресурсы
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
    
    /**
     * Создает копию этого drawable с другим цветом
     */
    public RoundedDrawable tint(Color newColor) {
        return new RoundedDrawable(newColor, cornerRadius, withInnerShadow, glowStrength, spaceStyle);
    }
} 