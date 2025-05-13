package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
//import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
//import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * Простая версия игры Space Courier.
 * Используется в качестве демонстрации основной версии игры.
 */
public class Main extends ApplicationAdapter {
    // Константы размеров экрана
    private static final float SCREEN_WIDTH = 800;
    private static final float SCREEN_HEIGHT = 480;
    
    // Константы размеров объектов
    private static final float SHIP_WIDTH = 64;
    private static final float SHIP_HEIGHT = 64;
    private static final float ASTEROID_WIDTH = 64;
    private static final float ASTEROID_HEIGHT = 64;
    private static final float ENEMY_WIDTH = 64;
    private static final float ENEMY_HEIGHT = 64;
    private static final float FUEL_WIDTH = 32;
    private static final float FUEL_HEIGHT = 32;
    
    // Константы игрового процесса
    private static final int SHIP_SPEED = 300;
    private static final float MAX_FUEL = 100f;
    private static final float FUEL_CONSUMPTION = 2f; // единиц в секунду
    private static final int INITIAL_LIVES = 3;
    private static final long ASTEROID_SPAWN_INTERVAL = 1000000000L; // 1 секунда в наносекундах
    private static final long ENEMY_SPAWN_INTERVAL = 2000000000L; // 2 секунды в наносекундах
    private static final long FUEL_SPAWN_INTERVAL = 3000000000L; // 3 секунды в наносекундах
    
    // Графические объекты
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private BitmapFont font;

    // Текстуры
    private Texture shipImage;
    private Texture asteroidImage;
    private Texture enemyImage;
    private Texture fuelImage;
    private Texture backgroundImage;

    // Звуки и музыка
    private Sound collectSound;
    private Sound explosionSound;
    private Music gameMusic;

    // Игровые объекты
    private Rectangle playerShip;
    private Array<Rectangle> asteroids;
    private Array<Rectangle> enemies;
    private Array<Rectangle> fuelCanisters;

    // Параметры игры
    private long lastAsteroidTime;
    private long lastEnemyTime;
    private long lastFuelTime;
    private int score;
    private int lives;
    private float fuel;
    private float difficulty;
    private float playerSkill;
    private boolean gameOver;

    @Override
    public void create() {
        initializeGraphics();
        loadAssets();
        initGame();
    }
    
    /**
     * Инициализирует графические компоненты
     */
    private void initializeGraphics() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        font = new BitmapFont();
    }
    
    /**
     * Загружает ресурсы игры (текстуры, звуки, музыку)
     */
    private void loadAssets() {
        // Загрузка текстур
        shipImage = new Texture(Gdx.files.internal("ship.png"));
        asteroidImage = new Texture(Gdx.files.internal("asteroid.png"));
        enemyImage = new Texture(Gdx.files.internal("enemy.png"));
        fuelImage = new Texture(Gdx.files.internal("fuel.png"));
        backgroundImage = new Texture(Gdx.files.internal("background.png"));

        // Загрузка звуков
        collectSound = Gdx.audio.newSound(Gdx.files.internal("collect.wav"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("gamemusic.mp3"));

        // Настройка музыки
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.7f);
        gameMusic.play();
    }

    /**
     * Инициализирует игровые объекты и параметры
     */
    private void initGame() {
        // Создание корабля игрока
        playerShip = new Rectangle();
        playerShip.x = SCREEN_WIDTH / 2 - SHIP_WIDTH / 2;
        playerShip.y = 20;
        playerShip.width = SHIP_WIDTH;
        playerShip.height = SHIP_HEIGHT;

        // Инициализация игровых коллекций
        asteroids = new Array<Rectangle>();
        enemies = new Array<Rectangle>();
        fuelCanisters = new Array<Rectangle>();

        // Сброс игровых параметров
        score = 0;
        lives = INITIAL_LIVES;
        fuel = MAX_FUEL;
        difficulty = 1.0f;
        playerSkill = 1.0f;
        gameOver = false;

        // Создание первых объектов
        spawnAsteroid();
        spawnFuelCanister();
    }

    /**
     * Создает новый астероид
     */
    private void spawnAsteroid() {
        Rectangle asteroid = new Rectangle();
        asteroid.x = MathUtils.random(0, SCREEN_WIDTH - ASTEROID_WIDTH);
        asteroid.y = SCREEN_HEIGHT;
        asteroid.width = ASTEROID_WIDTH;
        asteroid.height = ASTEROID_HEIGHT;
        asteroids.add(asteroid);
        lastAsteroidTime = TimeUtils.nanoTime();
    }

    /**
     * Создает нового врага
     */
    private void spawnEnemy() {
        Rectangle enemy = new Rectangle();
        enemy.x = MathUtils.random(0, SCREEN_WIDTH - ENEMY_WIDTH);
        enemy.y = SCREEN_HEIGHT;
        enemy.width = ENEMY_WIDTH;
        enemy.height = ENEMY_HEIGHT;
        enemies.add(enemy);
        lastEnemyTime = TimeUtils.nanoTime();
    }

    /**
     * Создает новую канистру с топливом
     */
    private void spawnFuelCanister() {
        Rectangle fuelCanister = new Rectangle();
        fuelCanister.x = MathUtils.random(0, SCREEN_WIDTH - FUEL_WIDTH);
        fuelCanister.y = SCREEN_HEIGHT;
        fuelCanister.width = FUEL_WIDTH;
        fuelCanister.height = FUEL_HEIGHT;
        fuelCanisters.add(fuelCanister);
        lastFuelTime = TimeUtils.nanoTime();
    }

    @Override
    public void render() {
        // Очистка экрана
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // Обновление камеры
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Обновление игровой логики
        if (!gameOver) {
            updateGame();
        }

        // Отрисовка игры
        drawGame();

        // Обработка перезапуска игры
        if (gameOver && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            initGame();
        }
    }

    /**
     * Отрисовывает все игровые объекты
     */
    private void drawGame() {
        batch.begin();

        // Фон
        batch.draw(backgroundImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Игровые объекты
        batch.draw(shipImage, playerShip.x, playerShip.y);

        for (Rectangle asteroid : asteroids) {
            batch.draw(asteroidImage, asteroid.x, asteroid.y);
        }

        for (Rectangle enemy : enemies) {
            batch.draw(enemyImage, enemy.x, enemy.y);
        }

        for (Rectangle fuelCanister : fuelCanisters) {
            batch.draw(fuelImage, fuelCanister.x, fuelCanister.y);
        }

        // Интерфейс
        font.draw(batch, "Очки: " + score, 10, SCREEN_HEIGHT - 10);
        font.draw(batch, "Жизни: " + lives, 10, SCREEN_HEIGHT - 30);
        font.draw(batch, "Топливо: " + (int)fuel, 10, SCREEN_HEIGHT - 50);

        if (gameOver) {
            font.draw(batch, "КОНЕЦ ИГРЫ - Нажмите Enter для перезапуска", 
                SCREEN_WIDTH / 2 - 180, SCREEN_HEIGHT / 2);
        }

        batch.end();
    }

    /**
     * Обновляет игровое состояние
     */
    private void updateGame() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Обработка ввода и перемещение игрока
        handleInput(deltaTime);
        
        // Расход топлива
        updateFuel(deltaTime);
        
        // Спавн новых объектов
        spawnNewObjects();
        
        // Перемещение и обработка столкновений
        updateGameObjects(deltaTime);
        
        // Адаптация сложности
        adaptDifficulty();
    }
    
    /**
     * Обрабатывает пользовательский ввод
     */
    private void handleInput(float deltaTime) {
        // Обработка касания экрана
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            
            if (touchPos.x > playerShip.x + SHIP_WIDTH / 2) {
                playerShip.x += SHIP_SPEED * deltaTime;
            } else {
                playerShip.x -= SHIP_SPEED * deltaTime;
            }
        }
        
        // Обработка клавиатуры
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) playerShip.x -= SHIP_SPEED * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) playerShip.x += SHIP_SPEED * deltaTime;
        
        // Ограничение положения игрока в пределах экрана
        playerShip.x = MathUtils.clamp(playerShip.x, 0, SCREEN_WIDTH - SHIP_WIDTH);
    }
    
    /**
     * Обновляет расход топлива
     */
    private void updateFuel(float deltaTime) {
        fuel -= FUEL_CONSUMPTION * deltaTime;
        if (fuel <= 0) {
            loseLife();
            fuel = MAX_FUEL / 2; // При потере жизни даем половину топлива
        }
    }
    
    /**
     * Создает новые игровые объекты, если пришло время
     */
    private void spawnNewObjects() {
        // Создание астероидов
        if (TimeUtils.nanoTime() - lastAsteroidTime > ASTEROID_SPAWN_INTERVAL / difficulty) {
            spawnAsteroid();
        }
        
        // Создание врагов (после определенного количества очков)
        if (score > 500 && TimeUtils.nanoTime() - lastEnemyTime > ENEMY_SPAWN_INTERVAL / difficulty) {
            spawnEnemy();
        }
        
        // Создание топлива
        if (TimeUtils.nanoTime() - lastFuelTime > FUEL_SPAWN_INTERVAL) {
            spawnFuelCanister();
        }
    }
    
    /**
     * Обновляет положение игровых объектов и обрабатывает столкновения
     */
    private void updateGameObjects(float deltaTime) {
        updateAsteroids(deltaTime);
        updateEnemies(deltaTime);
        updateFuelCanisters(deltaTime);
    }
    
    /**
     * Обновляет положение астероидов и обрабатывает столкновения
     */
    private void updateAsteroids(float deltaTime) {
        for (int i = asteroids.size - 1; i >= 0; i--) {
            Rectangle asteroid = asteroids.get(i);
            float speed = 100 + (difficulty * 20);
            asteroid.y -= speed * deltaTime;
            
            // Удаление астероидов, вышедших за пределы экрана
            if (asteroid.y + ASTEROID_HEIGHT < 0) {
                asteroids.removeIndex(i);
                score += 10; // Очки за уклонение
                updatePlayerSkill(true);
                continue;
            }
            
            // Обработка столкновения с игроком
            if (asteroid.overlaps(playerShip)) {
                explosionSound.play(0.3f);
                asteroids.removeIndex(i);
                loseLife();
                updatePlayerSkill(false);
            }
        }
    }
    
    /**
     * Обновляет положение врагов и обрабатывает столкновения
     */
    private void updateEnemies(float deltaTime) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Rectangle enemy = enemies.get(i);
            // Вертикальное движение вниз
            float speed = 150 + (difficulty * 30);
            enemy.y -= speed * deltaTime;
            
            // Горизонтальное следование за игроком
            if (enemy.x < playerShip.x) {
                enemy.x += (60 + difficulty * 10) * deltaTime;
            } else {
                enemy.x -= (60 + difficulty * 10) * deltaTime;
            }
            
            // Удаление врагов, вышедших за пределы экрана
            if (enemy.y + ENEMY_HEIGHT < 0) {
                enemies.removeIndex(i);
                score += 30; // Больше очков за уклонение от врага
                updatePlayerSkill(true);
                continue;
            }
            
            // Обработка столкновения с игроком
            if (enemy.overlaps(playerShip)) {
                explosionSound.play(0.5f);
                enemies.removeIndex(i);
                loseLife();
                updatePlayerSkill(false);
            }
        }
    }
    
    /**
     * Обновляет положение канистр с топливом и обрабатывает сбор
     */
    private void updateFuelCanisters(float deltaTime) {
        for (int i = fuelCanisters.size - 1; i >= 0; i--) {
            Rectangle fuelCanister = fuelCanisters.get(i);
            fuelCanister.y -= 120 * deltaTime;
            
            // Удаление канистр, вышедших за пределы экрана
            if (fuelCanister.y + FUEL_HEIGHT < 0) {
                fuelCanisters.removeIndex(i);
                continue;
            }
            
            // Обработка сбора канистры игроком
            if (fuelCanister.overlaps(playerShip)) {
                collectSound.play(0.5f);
                fuelCanisters.removeIndex(i);
                fuel = Math.min(MAX_FUEL, fuel + 25);
                score += 20;
                updatePlayerSkill(true);
            }
        }
    }
    
    /**
     * Обработка потери жизни
     */
    private void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
            gameMusic.stop();
        }
    }

    /**
     * Обновляет метрику "мастерства игрока" на основе успеха или неудачи
     */
    private void updatePlayerSkill(boolean success) {
        if (success) {
            // Увеличиваем мастерство игрока при успехе
            playerSkill += 0.01f;
        } else {
            // Уменьшаем мастерство игрока при неудаче
            playerSkill -= 0.05f;
        }
        
        // Ограничиваем мастерство в диапазоне [0.5, 2.0]
        playerSkill = MathUtils.clamp(playerSkill, 0.5f, 2.0f);
    }

    /**
     * Адаптирует уровень сложности на основе мастерства игрока
     */
    private void adaptDifficulty() {
        // Постепенное увеличение сложности с течением времени
        float timeFactor = Math.min(1.0f, score / 5000f);
        
        // Адаптация к мастерству игрока (лучшие игроки получают больше сложности)
        difficulty = 1.0f + timeFactor + (playerSkill - 1.0f) * 0.5f;
        
        // Ограничение сложности
        difficulty = MathUtils.clamp(difficulty, 1.0f, 3.0f);
    }

    @Override
    public void dispose() {
        // Освобождение ресурсов
        batch.dispose();
        font.dispose();
        
        // Освобождение текстур
        shipImage.dispose();
        asteroidImage.dispose();
        enemyImage.dispose();
        fuelImage.dispose();
        backgroundImage.dispose();
        
        // Освобождение звуков
        collectSound.dispose();
        explosionSound.dispose();
        gameMusic.dispose();
    }

    /**
     * Метод для запуска приложения на десктоп (для тестирования)
     * @param args аргументы командной строки
     */
//    public static void main(String[] args) {
//        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//        config.title = "Космический Курьер";
//        config.width = 800;
//        config.height = 480;
//        new LwjglApplication(new SpaceCourierGame(), config);
//    }
}
