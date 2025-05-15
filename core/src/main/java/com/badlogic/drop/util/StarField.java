package com.badlogic.drop.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Класс для создания и отображения анимированного звездного поля с эффектом падающих звезд.
 * Создает плавную анимацию звезд разных размеров и яркости, движущихся с разной скоростью.
 */
public class StarField implements Disposable {
    // Структура для хранения данных о звезде
    private class Star {
        float x, y;            // Позиция
        float size;            // Размер
        float speed;           // Скорость падения
        float alpha;           // Прозрачность
        float trailLength;     // Длина следа (для падающих звезд)
    }
    
    // Константы
    private static final int DEFAULT_STAR_COUNT = 250; // Больше звезд для лучшего эффекта
    private static final float MIN_STAR_SIZE = 0.5f;   // Уменьшаем минимальный размер (было 1.0f)
    private static final float MAX_STAR_SIZE = 1.8f;   // Уменьшаем максимальный размер (было 3.0f)
    private static final float MIN_STAR_SPEED = 25.0f; // Увеличиваем минимальную скорость
    private static final float MAX_STAR_SPEED = 80.0f; // Увеличиваем максимальную скорость
    private static final float MIN_ALPHA = 0.5f;       // Увеличиваем минимальную прозрачность
    private static final float MAX_ALPHA = 1.0f;
    
    // Константы для следов звезд
    private static final float MIN_TRAIL_LENGTH = 3.0f;  // Уменьшаем минимальную длину следа
    private static final float MAX_TRAIL_LENGTH = 10.0f; // Уменьшаем максимальную длину следа
    
    private final Array<Star> stars;
    private final Texture starTexture;
    private final Texture trailTexture;
    private final float screenWidth;
    private final float screenHeight;
    private float deltaAccumulator = 0; // Для плавной анимации
    
    /**
     * Создает звездное поле с указанным количеством звезд
     * @param width Ширина экрана
     * @param height Высота экрана
     * @param starCount Количество звезд
     */
    public StarField(float width, float height, int starCount) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.stars = new Array<>(starCount);
        
        // Создаем текстуры
        starTexture = createStarTexture();
        trailTexture = createTrailTexture();
        
        // Инициализируем звезды
        for (int i = 0; i < starCount; i++) {
            stars.add(createRandomStar());
        }
    }
    
    /**
     * Создает звездное поле с количеством звезд по умолчанию
     * @param width Ширина экрана
     * @param height Высота экрана
     */
    public StarField(float width, float height) {
        this(width, height, DEFAULT_STAR_COUNT);
    }
    
    /**
     * Создает текстуру для отрисовки звезды
     * @return Текстура звезды
     */
    private Texture createStarTexture() {
        // Создаем пиксельную карту для текстуры звезды - чисто белая точка
        Pixmap pixmap = new Pixmap(8, 8, Pixmap.Format.RGBA8888); // Уменьшаем размер до 8x8
        pixmap.setColor(Color.WHITE);
        
        // Рисуем идеально круглую точку
        int centerX = 4, centerY = 4, radius = 3; // Центр и радиус
        
        // Заполняем круг с плавными краями
        for (int x = 0; x < pixmap.getWidth(); x++) {
            for (int y = 0; y < pixmap.getHeight(); y++) {
                float distance = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                if (distance <= radius) {
                    // Создаем плавный градиент от центра к краю
                    float alpha = 1.0f;
                    if (distance > radius * 0.7f) {
                        // Начинаем уменьшать прозрачность ближе к краю для антиалиасинга
                        alpha = 1.0f - ((distance - radius * 0.7f) / (radius * 0.3f));
                    }
                    pixmap.setColor(1, 1, 1, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        
        // Создаем текстуру из пиксельной карты
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        
        return texture;
    }
    
    /**
     * Создает текстуру для следа падающей звезды
     * @return Текстура следа
     */
    private Texture createTrailTexture() {
        // Создаем пиксельную карту для текстуры следа - простая белая полоса
        Pixmap pixmap = new Pixmap(8, 4, Pixmap.Format.RGBA8888); // Уменьшаем до 8x4
        pixmap.setColor(Color.WHITE);
        
        // Создаем градиент для следа
        for (int x = 0; x < 8; x++) {
            float alpha = 1.0f - (x / 8.0f);
            alpha = (float) Math.pow(alpha, 0.7f);
            
            // Создаем градиент по ширине (более яркий в центре)
            for (int y = 0; y < 4; y++) {
                float yAlpha = 1.0f - Math.abs((y - 2) / 2.0f);
                yAlpha = (float) Math.pow(yAlpha, 0.5f);
                pixmap.setColor(1, 1, 1, alpha * yAlpha);
                pixmap.drawPixel(x, y);
            }
        }
        
        // Создаем текстуру из пиксельной карты
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        
        return texture;
    }
    
    /**
     * Создает новую звезду со случайными параметрами
     * @return Новая звезда
     */
    private Star createRandomStar() {
        Star star = new Star();
        
        // Случайная позиция по X на экране
        star.x = MathUtils.random(0, screenWidth);
        
        // Позиция по Y - случайная для начального заполнения, потом будет сверху экрана
        star.y = MathUtils.random(0, screenHeight);
        
        // Размер звезды
        star.size = MathUtils.random(MIN_STAR_SIZE, MAX_STAR_SIZE);
        
        // Длина следа зависит от размера звезды
        star.trailLength = MathUtils.random(MIN_TRAIL_LENGTH, MAX_TRAIL_LENGTH) * (star.size / MAX_STAR_SIZE);
        
        // Скорость зависит от размера (более крупные звезды падают быстрее)
        float speedFactor = (star.size - MIN_STAR_SIZE) / (MAX_STAR_SIZE - MIN_STAR_SIZE);
        star.speed = MIN_STAR_SPEED + speedFactor * (MAX_STAR_SPEED - MIN_STAR_SPEED);
        
        // Случайная прозрачность
        star.alpha = MathUtils.random(MIN_ALPHA, MAX_ALPHA);
        
        return star;
    }
    
    /**
     * Обновляет состояние звездного поля
     * @param delta Время, прошедшее с предыдущего кадра
     */
    public void update(float delta) {
        // Накапливаем дельту для плавной анимации
        deltaAccumulator += delta;
        
        // Сглаживаем анимацию
        float smoothDelta = Math.min(deltaAccumulator, 1/30f);
        deltaAccumulator -= smoothDelta;
        
        for (int i = 0; i < stars.size; i++) {
            Star star = stars.get(i);
            
            // Движение звезды вниз
            star.y -= star.speed * smoothDelta;
            
            // Если звезда вышла за пределы экрана, создаем новую
            if (star.y < -star.size - star.trailLength) {
                // Устанавливаем новую звезду сверху экрана
                star.x = MathUtils.random(0, screenWidth);
                star.y = screenHeight + MathUtils.random(0, 10);
                
                // Обновляем параметры звезды
                star.size = MathUtils.random(MIN_STAR_SIZE, MAX_STAR_SIZE);
                star.trailLength = MathUtils.random(MIN_TRAIL_LENGTH, MAX_TRAIL_LENGTH) * (star.size / MAX_STAR_SIZE);
                
                // Скорость зависит от размера
                float speedFactor = (star.size - MIN_STAR_SIZE) / (MAX_STAR_SIZE - MIN_STAR_SIZE);
                star.speed = MIN_STAR_SPEED + speedFactor * (MAX_STAR_SPEED - MIN_STAR_SPEED);
                
                star.alpha = MathUtils.random(MIN_ALPHA, MAX_ALPHA);
            }
        }
    }
    
    /**
     * Отрисовывает звездное поле
     * @param batch SpriteBatch для отрисовки
     */
    public void render(SpriteBatch batch) {
        for (Star star : stars) {
            // Рисуем след звезды
            if (star.trailLength > 0) {
                batch.setColor(1, 1, 1, star.alpha * 0.6f);
                batch.draw(
                    trailTexture,
                    star.x - trailTexture.getWidth()/4, // центрируем по ширине
                    star.y,
                    trailTexture.getWidth()/2,
                    0,
                    trailTexture.getWidth()/2,
                    star.trailLength,
                    1, 1, 0,
                    0, 0,
                    trailTexture.getWidth(),
                    trailTexture.getHeight(),
                    false, false
                );
            }
            
            // Рисуем саму звезду
            batch.setColor(1, 1, 1, star.alpha);
            batch.draw(
                starTexture,
                star.x - star.size/2,
                star.y - star.size/2,
                star.size, star.size
            );
        }
        
        // Возвращаем исходный цвет
        batch.setColor(Color.WHITE);
    }
    
    /**
     * Освобождает ресурсы, занятые звездным полем
     */
    @Override
    public void dispose() {
        if (starTexture != null) {
            starTexture.dispose();
        }
        if (trailTexture != null) {
            trailTexture.dispose();
        }
    }
} 