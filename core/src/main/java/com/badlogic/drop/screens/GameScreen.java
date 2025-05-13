package com.badlogic.drop.screens;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.drop.systems.DifficultySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Iterator;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.GL20;

/**
 * Основной экран игрового процесса, адаптированный для Samsung Galaxy S24 Ultra.
 * Содержит всю логику игры, управление объектами, показ UI и обработку ввода.
 */
public class GameScreen implements Screen {
    // Константы размеров экрана
    private static final float GAME_WIDTH = SpaceCourierGame.GAME_WIDTH;
    private static final float GAME_HEIGHT = SpaceCourierGame.GAME_HEIGHT;

    // Константы размеров объектов
    private static final int SHIP_SIZE = 96;
    private static final int ASTEROID_SIZE = 96;
    private static final int ENEMY_SIZE = 96;
    private static final int FUEL_SIZE = 48;
    private static final int HEART_SIZE = 48;
    private static final int POWERUP_SIZE = 64;
    private static final float POWERUP_ICON_SIZE = 40;
    private static final int BOSS_PROJECTILE_SIZE = 48; // Размер снаряда босса
    private static final int PLAYER_PROJECTILE_SIZE = 32; // Размер снаряда игрока

    // Константы игрового процесса
    private static final int SHIP_SPEED = 500;
    private static final float SPEED_BOOST_MULTIPLIER = 1.5f;
    private static final float MAX_FUEL = 100f;
    private static final float FUEL_CONSUMPTION = 2.5f; // Увеличено с 2.4f до 2.8f для более быстрого расхода
    private static final int MAX_LIVES = 3;
    private static final float PLAYER_PROJECTILE_SPEED = 400f; // Скорость снаряда игрока
    private static final long PLAYER_SHOOT_COOLDOWN = 500000000L; // Задержка между выстрелами игрока (0.5 сек)

    // Константы интервалов появления объектов
    private static final long GROUP_PAUSE = 2000000000L; // Уменьшено с 3000000000L до 2000000000L

    // Константы для анимаций и уведомлений
    private static final float LEVEL_UP_ANIMATION_DURATION = 2.0f;
    private static final float ACHIEVEMENT_NOTIFICATION_DURATION = 3.0f;

    // Ссылка на основной класс игры
    private final SpaceCourierGame game;

    // Графические объекты
    private OrthographicCamera camera;
    private BitmapFont font;

    // Текстуры
    private Texture shipImage;
    private Texture asteroidImage;
    private Texture enemyImage;
    private Texture fuelImage;
    private Texture heartImage;
    private Texture backgroundImage;
    private Texture pauseButtonTexture;
    private Texture shieldTexture;
    private Texture speedBoostTexture;
    private Texture magnetTexture;
    private Texture doubleScoreTexture;
    private Texture bossTexture;
    private Texture bossProjectileTexture; // Текстура снаряда босса
    private Texture playerProjectileTexture; // Текстура снаряда игрока

    // Звуки и музыка
    private Sound collectSound;
    private Sound explosionSound;
    private Music gameMusic;

    // Игровые объекты
    private Rectangle ship;
    private Array<Rectangle> asteroids;
    private Array<Rectangle> enemies;
    private Array<Rectangle> fuelCanisters;
    private Array<Rectangle> hearts;
    private Array<Powerup> powerups;
    private Array<BossProjectile> bossProjectiles; // Снаряды босса
    private Array<PlayerProjectile> playerProjectiles; // Снаряды игрока

    // Статистика для достижений
    private float gameTime;
    private int fuelCollected;
    private boolean damageTaken;
    private boolean achievementNotificationActive;
    private float achievementNotificationTime;
    private String achievementNotificationText;

    // Оповещение о низком уровне топлива
    private boolean lowFuelWarningActive;
    private float lowFuelWarningTime;
    private static final float LOW_FUEL_WARNING_DURATION = 2.0f;
    private static final float LOW_FUEL_THRESHOLD = 20.0f; // 20%

    // Состояние бонусов
    private boolean shieldActive;
    private boolean speedBoostActive;
    private boolean magnetActive;
    private boolean doubleScoreActive;

    // Параметры игры
    private long lastAsteroidTime;
    private long lastEnemyTime;
    private long lastFuelTime;
    private long lastHeartTime;
    private int score;
    private int lives;
    private float fuel;
    private boolean gameOver;
    private boolean needHeart;

    // Параметры групп объектов
    private int asteroidsInGroup = 0;
    private int maxAsteroidsPerGroup = 1; // Увеличено с 3 до 5
    private int enemiesInGroup = 0;
    private int maxEnemiesPerGroup = 1; // Увеличено с 2 до 3

    // Анимация нового уровня
    private boolean showLevelUpAnimation;
    private float levelUpAnimationTime;
    private String levelUpMessage;

    // Система настройки сложности
    private DifficultySystem difficultySystem;

    // UI для экрана проигрыша
    private Stage gameOverStage;
    private Skin gameOverSkin;
    private TextButton restartButton;
    private TextButton menuButton;
    private TextButton scoreLabel;

    // UI для экрана паузы
    private Stage pauseStage;
    private Skin pauseSkin;
    private TextButton continueButton;
    private TextButton pauseMenuButton;
    private boolean isPaused;
    private Rectangle pauseButtonRect;

    // Внутренние флаги отрисовки
    private boolean forceGameOverRender = false;

    // Константы для боссов
    private static final int BOSS_SIZE = 160;
    private static final int BOSS_HEALTH_MAX = 20;
    private static final float BOSS_SPEED = 80f;
    private static final float BOSS_PROJECTILE_SPEED = 200f; // Скорость снаряда босса
    private static final long BOSS_SHOOT_INTERVAL = 1500000000L; // Интервал стрельбы (1.5 секунды)

    // Параметры боссов
    private Rectangle boss;
    private boolean bossActive;
    private int bossHealth;
    private float bossMoveDirection = 1f; // 1 = вправо, -1 = влево
    private long lastBossAttackTime;
    private boolean bossDefeated;
    private float bossInvulnerabilityTimer;

    // Параметры стрельбы игрока
    private long lastPlayerShootTime = 0; // Время последнего выстрела игрока

    /**
     * Класс для бонусов в игре
     */
    private class Powerup {
        Rectangle bounds;
        PowerupType type;
        float activeDuration;
        float activeTime;
        boolean active;

        /**
         * Создает новый бонус указанного типа в заданной позиции
         */
        Powerup(float x, float y, PowerupType type) {
            this.bounds = new Rectangle(x, y, POWERUP_SIZE, POWERUP_SIZE);
            this.type = type;
            this.activeDuration = 10f;
            this.activeTime = 0;
            this.active = false;
        }
    }

    /**
     * Типы бонусов в игре
     */
    private enum PowerupType {
        SHIELD,        // Защита от столкновений
        SPEED_BOOST,   // Увеличение скорости
        MAGNET,        // Притягивает топливо и сердечки
        DOUBLE_SCORE   // Удвоение очков
    }

    /**
     * Класс для снарядов босса
     */
    private class BossProjectile {
        Rectangle bounds;
        float speedX;
        float speedY;

        /**
         * Создает новый снаряд босса в указанной позиции
         */
        BossProjectile(float x, float y) {
            this.bounds = new Rectangle(x, y, BOSS_PROJECTILE_SIZE, BOSS_PROJECTILE_SIZE);

            // Направляем снаряд в сторону игрока
            float dx = ship.x + ship.width/2 - x;
            float dy = ship.y + ship.height/2 - y;

            // Нормализуем вектор
            float length = (float)Math.sqrt(dx*dx + dy*dy);
            if (length != 0) {
                dx /= length;
                dy /= length;
            }

            // Устанавливаем скорость снаряда
            this.speedX = dx * BOSS_PROJECTILE_SPEED;
            this.speedY = dy * BOSS_PROJECTILE_SPEED;
        }

        /**
         * Обновляет позицию снаряда
         */
        void update(float delta) {
            bounds.x += speedX * delta;
            bounds.y += speedY * delta;
        }

        /**
         * Проверяет, находится ли снаряд за пределами экрана
         */
        boolean isOutOfScreen() {
            return bounds.x < -bounds.width || bounds.x > GAME_WIDTH ||
                   bounds.y < -bounds.height || bounds.y > GAME_HEIGHT;
        }
    }

    /**
     * Класс для снарядов игрока
     */
    private class PlayerProjectile {
        Rectangle bounds;

        /**
         * Создает новый снаряд игрока в указанной позиции
         */
        PlayerProjectile(float x, float y) {
            this.bounds = new Rectangle(x, y, PLAYER_PROJECTILE_SIZE, PLAYER_PROJECTILE_SIZE);
        }

        /**
         * Обновляет позицию снаряда (движение вверх)
         */
        void update(float delta) {
            bounds.y += PLAYER_PROJECTILE_SPEED * delta;
        }

        /**
         * Проверяет, находится ли снаряд за пределами экрана
         */
        boolean isOutOfScreen() {
            return bounds.y > GAME_HEIGHT;
        }
    }

    /**
     * Создает новый экран игры
     */
    public GameScreen(final SpaceCourierGame game) {
        this.game = game;

        // Инициализируем графику (включая pauseStage)
        initializeGraphics();

        // Загружаем игровые ресурсы
        loadResources();

        // Проверяем состояние аудио (звуки и музыка)
        checkAudioSettings();

        // Создаем экран конца игры
        createGameOverUI();

        // Создаем систему управления сложностью
        difficultySystem = new DifficultySystem();

        // Инициализируем игру
        initGame();
    }

    /**
     * Инициализирует графику (камера, шрифты и т.д.)
     */
    private void initializeGraphics() {
        // Настройка камеры
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);

        font = game.fontManager.getGameFont();

        // Создаем область для кнопки паузы - увеличиваем размер для удобства нажатия
        pauseButtonRect = new Rectangle(
            GAME_WIDTH - 90, GAME_HEIGHT - 90, 70, 70
        );

        // Инициализация сцены паузы
        pauseStage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));

        // Создаем UI паузы сразу
        createPauseUI();
    }

    /**
     * Загружает текстуры и звуки
     */
    private void loadResources() {
        // Загрузка текстур
        shipImage = new Texture(Gdx.files.internal("ship.png"));
        shipImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        asteroidImage = new Texture(Gdx.files.internal("asteroid.png"));
        asteroidImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        enemyImage = new Texture(Gdx.files.internal("enemy.png"));
        enemyImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        bossTexture = new Texture(Gdx.files.internal("boss.png"));
        bossTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        fuelImage = new Texture(Gdx.files.internal("fuel.png"));
        fuelImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        heartImage = new Texture(Gdx.files.internal("heart.png"));
        heartImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        backgroundImage = new Texture(Gdx.files.internal("background.png"));
        backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        pauseButtonTexture = new Texture(Gdx.files.internal("pause_button.png"));
        pauseButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Загрузка текстур бонусов
        shieldTexture = new Texture(Gdx.files.internal("shield.png"));
        speedBoostTexture = new Texture(Gdx.files.internal("speed.png"));
        magnetTexture = new Texture(Gdx.files.internal("magnet.png"));
        doubleScoreTexture = new Texture(Gdx.files.internal("double_score.png"));

        bossProjectileTexture = new Texture(Gdx.files.internal("boss_projectile.png"));
        bossProjectileTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        playerProjectileTexture = new Texture(Gdx.files.internal("player_projectile.png"));
        playerProjectileTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Загрузка звуков
        collectSound = Gdx.audio.newSound(Gdx.files.internal("collect.wav"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("gamemusic.mp3"));

        // Настройка музыки
        gameMusic.setLooping(true);
        game.soundManager.setMusic(gameMusic, true);
    }

    private void initGame() {
        // Создание корабля игрока (в центре экрана)
        ship = new Rectangle();
        ship.x = GAME_WIDTH / 2 - SHIP_SIZE / 2;
        ship.y = GAME_HEIGHT / 2 - SHIP_SIZE / 2; // Помещаем корабль в центр экрана
        ship.width = SHIP_SIZE;
        ship.height = SHIP_SIZE;

        // Инициализация игровых коллекций
        asteroids = new Array<Rectangle>();
        enemies = new Array<Rectangle>();
        fuelCanisters = new Array<Rectangle>();
        hearts = new Array<Rectangle>(); // Инициализация коллекции сердечек
        powerups = new Array<Powerup>(); // Инициализация коллекции бонусов
        bossProjectiles = new Array<BossProjectile>(); // Инициализация коллекции снарядов босса
        playerProjectiles = new Array<PlayerProjectile>(); // Инициализация коллекции снарядов игрока

        // Сброс игровых параметров
        score = 0;
        lives = 3;
        fuel = MAX_FUEL;
        gameOver = false;
        needHeart = false; // Изначально сердечки не нужны, так как жизни полные
        isPaused = false; // Сбрасываем состояние паузы

        // Сбрасываем счетчики групп
        asteroidsInGroup = 0;
        enemiesInGroup = 0;

        // Сбрасываем состояние бонусов
        shieldActive = false;
        speedBoostActive = false;
        magnetActive = false;
        doubleScoreActive = false;

        // Сбрасываем состояние боссов
        bossActive = false;
        bossDefeated = false;
        bossHealth = 0;
        boss = null;
        bossProjectiles.clear(); // Очищаем снаряды босса
        playerProjectiles.clear(); // Очищаем снаряды игрока

        // Сброс системы сложности
        difficultySystem.reset();

        // Сброс статистики для достижений
        gameTime = 0;
        fuelCollected = 0;
        damageTaken = false;
        achievementNotificationActive = false;

        // Разблокируем достижение за первый полет
        if (game.achievementSystem.unlockAchievement(AchievementSystem.ACHIEVEMENT_FIRST_FLIGHT)) {
            showAchievementNotification("Достижение разблокировано: Первый полет");
        }

        // Сброс параметров стрельбы
        lastPlayerShootTime = 0;

        // Создание первых объектов
        spawnAsteroid();
        spawnFuelCanister();
    }

    private void spawnAsteroid() {
        Rectangle asteroid = new Rectangle();
        asteroid.width = ASTEROID_SIZE;
        asteroid.height = ASTEROID_SIZE;

        // Максимальное количество попыток найти свободное место
        int maxAttempts = 10;
        boolean hasOverlap = true;

        // Пытаемся найти место без пересечений
        for (int attempt = 0; attempt < maxAttempts && hasOverlap; attempt++) {
            // Задаем случайную позицию X
            asteroid.x = MathUtils.random(0, GAME_WIDTH - ASTEROID_SIZE);
            asteroid.y = GAME_HEIGHT; // Сверху экрана

            // Проверяем пересечения с другими астероидами
            hasOverlap = false;
            for (Rectangle existingAsteroid : asteroids) {
                // Допускаем небольшое наложение (80% от размера)
                float minDistance = ASTEROID_SIZE * 0.8f;

                // Проверяем расстояние между центрами объектов
                float dx = (existingAsteroid.x + existingAsteroid.width/2) - (asteroid.x + asteroid.width/2);
                float dy = (existingAsteroid.y + existingAsteroid.height/2) - (asteroid.y + asteroid.height/2);
                float distance = (float)Math.sqrt(dx*dx + dy*dy);

                if (distance < minDistance) {
                    hasOverlap = true;
                    break;
                }
            }
        }

        // Добавляем астероид
        asteroids.add(asteroid);
        lastAsteroidTime = TimeUtils.nanoTime();
    }

    private void spawnEnemy() {
        Rectangle enemy = new Rectangle();
        enemy.width = ENEMY_SIZE;
        enemy.height = ENEMY_SIZE;

        // Максимальное количество попыток найти свободное место
        int maxAttempts = 10;
        boolean hasOverlap = true;

        // Пытаемся найти место без пересечений
        for (int attempt = 0; attempt < maxAttempts && hasOverlap; attempt++) {
            // Задаем случайную позицию X
            enemy.x = MathUtils.random(0, GAME_WIDTH - ENEMY_SIZE);
            enemy.y = GAME_HEIGHT; // Сверху экрана

            // Проверяем пересечения с астероидами
            hasOverlap = false;

            // Проверка пересечения с другими врагами
            for (Rectangle existingEnemy : enemies) {
                // Допускаем небольшое наложение (80% от размера)
                float minDistance = ENEMY_SIZE * 0.8f;

                // Проверяем расстояние между центрами объектов
                float dx = (existingEnemy.x + existingEnemy.width/2) - (enemy.x + enemy.width/2);
                float dy = (existingEnemy.y + existingEnemy.height/2) - (enemy.y + enemy.height/2);
                float distance = (float)Math.sqrt(dx*dx + dy*dy);

                if (distance < minDistance) {
                    hasOverlap = true;
                    break;
                }
            }

            // Если нет пересечения с врагами, проверяем пересечения с астероидами
            if (!hasOverlap) {
                for (Rectangle asteroid : asteroids) {
                    // Допускаем небольшое наложение (70% от суммы размеров)
                    float minDistance = (ENEMY_SIZE + ASTEROID_SIZE) * 0.35f;

                    // Проверяем расстояние между центрами объектов
                    float dx = (asteroid.x + asteroid.width/2) - (enemy.x + enemy.width/2);
                    float dy = (asteroid.y + asteroid.height/2) - (enemy.y + enemy.height/2);
                    float distance = (float)Math.sqrt(dx*dx + dy*dy);

                    if (distance < minDistance) {
                        hasOverlap = true;
                        break;
                    }
                }
            }
        }

        // Добавляем врага
        enemies.add(enemy);
        lastEnemyTime = TimeUtils.nanoTime();
    }

    private void spawnFuelCanister() {
        Rectangle fuelCanister = new Rectangle();
        fuelCanister.width = FUEL_SIZE;
        fuelCanister.height = FUEL_SIZE;

        // Максимальное количество попыток найти свободное место
        int maxAttempts = 15; // Больше попыток, так как топливо важнее
        boolean hasOverlap = true;

        // Пытаемся найти место без пересечений
        for (int attempt = 0; attempt < maxAttempts && hasOverlap; attempt++) {
            // Задаем случайную позицию X
            fuelCanister.x = MathUtils.random(0, GAME_WIDTH - FUEL_SIZE);
            fuelCanister.y = GAME_HEIGHT; // Сверху экрана

            hasOverlap = false;

            // Проверка пересечения с астероидами
            for (Rectangle asteroid : asteroids) {
                // Допускаем небольшое наложение
                float minDistance = (FUEL_SIZE + ASTEROID_SIZE) * 0.4f;

                // Проверяем расстояние между центрами объектов
                float dx = (asteroid.x + asteroid.width/2) - (fuelCanister.x + fuelCanister.width/2);
                float dy = (asteroid.y + asteroid.height/2) - (fuelCanister.y + fuelCanister.height/2);
                float distance = (float)Math.sqrt(dx*dx + dy*dy);

                if (distance < minDistance) {
                    hasOverlap = true;
                    break;
                }
            }

            // Если нет пересечения с астероидами, проверяем пересечения с врагами
            if (!hasOverlap) {
                for (Rectangle enemy : enemies) {
                    // Допускаем небольшое наложение
                    float minDistance = (FUEL_SIZE + ENEMY_SIZE) * 0.4f;

                    // Проверяем расстояние между центрами объектов
                    float dx = (enemy.x + enemy.width/2) - (fuelCanister.x + fuelCanister.width/2);
                    float dy = (enemy.y + enemy.height/2) - (fuelCanister.y + fuelCanister.height/2);
                    float distance = (float)Math.sqrt(dx*dx + dy*dy);

                    if (distance < minDistance) {
                        hasOverlap = true;
                        break;
                    }
                }
            }

            // Если слишком много попыток безуспешны, уменьшаем требования к расстоянию
            if (attempt > maxAttempts * 0.7f && hasOverlap) {
                // На последних попытках разрешаем размещать с минимальными ограничениями
                hasOverlap = false;
            }
        }

        // Добавляем канистру с топливом
        fuelCanisters.add(fuelCanister);
        lastFuelTime = TimeUtils.nanoTime();
    }

    /**
     * Создает новое сердечко для восстановления жизни
     */
    private void spawnHeart() {
        Rectangle heart = new Rectangle();
        heart.width = HEART_SIZE;
        heart.height = HEART_SIZE;

        // Максимальное количество попыток найти свободное место
        int maxAttempts = 15; // Больше попыток, так как сердечки важны
        boolean hasOverlap = true;

        // Пытаемся найти место без пересечений
        for (int attempt = 0; attempt < maxAttempts && hasOverlap; attempt++) {
            // Задаем случайную позицию X
            heart.x = MathUtils.random(0, GAME_WIDTH - HEART_SIZE);
            heart.y = GAME_HEIGHT; // Сверху экрана

            hasOverlap = false;

            // Проверка пересечения с астероидами
            for (Rectangle asteroid : asteroids) {
                // Допускаем небольшое наложение
                float minDistance = (HEART_SIZE + ASTEROID_SIZE) * 0.4f;

                // Проверяем расстояние между центрами объектов
                float dx = (asteroid.x + asteroid.width/2) - (heart.x + heart.width/2);
                float dy = (asteroid.y + asteroid.height/2) - (heart.y + heart.height/2);
                float distance = (float)Math.sqrt(dx*dx + dy*dy);

                if (distance < minDistance) {
                    hasOverlap = true;
                    break;
                }
            }

            // Если нет пересечения с астероидами, проверяем пересечения с врагами
            if (!hasOverlap) {
                for (Rectangle enemy : enemies) {
                    // Допускаем небольшое наложение
                    float minDistance = (HEART_SIZE + ENEMY_SIZE) * 0.4f;

                    // Проверяем расстояние между центрами объектов
                    float dx = (enemy.x + enemy.width/2) - (heart.x + heart.width/2);
                    float dy = (enemy.y + enemy.height/2) - (heart.y + heart.height/2);
                    float distance = (float)Math.sqrt(dx*dx + dy*dy);

                    if (distance < minDistance) {
                        hasOverlap = true;
                        break;
                    }
                }
            }

            // Проверка пересечения с топливом
            if (!hasOverlap) {
                for (Rectangle fuel : fuelCanisters) {
                    // Минимальное расстояние между центрами
                    float minDistance = (HEART_SIZE + FUEL_SIZE) * 0.5f;

                    // Проверяем расстояние между центрами объектов
                    float dx = (fuel.x + fuel.width/2) - (heart.x + heart.width/2);
                    float dy = (fuel.y + fuel.height/2) - (heart.y + heart.height/2);
                    float distance = (float)Math.sqrt(dx*dx + dy*dy);

                    if (distance < minDistance) {
                        hasOverlap = true;
                        break;
                    }
                }
            }

            // Если слишком много попыток безуспешны, уменьшаем требования к расстоянию
            if (attempt > maxAttempts * 0.7f && hasOverlap) {
                // На последних попытках разрешаем размещать с минимальными ограничениями
                hasOverlap = false;
            }
        }

        // Добавляем сердечко
        hearts.add(heart);
        lastHeartTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        // Очистка экрана
        clearScreen();

        // Обработка ввода (должна быть раньше обновления игры)
        handleInput(delta);

        // Обновление игрового состояния, если игра не на паузе
        if (!isPaused && !gameOver) {
            updateGame(delta);
        }

        // Отрисовка игры
        drawGame(delta);

        // Отрисовка UI оверлеев (интерфейс паузы, конца игры)
        drawUI(delta);
    }

    /**
     * Очищает экран перед рендерингом
     */
    private void clearScreen() {
        // Очистка экрана с цветом фона игры
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Обновление камеры
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Отрисовывает все игровые объекты
     */
    private void drawGame(float delta) {
        game.batch.begin();

        // Фон
        game.batch.draw(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT);

        // Отрисовка объектов только если игра не окончена
        if (!gameOver || forceGameOverRender) {
            // Игровые объекты
            drawGameObjects();

            // Интерфейс
            drawGameInterface();

            // Отрисовка кнопки паузы (если игра не на паузе)
            if (!isPaused && pauseButtonTexture != null) {
                game.batch.draw(pauseButtonTexture, pauseButtonRect.x, pauseButtonRect.y,
                                pauseButtonRect.width, pauseButtonRect.height);
            }

            // Если есть активная анимация нового уровня, рисуем ее
            if (showLevelUpAnimation) {
                drawLevelUpAnimation(delta);
            }

            // Если есть активное уведомление о достижении, рисуем его
            if (achievementNotificationActive) {
                drawAchievementNotification(delta);
            }

            // Если активно предупреждение о низком уровне топлива, отображаем его
            if (lowFuelWarningActive) {
                drawLowFuelWarning(delta);
            }
        }

        game.batch.end();
    }

    /**
     * Отрисовывает UI оверлеи (экраны паузы, конца игры)
     */
    private void drawUI(float delta) {
        // Если игра окончена, рисуем экран проигрыша
        if (gameOver) {
            // Отрисовка сцены с кнопками конца игры
            gameOverStage.act(delta);
            gameOverStage.draw();
        }

        // Если игра на паузе, рисуем экран паузы
        if (isPaused) {
            // Отрисовка сцены с кнопками паузы
            pauseStage.act(delta);
            pauseStage.draw();
        }
    }

    // Отрисовка игрового интерфейса
    private void drawGameInterface() {
        // Отображение счета
        font.draw(game.batch, "Score: " + score, 20, GAME_HEIGHT - 20);

        // Отображение уровня, сложности и прогресса в процентах
        int currentLevel = difficultySystem.getCurrentLevel();
        int nextLevelScore = difficultySystem.getScoreForNextLevel();
        float difficulty = difficultySystem.getDifficulty();

        font.draw(game.batch, "Level: " + currentLevel, 20, GAME_HEIGHT - 60);

        // Вычисляем прогресс до следующего уровня в процентах
        int progressToNextLevel = 0;

        if (currentLevel > 1) {
            int prevLevelScore = difficultySystem.getLevelThreshold(currentLevel - 1);
            float progress = (float)(score - prevLevelScore) / (nextLevelScore - prevLevelScore);
            progressToNextLevel = (int)(progress * 100);
        } else {
            float progress = (float)score / nextLevelScore;
            progressToNextLevel = (int)(progress * 100);
        }

        // Показываем текущую сложность (от 1.0 до 10.0) и прогресс в процентах
        font.draw(game.batch, "Difficulty: " + String.format("%.1f", difficulty), 20, GAME_HEIGHT - 90);
        font.draw(game.batch, "Next level: " + progressToNextLevel + "%", 20, GAME_HEIGHT - 120);

        // Отображение уровня топлива в процентах
        int fuelPercent = (int)((fuel / MAX_FUEL) * 100);

        // Отображение иконки топлива с процентным значением в нижнем левом углу
        float fuelIconSize = 35; // Размер иконки топлива
        float fuelTextOffsetX = fuelIconSize + 10; // Отступ для текста процента
        float fuelY = 40;

        // Отрисовываем иконку топлива
        game.batch.draw(fuelImage, 20, fuelY, fuelIconSize, fuelIconSize);

        // Определяем цвет для текста процента топлива (зеленый -> желтый -> красный)
        if (fuelPercent > 60) {
            font.setColor(0.2f, 0.8f, 0.2f, 1.0f); // Зеленый для высокого уровня топлива
        } else if (fuelPercent > 30) {
            font.setColor(0.8f, 0.8f, 0.2f, 1.0f); // Желтый для среднего уровня топлива
        } else {
            font.setColor(0.8f, 0.2f, 0.2f, 1.0f); // Красный для низкого уровня топлива
        }

        // Отрисовываем текст процента топлива
        font.draw(game.batch, fuelPercent + "%", 20 + fuelTextOffsetX, fuelY + fuelIconSize/2 + 5);

        // Возвращаем цвет шрифта к белому
        font.setColor(1, 1, 1, 1);

        // Отображение сердечек для жизней (вместо числа)
        float heartIconSize = 35; // Размер сердечка
        float heartSpacing = 5; // Расстояние между сердечками
        float heartsStartX = 20; // Начальная X-координата для сердечек
        float heartsY = 85; // Y-координата для сердечек (над индикатором топлива)

        // Отрисовка сердечек в соответствии с числом жизней
        for (int i = 0; i < MAX_LIVES; i++) {
            if (i < lives) {
                // Полное сердечко для имеющихся жизней
                game.batch.setColor(1, 1, 1, 1);
            } else {
                // Прозрачное сердечко для потерянных жизней
                game.batch.setColor(1, 1, 1, 0.3f);
            }
            game.batch.draw(heartImage, heartsStartX + i * (heartIconSize + heartSpacing), heartsY, heartIconSize, heartIconSize);
        }

        // Возвращаем нормальный цвет
        game.batch.setColor(1, 1, 1, 1);
        font.setColor(1, 1, 1, 1);

        // Отображение активных бонусов
        float iconX = GAME_WIDTH - POWERUP_ICON_SIZE - 20;

        if (shieldActive) {
            game.batch.draw(shieldTexture, iconX, GAME_HEIGHT - POWERUP_ICON_SIZE - 20, POWERUP_ICON_SIZE, POWERUP_ICON_SIZE);
            iconX -= POWERUP_ICON_SIZE + 10;
        }

        if (speedBoostActive) {
            game.batch.draw(speedBoostTexture, iconX, GAME_HEIGHT - POWERUP_ICON_SIZE - 20, POWERUP_ICON_SIZE, POWERUP_ICON_SIZE);
            iconX -= POWERUP_ICON_SIZE + 10;
        }

        if (magnetActive) {
            game.batch.draw(magnetTexture, iconX, GAME_HEIGHT - POWERUP_ICON_SIZE - 20, POWERUP_ICON_SIZE, POWERUP_ICON_SIZE);
            iconX -= POWERUP_ICON_SIZE + 10;
        }

        if (doubleScoreActive) {
            game.batch.draw(doubleScoreTexture, iconX, GAME_HEIGHT - POWERUP_ICON_SIZE - 20, POWERUP_ICON_SIZE, POWERUP_ICON_SIZE);
        }
    }

    // Отрисовка анимации нового уровня
    private void drawLevelUpAnimation(float delta) {
        // Обновляем таймер анимации
        levelUpAnimationTime += delta;

        // Проверяем, завершилась ли анимация
        if (levelUpAnimationTime >= LEVEL_UP_ANIMATION_DURATION) {
            showLevelUpAnimation = false;
            levelUpAnimationTime = 0;
            return;
        }

        // Расчет прозрачности текста (сначала появляется, потом исчезает)
        float alpha;
        if (levelUpAnimationTime < LEVEL_UP_ANIMATION_DURATION / 2) {
            alpha = levelUpAnimationTime / (LEVEL_UP_ANIMATION_DURATION / 2);
        } else {
            alpha = 1.0f - (levelUpAnimationTime - LEVEL_UP_ANIMATION_DURATION / 2) / (LEVEL_UP_ANIMATION_DURATION / 2);
        }

        // Отрисовка полупрозрачного фона
        game.batch.setColor(0, 0, 0, alpha * 0.7f);
        game.batch.draw(backgroundImage, 0, GAME_HEIGHT/2 - 100, GAME_WIDTH, 200);

        // Получаем шрифт заголовка
        BitmapFont levelFont = game.fontManager.getTitleFont();
        levelFont.setColor(1, 1, 1, alpha);

        // Измеряем ширину текста для центрирования
        GlyphLayout layout = new GlyphLayout(levelFont, levelUpMessage);
        float x = (GAME_WIDTH - layout.width) / 2;
        float y = GAME_HEIGHT / 2 + layout.height / 2;

        // Отрисовка текста уровня
        levelFont.draw(game.batch, levelUpMessage, x, y);

        // Сбрасываем цвет
        levelFont.setColor(1, 1, 1, 1);
        game.batch.setColor(1, 1, 1, 1);
    }

    private void updateGame(float delta) {
        // Обновляем время игры
        gameTime += delta;

        // Проверяем достижение "Выжить 5 минут"
        if (gameTime >= 300 && !gameOver) { // 300 секунд = 5 минут
            if (game.achievementSystem.unlockAchievement(AchievementSystem.ACHIEVEMENT_SURVIVE_5_MIN)) {
                showAchievementNotification("Достижение разблокировано: Долгий путь");
            }
        }

        // Проверяем достижение "Набрать 10000 очков"
        if (score >= 10000) {
            if (game.achievementSystem.unlockAchievement(AchievementSystem.ACHIEVEMENT_SCORE_10000)) {
                showAchievementNotification("Достижение разблокировано: Звездный рейтинг");
            }
        }

        // Проверяем достижение "Неуязвимый" (если набрали 3000 очков без урона)
        if (score >= 3000 && !damageTaken) {
            if (game.achievementSystem.unlockAchievement(AchievementSystem.ACHIEVEMENT_NO_DAMAGE)) {
                showAchievementNotification("Достижение разблокировано: Неуязвимый");
            }
        }

        // Проверяем достижение "Достичь 5 уровня"
        if (difficultySystem.getCurrentLevel() >= 5) {
            if (game.achievementSystem.updateProgress(AchievementSystem.ACHIEVEMENT_LEVEL_5, difficultySystem.getCurrentLevel())) {
                showAchievementNotification("Достижение разблокировано: Опытный курьер");
            }
        }

        // Проверяем достижение "Достичь 10 уровня"
        if (difficultySystem.getCurrentLevel() >= 10) {
            if (game.achievementSystem.updateProgress(AchievementSystem.ACHIEVEMENT_LEVEL_10, difficultySystem.getCurrentLevel())) {
                showAchievementNotification("Достижение разблокировано: Мастер доставки");
            }
        }

        // Проверка на изменение уровня
        if (difficultySystem.hasLevelChanged()) {
            showLevelUpAnimation = true;
            levelUpAnimationTime = 0;
            levelUpMessage = "LEVEL " + difficultySystem.getCurrentLevel() + "!";

            // Создаем бонус при переходе на новый уровень
            spawnPowerupOnLevelUp();
        }

        // Обновление системы сложности
        difficultySystem.update(score, delta);
        float difficulty = difficultySystem.getDifficulty();

        // Обновление бонусов
        updatePowerups(delta);

        // Обработка ввода и движение корабля
        handleInput(delta);

        // Расход топлива
        float prevFuel = fuel;
        fuel -= FUEL_CONSUMPTION * delta;

        // Проверка на низкий уровень топлива
        if (fuel <= MAX_FUEL * (LOW_FUEL_THRESHOLD / 100f) && prevFuel > MAX_FUEL * (LOW_FUEL_THRESHOLD / 100f)) {
            showLowFuelWarning();
        }

        if (fuel <= 0) {
            loseLife();
            fuel = MAX_FUEL / 2; // Дадим половину бака при потере жизни
        }

        // Создание новых объектов (сложность влияет на частоту появления)
        // Создаем новые объекты только если босс не активен
        if (!bossActive) {
            // Используем систему групп для астероидов
            if (TimeUtils.nanoTime() - lastAsteroidTime > 2000000000L / difficulty) { // Уменьшено с 2500000000L до 2000000000L
                // Проверяем, нужно ли делать паузу между группами
                if (asteroidsInGroup < maxAsteroidsPerGroup) {
                    spawnAsteroid();
                    asteroidsInGroup++;
                } else if (TimeUtils.nanoTime() - lastAsteroidTime > GROUP_PAUSE) {
                    // После паузы сбрасываем счетчик группы и создаем новый астероид
                    asteroidsInGroup = 1;
                    spawnAsteroid();
                }
            }

            // Враги появляются только после определенного количества очков и в зависимости от сложности
            // Используем систему групп для врагов
            if (score > 300 && TimeUtils.nanoTime() - lastEnemyTime > 4000000000L / difficulty) { // Уменьшен порог с 500 до 300 очков и интервал с 5000000000L до 4000000000L
                // Проверяем, нужно ли делать паузу между группами
                if (enemiesInGroup < maxEnemiesPerGroup) {
                    spawnEnemy();
                    enemiesInGroup++;
                } else if (TimeUtils.nanoTime() - lastEnemyTime > GROUP_PAUSE) {
                    // После паузы сбрасываем счетчик группы и создаем нового врага
                    enemiesInGroup = 1;
                    spawnEnemy();
                }
            }
        }

        // Топливо появляется даже во время боя с боссом
        if (TimeUtils.nanoTime() - lastFuelTime > 11000000000L) { // Увеличено с 9000000000L до 11000000000L
            spawnFuelCanister();
        }

        // Сердечки появляются только если игрок потерял жизнь и они нужны (флаг needHeart)
        // Они также доступны во время боя с боссом
        if (needHeart && TimeUtils.nanoTime() - lastHeartTime > 5000000000L) { // Редкое появление, каждые 5 секунд
            spawnHeart();
        }

        // Увеличение сложности с течением времени
        difficultySystem.update(score, delta);

        // Обновление движения объектов
        updateAsteroids(delta, difficulty);
        updateEnemies(delta, difficulty);
        updateFuelCanisters(delta);
        updateHearts(delta); // Обновление сердечек

        // Применяем магнитное притяжение, если активно
        applyMagneticEffect(delta);

        // Проверяет необходимость активации босса на основе текущего уровня
        checkForBossLevel();

        // Обновляет состояние босса
        updateBoss(delta);

        // Обновляем снаряды босса
        updateBossProjectiles(delta);

        // Обновляем снаряды игрока
        updatePlayerProjectiles(delta);
    }

    /**
     * Обрабатывает ввод пользователя
     */
    private void handleInput(float delta) {
        // Обработка клавиши Escape для паузы
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !gameOver) {
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
            return; // Выходим из метода, чтобы не обрабатывать другие клавиши
        }

        // Проверка касания кнопки паузы (обработка до других действий)
        if (!gameOver && Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            // Отладочная информация о позиции касания
            Gdx.app.debug("GameScreen", "Touch at X:" + touchPos.x + " Y:" + touchPos.y);
            Gdx.app.debug("GameScreen", "Pause button at X:" + pauseButtonRect.x + " Y:" + pauseButtonRect.y +
                         " Width:" + pauseButtonRect.width + " Height:" + pauseButtonRect.height);

            // Проверяем, было ли касание по кнопке паузы
            if (touchPos.x >= pauseButtonRect.x && touchPos.x <= pauseButtonRect.x + pauseButtonRect.width &&
                touchPos.y >= pauseButtonRect.y && touchPos.y <= pauseButtonRect.y + pauseButtonRect.height) {

                Gdx.app.log("GameScreen", "Pause button touched");

                if (isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
                return; // Выходим из метода, чтобы не обрабатывать другие касания
            }
        }

        // Проверка на стрельбу (только при битве с боссом)
        if (bossActive && !isPaused && !gameOver) {
            boolean shouldShoot = false;

            // Проверка удержания пальца на экране (непрерывная стрельба)
            if (Gdx.input.isTouched()) {
                // Получаем позицию касания
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPos);

                // Проверяем, что касание не на кнопке паузы
                if (!(touchPos.x >= pauseButtonRect.x && touchPos.x <= pauseButtonRect.x + pauseButtonRect.width &&
                    touchPos.y >= pauseButtonRect.y && touchPos.y <= pauseButtonRect.y + pauseButtonRect.height)) {
                    shouldShoot = true;
                }
            }

            // Проверка нажатия пробела (непрерывная стрельба)
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                shouldShoot = true;
            }

            // Если нужно стрелять и прошло достаточно времени с последнего выстрела
            if (shouldShoot && TimeUtils.nanoTime() - lastPlayerShootTime > PLAYER_SHOOT_COOLDOWN) {
                playerShoot();
            }
        }

        // Обработка сенсорного ввода для движения корабля
        if (Gdx.input.isTouched()) {
            // Если игра на паузе, не обрабатываем движения
            if (isPaused || gameOver) return;

            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            // Улучшенное управление: постепенное движение к позиции касания для плавности
            float targetX = touchPos.x - SHIP_SIZE / 2;
            float targetY = touchPos.y - SHIP_SIZE / 2;
            float moveStep = SHIP_SPEED * 1.5f * delta; // Увеличиваем скорость реакции

            // Двигаемся плавно к точке касания по X
            if (Math.abs(ship.x - targetX) <= moveStep) {
                ship.x = targetX; // Если уже близко, просто устанавливаем позицию
            } else if (ship.x < targetX) {
                ship.x += moveStep;
            } else {
                ship.x -= moveStep;
            }

            // Двигаемся плавно к точке касания по Y
            if (Math.abs(ship.y - targetY) <= moveStep) {
                ship.y = targetY; // Если уже близко, просто устанавливаем позицию
            } else if (ship.y < targetY) {
                ship.y += moveStep;
            } else {
                ship.y -= moveStep;
            }
        } else {
            // Если игра на паузе, не обрабатываем движения
            if (isPaused || gameOver) return;

            // Обработка клавиатуры - движение со скоростью, адаптированной для диагонального движения
            float moveX = 0;
            float moveY = 0;

            // Определяем направление движения по осям X и Y
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1;

            // Если движемся по диагонали, нормализуем скорость
            if (moveX != 0 && moveY != 0) {
                // Нормализация для диагонального движения (чтобы суммарная скорость не превышала SHIP_SPEED)
                float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
                moveX /= length;
                moveY /= length;
            }

            // Применяем множитель скорости, если активен соответствующий бонус
            float actualSpeed = speedBoostActive ? SHIP_SPEED * SPEED_BOOST_MULTIPLIER : SHIP_SPEED;

            // Перемещаем корабль
            ship.x += moveX * actualSpeed * delta;
            ship.y += moveY * actualSpeed * delta;
        }

        // Ограничения положения корабля по X
        if (ship.x < 0) ship.x = 0;
        if (ship.x > GAME_WIDTH - SHIP_SIZE) ship.x = GAME_WIDTH - SHIP_SIZE;

        // Ограничения положения корабля по Y
        if (ship.y < 0) ship.y = 0;
        if (ship.y > GAME_HEIGHT - SHIP_SIZE) ship.y = GAME_HEIGHT - SHIP_SIZE;
    }

    /**
     * Игрок выполняет выстрел
     */
    private void playerShoot() {
        // Создаем снаряд над кораблем игрока
        float projectileX = ship.x + ship.width/2 - PLAYER_PROJECTILE_SIZE/2;
        float projectileY = ship.y + ship.height;

        // Добавляем снаряд в коллекцию
        playerProjectiles.add(new PlayerProjectile(projectileX, projectileY));

        // Запоминаем время выстрела
        lastPlayerShootTime = TimeUtils.nanoTime();

        // Звук выстрела
        if (game.soundManager.isSfxEnabled()) {
            collectSound.play(0.3f);
        }
    }

    /**
     * Обновляет снаряды игрока
     */
    private void updatePlayerProjectiles(float delta) {
        // Если босс не активен, снаряды игрока не нужны
        if (!bossActive) {
            playerProjectiles.clear();
            return;
        }

        // Обрабатываем каждый снаряд
        for (Iterator<PlayerProjectile> iter = playerProjectiles.iterator(); iter.hasNext();) {
            PlayerProjectile projectile = iter.next();

            // Обновляем позицию снаряда
            projectile.update(delta);

            // Проверяем столкновение с боссом
            if (boss != null && projectile.bounds.overlaps(boss)) {
                // Наносим урон боссу
                damageBoss();

                // Удаляем снаряд при попадании
                iter.remove();

                // Добавляем очки за попадание
                addScore(50);

                // Воспроизводим звук попадания
                if (game.soundManager.isSfxEnabled()) {
                    explosionSound.play(0.3f);
                }

                continue;
            }

            // Удаляем снаряды за пределами экрана
            if (projectile.isOutOfScreen()) {
                iter.remove();
            }
        }
    }

    private void updateAsteroids(float delta, float difficulty) {
        for (Iterator<Rectangle> iter = asteroids.iterator(); iter.hasNext(); ) {
            Rectangle asteroid = iter.next();
            // Падение астероидов сверху вниз с учетом сложности
            // Увеличена базовая скорость астероидов и её зависимость от сложности
            asteroid.y -= (120 + 35 * difficulty) * delta; // Увеличено с 100+25 до 120+35

            // Удаление астероидов, вышедших за пределы экрана
            if (asteroid.y + ASTEROID_SIZE < 0) {
                iter.remove();
                score += 10; // Очки за уклонение от астероида
                // Используем специализированный метод для уклонения
                difficultySystem.registerDodge();
            }

            // Проверка столкновения астероида с боссом
            if (bossActive && asteroid.y > GAME_HEIGHT / 2) {
                if (asteroid.overlaps(boss)) {
                    // Наносим урон боссу
                    damageBoss();

                    // Удаляем астероид
                    iter.remove();

                    // Добавляем очки за попадание
                    score += 50;

                    // Регистрируем успех
                    difficultySystem.registerSuccess();

                    // Воспроизводим звук
                    if (game.soundManager.isSfxEnabled()) {
                        explosionSound.play(0.3f);
                    }

                    continue; // Переходим к следующему астероиду
                }
            }

            // Обработка столкновения с кораблем
            if (asteroid.overlaps(ship)) {
                handleAsteroidCollision(asteroid);
            }
        }
    }

    private void updateEnemies(float delta, float difficulty) {
        for (Iterator<Rectangle> iter = enemies.iterator(); iter.hasNext(); ) {
            Rectangle enemy = iter.next();
            // Движение врагов сверху вниз с учетом сложности
            // Увеличена базовая скорость врагов и её зависимость от сложности
            enemy.y -= (150 + 45 * difficulty) * delta; // Увеличено с 120+35 до 150+45

            // Движение врагов влево-вправо, следуя за кораблем (более разумное преследование)
            // Увеличена скорость преследования
            if (enemy.x < ship.x) enemy.x += (50 + 8 * difficulty) * delta; // Увеличено с 40+6 до 50+8
            if (enemy.x > ship.x) enemy.x -= (50 + 8 * difficulty) * delta; // Увеличено с 40+6 до 50+8

            // Удаление врагов, вышедших за пределы экрана
            if (enemy.y + ENEMY_SIZE < 0) {
                iter.remove();
                score += 30; // Больше очков за уклонение от врага
                // Используем специализированный метод для уклонения
                difficultySystem.registerDodge();
            }

            // Обработка столкновения с кораблем
            if (enemy.overlaps(ship)) {
                if (game.soundManager.isSfxEnabled()) {
                    explosionSound.play();
                }
                iter.remove();
                loseLife();
                difficultySystem.registerFailure(); // Регистрируем неудачу
            }
        }
    }

    private void updateFuelCanisters(float delta) {
        for (Iterator<Rectangle> iter = fuelCanisters.iterator(); iter.hasNext(); ) {
            Rectangle fuelCanister = iter.next();
            // Падение канистр с топливом сверху вниз
            fuelCanister.y -= 120 * delta;

            // Удаление канистр, вышедших за пределы экрана
            if (fuelCanister.y + FUEL_SIZE < 0) {
                iter.remove();
            }

            // Обработка сбора канистры с топливом
            if (fuelCanister.overlaps(ship)) {
                handleFuelCollection(fuelCanister);
            }
        }
    }

    /**
     * Обновляет движение и сбор сердечек
     */
    private void updateHearts(float delta) {
        for (Iterator<Rectangle> iter = hearts.iterator(); iter.hasNext(); ) {
            Rectangle heart = iter.next();
            // Падение сердечек сверху вниз
            heart.y -= 150 * delta; // Немного быстрее топлива

            // Удаление сердечек, вышедших за пределы экрана
            if (heart.y + HEART_SIZE < 0) {
                iter.remove();
            }

            // Обработка сбора сердечка
            if (heart.overlaps(ship)) {
                // Воспроизводим звук сбора если звуковые эффекты включены
                if (game.soundManager.isSfxEnabled()) {
                    collectSound.play();
                }

                iter.remove();
                if (lives < 3) { // Проверка, что не превышаем максимум жизней
                    lives++;
                    if (lives >= 3) {
                        needHeart = false; // Жизни восстановлены до максимума
                    }
                }
                score += 50; // Бонусные очки за сбор сердечка
            }
        }
    }

    private void loseLife() {
        lives--;
        needHeart = true; // Активируем появление сердечек, так как игрок потерял жизнь

        // Отмечаем, что игрок получил урон (для достижения "Неуязвимый")
        damageTaken = true;

        // Воспроизводим звук взрыва если звуковые эффекты включены
        if (game.soundManager.isSfxEnabled()) {
            explosionSound.play();
        }

        if (lives <= 0) {
            gameOver = true;
            gameMusic.stop();

            // Очищаем снаряды босса и игрока при проигрыше
            bossProjectiles.clear();
            playerProjectiles.clear();

            // Пересоздаем UI экрана проигрыша для гарантии его отображения
            if (gameOverStage != null) {
                gameOverStage.dispose();
            }

            // Пересоздаем сцену для гарантии корректной отрисовки
            gameOverStage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));
            createGameOverUI();

            // Обновляем текст с финальным счетом
            if (scoreLabel != null) {
                scoreLabel.setText("SCORE: " + score);
            }

            // При проигрыше устанавливаем обработчик ввода на gameOverStage
            Gdx.input.setInputProcessor(gameOverStage);

            // Устанавливаем флаг для принудительной отрисовки экрана проигрыша
            forceGameOverRender = true;
        }
    }

    @Override
    public void resize(int width, int height) {
        // Обеспечиваем корректное масштабирование для всех типов экранов
        if (gameOverStage != null) {
            gameOverStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void show() {
        // Вызывается при показе экрана
        gameMusic.play();
        // При старте игры устанавливаем ввод на игровой процесс (не на UI)
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void hide() {
        // Вызывается, когда экран перестает быть видимым
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        // Пауза игры (для мобильных устройств)
    }

    @Override
    public void resume() {
        // Возобновление игры после паузы
    }

    @Override
    public void dispose() {
        // Освобождаем ресурсы
        shipImage.dispose();
        asteroidImage.dispose();
        enemyImage.dispose();
        bossTexture.dispose();
        bossProjectileTexture.dispose();
        playerProjectileTexture.dispose();
        fuelImage.dispose();
        heartImage.dispose();
        backgroundImage.dispose();
        pauseButtonTexture.dispose();
        collectSound.dispose();
        explosionSound.dispose();
        gameMusic.dispose();

        // Освобождаем ресурсы UI
        if (gameOverStage != null) gameOverStage.dispose();
        if (gameOverSkin != null) gameOverSkin.dispose();
        if (pauseStage != null) pauseStage.dispose();
        if (pauseSkin != null) pauseSkin.dispose();
        // Не освобождаем font, т.к. это делает FontManager

        // Освобождаем текстуры бонусов
        shieldTexture.dispose();
        speedBoostTexture.dispose();
        magnetTexture.dispose();
        doubleScoreTexture.dispose();
    }

    /**
     * Создаем пользовательский интерфейс для экрана проигрыша
     */
    private void createGameOverUI() {
        // Создаем скин для кнопок
        gameOverSkin = new Skin();

        // Используем более крупный шрифт из FontManager
        BitmapFont titleFont = game.fontManager.getTitleFont();
        BitmapFont gameFont = game.fontManager.getGameFont();

        // Все шрифты устанавливаем в белый цвет
        titleFont.setColor(Color.WHITE);
        gameFont.setColor(Color.WHITE);

        gameOverSkin.add("title-font", titleFont);
        gameOverSkin.add("game-font", gameFont);

        gameOverSkin.add("white", new Color(1, 1, 1, 1));
        gameOverSkin.add("gray", new Color(0.5f, 0.5f, 0.5f, 1));
        gameOverSkin.add("blue", new Color(0.2f, 0.4f, 0.8f, 1));
        gameOverSkin.add("black", new Color(0, 0, 0, 1));
        gameOverSkin.add("transparent", new Color(0, 0, 0, 0.7f));

        // Добавляем белый пиксель для фона кнопок и панелей
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        gameOverSkin.add("white-pixel", new Texture(pixmap));
        pixmap.dispose();

        // Стиль для кнопок - как в главном меню (синие)
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = gameOverSkin.getFont("game-font");
        // Гарантируем, что цвет текста остается белым для всех состояний кнопки
        buttonStyle.fontColor = new Color(1, 1, 1, 1);
        buttonStyle.downFontColor = new Color(0.9f, 0.9f, 0.9f, 1);
        buttonStyle.overFontColor = new Color(0.8f, 0.8f, 1, 1);
        buttonStyle.disabledFontColor = gameOverSkin.getColor("gray");

        // Добавляем фоны для кнопок - как в главном меню
        buttonStyle.up = gameOverSkin.newDrawable("white-pixel", new Color(0.2f, 0.3f, 0.5f, 0.8f));
        buttonStyle.down = gameOverSkin.newDrawable("white-pixel", new Color(0.1f, 0.2f, 0.4f, 0.9f));
        buttonStyle.over = gameOverSkin.newDrawable("white-pixel", new Color(0.3f, 0.4f, 0.6f, 0.8f));

        gameOverSkin.add("default", buttonStyle);

        // Стиль для заголовка и счета без фона
        TextButtonStyle titleStyle = new TextButtonStyle();
        titleStyle.font = gameOverSkin.getFont("title-font");
        titleStyle.fontColor = gameOverSkin.getColor("white");
        // Убираем фон для заголовка и счета

        gameOverSkin.add("title", titleStyle);

        // Создаем таблицу для размещения UI элементов
        Table gameOverTable = new Table();
        gameOverTable.setFillParent(true);
        gameOverTable.center();

        // Добавляем полупрозрачный фон для всего экрана проигрыша
        Table background = new Table();
        background.setFillParent(true);
        background.setBackground(gameOverSkin.newDrawable("white-pixel", new Color(0, 0, 0, 0.6f)));
        gameOverStage.addActor(background);

        // Создаем кнопки с крупным понятным текстом
        TextButton gameOverTitle = new TextButton("GAME OVER", titleStyle);
        gameOverTitle.getLabel().setFontScale(1.5f); // Увеличиваем размер текста заголовка
        gameOverTitle.setDisabled(true);

        // Создаем кнопки и настраиваем размер шрифта
        restartButton = new TextButton("RESTART", buttonStyle);
        restartButton.getLabel().setFontScale(1.3f); // Увеличиваем размер текста

        menuButton = new TextButton("MAIN MENU", buttonStyle);
        menuButton.getLabel().setFontScale(1.3f); // Увеличиваем размер текста

        // Добавляем заголовок
        gameOverTable.add(gameOverTitle).padBottom(60).row();

        // Отображаем финальный счет (будет обновляться при проигрыше)
        scoreLabel = new TextButton("SCORE: 0", titleStyle);
        scoreLabel.setDisabled(true);
        gameOverTable.add(scoreLabel).padBottom(50).row();

        // Настраиваем размеры кнопок и добавляем их в таблицу как в MainMenuScreen
        gameOverTable.add(restartButton).width(450).height(120).pad(20).row();
        gameOverTable.add(menuButton).width(450).height(120).pad(20).row();

        // Добавляем обработчики событий на кнопки
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                initGame();
                gameMusic.play();
                Gdx.input.setInputProcessor(null);
            }
        });

        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Сначала останавливаем все звуки и музыку
                if (gameMusic.isPlaying()) {
                    gameMusic.stop();
                }

                // Принудительно освобождаем все ресурсы UI
                if (gameOverStage != null) {
                    gameOverStage.dispose();
                    gameOverStage = null;
                }

                if (gameOverSkin != null) {
                    gameOverSkin.dispose();
                    gameOverSkin = null;
                }

                // Пересоздаем менеджер шрифтов полностью
                game.recreateFontManager();

                // Сначала освобождаем ресурсы текущего экрана
                dispose();

                // Затем переходим в главное меню
                game.setScreen(new MainMenuScreen(game));
            }
        });

        // Добавляем таблицу на сцену
        gameOverStage.addActor(gameOverTable);
    }

    // Генерация бонуса при повышении уровня
    private void spawnPowerupOnLevelUp() {
        // Выбираем случайный тип бонуса
        PowerupType[] types = PowerupType.values();
        PowerupType type = types[MathUtils.random(types.length - 1)];

        // Координаты для размещения
        float x = 0;
        float y = GAME_HEIGHT;

        // Максимальное количество попыток найти свободное место
        int maxAttempts = 15;
        boolean hasOverlap = true;

        // Пытаемся найти место без пересечений
        for (int attempt = 0; attempt < maxAttempts && hasOverlap; attempt++) {
            // Задаем случайную позицию X
            x = MathUtils.random(0, GAME_WIDTH - POWERUP_SIZE);
            y = GAME_HEIGHT; // Сверху экрана

            hasOverlap = false;

            // Проверка пересечения с астероидами
            for (Rectangle asteroid : asteroids) {
                // Допускаем небольшое наложение
                float minDistance = (POWERUP_SIZE + ASTEROID_SIZE) * 0.4f;

                // Проверяем расстояние между центрами объектов
                float dx = (asteroid.x + asteroid.width/2) - (x + POWERUP_SIZE/2);
                float dy = (asteroid.y + asteroid.height/2) - (y + POWERUP_SIZE/2);
                float distance = (float)Math.sqrt(dx*dx + dy*dy);

                if (distance < minDistance) {
                    hasOverlap = true;
                    break;
                }
            }

            // Проверка пересечения с врагами
            if (!hasOverlap) {
                for (Rectangle enemy : enemies) {
                    float minDistance = (POWERUP_SIZE + ENEMY_SIZE) * 0.4f;
                    float dx = (enemy.x + enemy.width/2) - (x + POWERUP_SIZE/2);
                    float dy = (enemy.y + enemy.height/2) - (y + POWERUP_SIZE/2);
                    float distance = (float)Math.sqrt(dx*dx + dy*dy);

                    if (distance < minDistance) {
                        hasOverlap = true;
                        break;
                    }
                }
            }

            // На последних попытках разрешаем размещать с минимальными проверками
            if (attempt > maxAttempts * 0.7f && hasOverlap) {
                hasOverlap = false;
            }
        }

        // Создаем бонус в найденном месте
        powerups.add(new Powerup(x, y, type));
    }

    // Обновление бонусов
    private void updatePowerups(float delta) {
        // Обновляем активные бонусы
        if (shieldActive || speedBoostActive || magnetActive || doubleScoreActive) {
            for (Powerup powerup : powerups) {
                if (powerup.active) {
                    powerup.activeTime += delta;
                    if (powerup.activeTime >= powerup.activeDuration) {
                        // Деактивируем бонус по окончании времени действия
                        deactivatePowerup(powerup.type);
                        powerup.active = false;
                    }
                }
            }
        }

        // Обновляем движение бонусов
        for (Iterator<Powerup> iter = powerups.iterator(); iter.hasNext();) {
            Powerup powerup = iter.next();

            if (!powerup.active) {
                // Движение бонуса сверху вниз
                powerup.bounds.y -= 200 * delta;

                // Проверка на сбор бонуса
                if (powerup.bounds.overlaps(ship)) {
                    // Активируем бонус
                    activatePowerup(powerup.type);
                    powerup.active = true;
                    powerup.activeTime = 0;

                    // Воспроизводим звук сбора (используем звук сбора топлива)
                    collectSound.play();
                }

                // Удаляем бонус, если он ушел за экран
                if (powerup.bounds.y + powerup.bounds.height < 0) {
                    iter.remove();
                }
            }
        }
    }

    // Активация бонуса
    private void activatePowerup(PowerupType type) {
        switch (type) {
            case SHIELD:
                shieldActive = true;
                break;
            case SPEED_BOOST:
                speedBoostActive = true;
                break;
            case MAGNET:
                magnetActive = true;
                break;
            case DOUBLE_SCORE:
                doubleScoreActive = true;
                break;
        }
    }

    // Деактивация бонуса
    private void deactivatePowerup(PowerupType type) {
        switch (type) {
            case SHIELD:
                shieldActive = false;
                break;
            case SPEED_BOOST:
                speedBoostActive = false;
                break;
            case MAGNET:
                magnetActive = false;
                break;
            case DOUBLE_SCORE:
                doubleScoreActive = false;
                break;
        }
    }

    // Обработка столкновений с астероидами
    private void handleAsteroidCollision(Rectangle asteroid) {
        // Если активен щит, то не теряем жизнь при столкновении
        if (!shieldActive) {
            loseLife();
        }

        // Удаляем астероид в любом случае
        asteroids.removeValue(asteroid, true);

        // Воспроизводим звук взрыва если звуковые эффекты включены
        if (game.soundManager.isSfxEnabled()) {
            explosionSound.play();
        }
    }

    // Обработка сбора топлива
    private void handleFuelCollection(Rectangle fuelCanister) {
        // Добавляем топливо
        fuel = Math.min(fuel + 25, MAX_FUEL);

        // Увеличиваем счет (снижено до 25 очков)
        addScore(25);

        // Удаляем канистру
        fuelCanisters.removeValue(fuelCanister, true);

        // Сообщаем системе сложности об успехе - используем новый метод
        difficultySystem.registerFuelCollection();

        // Воспроизводим звук сбора если звуковые эффекты включены
        if (game.soundManager.isSfxEnabled()) {
            collectSound.play();
        }

        // Увеличиваем счетчик собранного топлива для достижения
        fuelCollected++;

        // Проверяем достижение "Собрать 50 канистр с топливом"
        if (game.achievementSystem.updateProgress(AchievementSystem.ACHIEVEMENT_COLLECT_50_FUEL, fuelCollected) && fuelCollected >= 50) {
            showAchievementNotification("Достижение разблокировано: Заправщик");
        }
    }

    // Добавление очков с учетом бонуса двойных очков
    private void addScore(int baseScore) {
        if (doubleScoreActive) {
            score += baseScore * 2;
        } else {
            score += baseScore;
        }
    }

    // Магнитное притяжение предметов к кораблю
    private void applyMagneticEffect(float delta) {
        if (magnetActive) {
            // Притягиваем топливо
            for (Rectangle fuel : fuelCanisters) {
                // Вычисляем направление к кораблю
                float dx = ship.x + ship.width/2 - (fuel.x + fuel.width/2);
                float dy = ship.y + ship.height/2 - (fuel.y + fuel.height/2);

                // Нормализуем вектор
                float length = (float)Math.sqrt(dx*dx + dy*dy);
                if (length > 0) {
                    dx /= length;
                    dy /= length;
                }

                // Применяем магнитное притяжение
                float magnetSpeed = 300; // скорость притяжения
                fuel.x += dx * magnetSpeed * delta;
                fuel.y += dy * magnetSpeed * delta;
            }

            // Притягиваем сердечки
            for (Rectangle heart : hearts) {
                float dx = ship.x + ship.width/2 - (heart.x + heart.width/2);
                float dy = ship.y + ship.height/2 - (heart.y + heart.height/2);

                float length = (float)Math.sqrt(dx*dx + dy*dy);
                if (length > 0) {
                    dx /= length;
                    dy /= length;
                }

                float magnetSpeed = 300;
                heart.x += dx * magnetSpeed * delta;
                heart.y += dy * magnetSpeed * delta;
            }
        }
    }

    // Получение текстуры для типа бонуса
    private Texture getPowerupTexture(PowerupType type) {
        switch (type) {
            case SHIELD: return shieldTexture;
            case SPEED_BOOST: return speedBoostTexture;
            case MAGNET: return magnetTexture;
            case DOUBLE_SCORE: return doubleScoreTexture;
            default: return shieldTexture; // По умолчанию
        }
    }

    // Отображает уведомление о достижении
    private void showAchievementNotification(String text) {
        achievementNotificationActive = true;
        achievementNotificationTime = 0;
        achievementNotificationText = text;
    }

    /**
     * Отрисовка уведомления о достижении
     */
    private void drawAchievementNotification(float delta) {
        // Обновляем таймер уведомления
        achievementNotificationTime += delta;

        // Проверяем, завершилось ли отображение уведомления
        if (achievementNotificationTime >= ACHIEVEMENT_NOTIFICATION_DURATION) {
            achievementNotificationActive = false;
            achievementNotificationTime = 0;
            return;
        }

        // Расчет прозрачности текста (сначала появляется, потом исчезает)
        float alpha;
        if (achievementNotificationTime < ACHIEVEMENT_NOTIFICATION_DURATION / 4) {
            alpha = achievementNotificationTime / (ACHIEVEMENT_NOTIFICATION_DURATION / 4);
        } else if (achievementNotificationTime > ACHIEVEMENT_NOTIFICATION_DURATION * 3 / 4) {
            alpha = 1.0f - (achievementNotificationTime - ACHIEVEMENT_NOTIFICATION_DURATION * 3 / 4) / (ACHIEVEMENT_NOTIFICATION_DURATION / 4);
        } else {
            alpha = 1.0f;
        }

        // Вычисляем размеры оповещения относительно экрана и добавляем отступы
        float notificationHeight = GAME_HEIGHT * 0.1f; // 10% от высоты экрана (уменьшено с 12%)
        float notificationWidth = GAME_WIDTH * 0.8f; // 80% от ширины экрана
        float notificationY = GAME_HEIGHT - notificationHeight - GAME_HEIGHT * 0.05f; // 5% отступ сверху (увеличен с 2%)
        float notificationX = (GAME_WIDTH - notificationWidth) / 2; // центрирование по горизонтали

        // Отрисовка черного полупрозрачного фона
        game.batch.setColor(0, 0, 0, 0.7f * alpha);
        game.batch.draw(backgroundImage, notificationX, notificationY, notificationWidth, notificationHeight);

        // Получаем шрифт для уведомления
        BitmapFont notificationFont = game.fontManager.getUIFont();
        notificationFont.setColor(1, 1, 0.3f, alpha); // Желтоватый цвет для достижений

        // Масштабируем шрифт относительно размера экрана (увеличенный)
        float fontScale = GAME_WIDTH / 1200f; // Увеличенный масштаб шрифта (было 1500f)
        notificationFont.getData().setScale(fontScale);

        // Измеряем ширину текста для центрирования
        GlyphLayout layout = new GlyphLayout(notificationFont, achievementNotificationText);
        float x = notificationX + (notificationWidth - layout.width) / 2;
        float y = notificationY + (notificationHeight + layout.height) / 2;

        // Отрисовка текста уведомления
        notificationFont.draw(game.batch, achievementNotificationText, x, y);

        // Сбрасываем цвет и масштаб шрифта
        notificationFont.setColor(1, 1, 1, 1);
        notificationFont.getData().setScale(1f);
        game.batch.setColor(1, 1, 1, 1);
    }

    /**
     * Создает пользовательский интерфейс для экрана паузы
     */
    private void createPauseUI() {
        // Создаем скин для кнопок
        pauseSkin = new Skin();

        // Используем шрифты из FontManager
        BitmapFont titleFont = game.fontManager.getTitleFont();
        BitmapFont gameFont = game.fontManager.getGameFont();

        // Все шрифты устанавливаем в белый цвет
        titleFont.setColor(Color.WHITE);
        gameFont.setColor(Color.WHITE);

        pauseSkin.add("title-font", titleFont);
        pauseSkin.add("game-font", gameFont);

        pauseSkin.add("white", new Color(1, 1, 1, 1));
        pauseSkin.add("gray", new Color(0.5f, 0.5f, 0.5f, 1));
        pauseSkin.add("blue", new Color(0.2f, 0.4f, 0.8f, 1));
        pauseSkin.add("black", new Color(0, 0, 0, 1));
        pauseSkin.add("transparent", new Color(0, 0, 0, 0.7f));

        // Добавляем белый пиксель для фона кнопок и панелей
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pauseSkin.add("white-pixel", new Texture(pixmap));
        pixmap.dispose();

        // Стиль для кнопок
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = pauseSkin.getFont("game-font");
        // Гарантируем, что цвет текста остается белым для всех состояний кнопки
        buttonStyle.fontColor = new Color(1, 1, 1, 1);
        buttonStyle.downFontColor = new Color(0.9f, 0.9f, 0.9f, 1);
        buttonStyle.overFontColor = new Color(0.8f, 0.8f, 1, 1);
        buttonStyle.disabledFontColor = pauseSkin.getColor("gray");

        // Добавляем фоны для кнопок - как в главном меню
        buttonStyle.up = pauseSkin.newDrawable("white-pixel", new Color(0.2f, 0.3f, 0.5f, 0.8f));
        buttonStyle.down = pauseSkin.newDrawable("white-pixel", new Color(0.1f, 0.2f, 0.4f, 0.9f));
        buttonStyle.over = pauseSkin.newDrawable("white-pixel", new Color(0.3f, 0.4f, 0.6f, 0.8f));

        pauseSkin.add("default", buttonStyle);

        // Стиль для заголовка
        TextButtonStyle titleStyle = new TextButtonStyle();
        titleStyle.font = pauseSkin.getFont("title-font");
        titleStyle.fontColor = pauseSkin.getColor("white");

        pauseSkin.add("title", titleStyle);

        // Создаем таблицу для размещения UI элементов
        Table pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.center();

        // Добавляем полупрозрачный фон для всего экрана паузы
        Table background = new Table();
        background.setFillParent(true);
        background.setBackground(pauseSkin.newDrawable("white-pixel", new Color(0, 0, 0, 0.6f)));
        pauseStage.addActor(background);

        // Создаем заголовок паузы
        TextButton pauseTitle = new TextButton("ПАУЗА", titleStyle);
        pauseTitle.getLabel().setFontScale(1.5f); // Увеличиваем размер текста заголовка
        pauseTitle.setDisabled(true);

        // Создаем кнопки и настраиваем размер шрифта
        continueButton = new TextButton("ПРОДОЛЖИТЬ", buttonStyle);
        continueButton.getLabel().setFontScale(1.3f); // Увеличиваем размер текста

        pauseMenuButton = new TextButton("ГЛАВНОЕ МЕНЮ", buttonStyle);
        pauseMenuButton.getLabel().setFontScale(1.3f); // Увеличиваем размер текста

        // Добавляем заголовок
        pauseTable.add(pauseTitle).padBottom(60).row();

        // Настраиваем размеры кнопок и добавляем их в таблицу
        pauseTable.add(continueButton).width(450).height(120).pad(20).row();
        pauseTable.add(pauseMenuButton).width(450).height(120).pad(20).row();

        // Добавляем обработчики событий на кнопки
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Возобновляем игру, используя метод resumeGame()
                resumeGame();
            }
        });

        pauseMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Сначала останавливаем все звуки и музыку
                if (gameMusic.isPlaying()) {
                    gameMusic.stop();
                }

                // Освобождаем ресурсы UI
                if (pauseStage != null) {
                    pauseStage.dispose();
                    pauseStage = null;
                }

                if (pauseSkin != null) {
                    pauseSkin.dispose();
                    pauseSkin = null;
                }

                // Пересоздаем менеджер шрифтов
                game.recreateFontManager();

                // Освобождаем ресурсы текущего экрана
                dispose();

                // Переходим в главное меню
                game.setScreen(new MainMenuScreen(game));
            }
        });

        // Добавляем таблицу на сцену
        pauseStage.addActor(pauseTable);
    }

    /**
     * Ставит игру на паузу
     */
    private void pauseGame() {
        if (isPaused) return;

        isPaused = true;

        // Отладочное сообщение
        Gdx.app.log("GameScreen", "Pausing game");

        // Останавливаем музыку
        game.soundManager.pauseMusic();

        // Убеждаемся, что pauseStage инициализирован
        if (pauseStage == null) {
            createPauseUI();
        }

        // Устанавливаем обработчик ввода на экран паузы
        Gdx.input.setInputProcessor(pauseStage);
    }

    /**
     * Возобновляет игру после паузы
     */
    private void resumeGame() {
        if (!isPaused) return;

        // Отладочное сообщение
        Gdx.app.log("GameScreen", "Resuming game");

        isPaused = false;

        // Возвращаем обработку ввода к игре
        Gdx.input.setInputProcessor(null);

        // Возобновляем музыку
        game.soundManager.resumeMusic();
    }

    /**
     * Отрисовывает все игровые объекты (корабль, астероиды, враги и т.д.)
     */
    private void drawGameObjects() {
        // Корабль игрока
        game.batch.draw(shipImage, ship.x, ship.y, ship.width, ship.height);

        // Астероиды
        for (Rectangle asteroid : asteroids) {
            game.batch.draw(asteroidImage, asteroid.x, asteroid.y, asteroid.width, asteroid.height);
        }

        // Враги
        for (Rectangle enemy : enemies) {
            game.batch.draw(enemyImage, enemy.x, enemy.y, enemy.width, enemy.height);
        }

        // Канистры с топливом
        for (Rectangle fuelCanister : fuelCanisters) {
            game.batch.draw(fuelImage, fuelCanister.x, fuelCanister.y, fuelCanister.width, fuelCanister.height);
        }

        // Сердечки
        for (Rectangle heart : hearts) {
            game.batch.draw(heartImage, heart.x, heart.y, heart.width, heart.height);
        }

        // Отрисовка бонусов
        for (Powerup powerup : powerups) {
            if (!powerup.active) {
                // Отрисовываем только неактивированные бонусы
                Texture powerupTexture = getPowerupTexture(powerup.type);
                game.batch.draw(powerupTexture, powerup.bounds.x, powerup.bounds.y,
                               powerup.bounds.width, powerup.bounds.height);
            }
        }

        // Отрисовка снарядов босса
        for (BossProjectile projectile : bossProjectiles) {
            game.batch.draw(bossProjectileTexture, projectile.bounds.x, projectile.bounds.y,
                           projectile.bounds.width, projectile.bounds.height);
        }

        // Отрисовка снарядов игрока
        for (PlayerProjectile projectile : playerProjectiles) {
            game.batch.draw(playerProjectileTexture, projectile.bounds.x, projectile.bounds.y,
                           projectile.bounds.width, projectile.bounds.height);
        }

        // Отрисовка эффекта щита, если он активен
        if (shieldActive) {
            // Увеличиваем размер щита относительно корабля
            float shieldSize = SHIP_SIZE * 1.5f;
            game.batch.setColor(0.4f, 0.8f, 1.0f, 0.6f);
            game.batch.draw(shieldTexture,
                            ship.x - (shieldSize - ship.width) / 2,
                            ship.y - (shieldSize - ship.height) / 2,
                            shieldSize, shieldSize);
            game.batch.setColor(1, 1, 1, 1);
        }

        // Отрисовка босса и его полоски здоровья
        drawBoss();
    }

    /**
     * Проверяет состояние аудио настроек и применяет их
     */
    private void checkAudioSettings() {
        // Инициализация Stage для UI (только для экрана окончания игры)
        gameOverStage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));
    }

    /**
     * Проверяет необходимость активации босса на основе текущего уровня
     */
    private void checkForBossLevel() {
        int level = difficultySystem.getCurrentLevel();

        // При переходе на новый уровень сбрасываем флаг победы над боссом предыдущего уровня
        if (difficultySystem.hasLevelChanged() && level % 5 != 0) {
            bossDefeated = false;
        }

        // Активируем босса каждый 5-й уровень (5, 10, 15, ...)
        if (level % 5 == 0 && level > 0 && !bossActive && !bossDefeated) {
            spawnBoss();
        }
    }

    /**
     * Создает босса
     */
    private void spawnBoss() {
        // Очищаем экран от других объектов
        asteroids.clear();
        enemies.clear();
        bossProjectiles.clear();
        playerProjectiles.clear();

        // Создаем босса в верхней части экрана
        boss = new Rectangle();
        boss.width = BOSS_SIZE;
        boss.height = BOSS_SIZE;
        boss.x = GAME_WIDTH / 2 - BOSS_SIZE / 2;
        boss.y = GAME_HEIGHT - BOSS_SIZE - 50;

        // Устанавливаем параметры
        bossActive = true;
        bossHealth = BOSS_HEALTH_MAX;
        bossMoveDirection = 1f;
        bossDefeated = false;
        bossInvulnerabilityTimer = 0;

        // Сохраняем время для атак босса
        lastBossAttackTime = TimeUtils.nanoTime();

        // Воспроизводим звук появления (используем существующий звук)
        if (game.soundManager.isSfxEnabled()) {
            explosionSound.play(0.7f);
        }
    }

    /**
     * Обновляет состояние босса
     */
    private void updateBoss(float delta) {
        // Если босс не активен, выходим
        if (!bossActive) return;

        // Обновляем таймер неуязвимости
        if (bossInvulnerabilityTimer > 0) {
            bossInvulnerabilityTimer -= delta;
        }

        // Движение босса из стороны в сторону
        boss.x += bossMoveDirection * BOSS_SPEED * delta;

        // Изменение направления движения при достижении краев экрана
        if (boss.x <= 0) {
            boss.x = 0;
            bossMoveDirection = 1;
        } else if (boss.x >= GAME_WIDTH - BOSS_SIZE) {
            boss.x = GAME_WIDTH - BOSS_SIZE;
            bossMoveDirection = -1;
        }

        // Атака босса - создание астероидов и стрельба
        if (TimeUtils.nanoTime() - lastBossAttackTime > 2000000000L) { // Каждые 2 секунды
            // Создаем атаку астероидами
            bossFire();

            // Обновляем время последней атаки
            lastBossAttackTime = TimeUtils.nanoTime();
        }

        // Стрельба снарядами (отдельно от сброса астероидов)
        if (TimeUtils.nanoTime() - lastBossAttackTime > BOSS_SHOOT_INTERVAL) {
            // Стреляем в игрока
            bossShoot();
        }

        // Проверка столкновения корабля с боссом
        if (boss.overlaps(ship)) {
            loseLife();
            // Отталкиваем корабль
            ship.y -= 100;
            if (ship.y < 0) ship.y = 0;
        }

        // Если босс побежден
        if (bossHealth <= 0 && !bossDefeated) {
            bossDefeated = true;
            bossActive = false;

            // Очищаем все снаряды босса
            bossProjectiles.clear();
            playerProjectiles.clear();

            // Добавляем очки за победу
            score += 2000;

            // Показываем анимацию
            showLevelUpAnimation = true;
            levelUpAnimationTime = 0;
            levelUpMessage = "BOSS DEFEATED!";

            // Создаем бонусы в награду
            spawnRewardsAfterBoss();

            // Воспроизводим звук
            if (game.soundManager.isSfxEnabled()) {
                explosionSound.play(1.0f);
            }
        }
    }

    /**
     * Обрабатывает атаку по боссу
     */
    private void damageBoss() {
        // Если босс неуязвим, выходим
        if (bossInvulnerabilityTimer > 0) return;

        // Наносим урон
        bossHealth--;

        // Устанавливаем период неуязвимости
        bossInvulnerabilityTimer = 0.3f;

        // Звук получения урона
        if (game.soundManager.isSfxEnabled()) {
            collectSound.play(0.5f);
        }
    }

    /**
     * Создает атаку босса
     */
    private void bossFire() {
        // Создаем астероид под боссом
        Rectangle asteroid = new Rectangle();
        asteroid.width = ASTEROID_SIZE;
        asteroid.height = ASTEROID_SIZE;
        asteroid.x = boss.x + (boss.width - ASTEROID_SIZE) / 2;
        asteroid.y = boss.y - ASTEROID_SIZE;

        // Добавляем астероид
        asteroids.add(asteroid);

        // Звук атаки
        if (game.soundManager.isSfxEnabled()) {
            explosionSound.play(0.3f);
        }
    }

    /**
     * Создает награды после победы над боссом
     */
    private void spawnRewardsAfterBoss() {
        // Создаем несколько канистр с топливом
        for (int i = 0; i < 3; i++) {
            Rectangle fuel = new Rectangle();
            fuel.width = FUEL_SIZE;
            fuel.height = FUEL_SIZE;
            fuel.x = MathUtils.random(0, GAME_WIDTH - FUEL_SIZE);
            fuel.y = GAME_HEIGHT - i * 100;
            fuelCanisters.add(fuel);
        }

        // Если у игрока не максимум жизней, создаем сердечко
        if (lives < MAX_LIVES) {
            Rectangle heart = new Rectangle();
            heart.width = HEART_SIZE;
            heart.height = HEART_SIZE;
            heart.x = GAME_WIDTH / 2 - HEART_SIZE / 2;
            heart.y = GAME_HEIGHT;
            hearts.add(heart);
        }

        // Создаем два случайных бонуса
        for (int i = 0; i < 2; i++) {
            PowerupType[] types = PowerupType.values();
            PowerupType type = types[MathUtils.random(types.length - 1)];
            float x = MathUtils.random(0, GAME_WIDTH - POWERUP_SIZE);
            float y = GAME_HEIGHT - i * 120;
            powerups.add(new Powerup(x, y, type));
        }
    }

    /**
     * Отрисовывает босса и его полосу здоровья
     */
    private void drawBoss() {
        if (!bossActive) return;

        // Определяем альфа для эффекта мигания при получении урона
        float alpha = 1.0f;
        if (bossInvulnerabilityTimer > 0) {
            alpha = bossInvulnerabilityTimer % 0.2f > 0.1f ? 0.5f : 1.0f;
        }

        // Отрисовка босса с альфа
        game.batch.setColor(1, 1, 1, alpha); // Обычный цвет для босса (не красный)
        game.batch.draw(bossTexture, boss.x, boss.y, boss.width, boss.height);
        game.batch.setColor(1, 1, 1, 1);

        // Вычисляем процент здоровья босса
        float healthPercentage = (float) bossHealth / BOSS_HEALTH_MAX;
        int healthPercent = (int)(healthPercentage * 100);

        // Формируем текст для отображения
        String healthText = "BOSS: " + healthPercent + "%";

        // Выбираем цвет в зависимости от оставшегося здоровья
        if (healthPercent > 60) {
            font.setColor(0.2f, 0.8f, 0.2f, 1.0f); // Зеленый для высокого здоровья
        } else if (healthPercent > 30) {
            font.setColor(0.8f, 0.8f, 0.2f, 1.0f); // Желтый для среднего здоровья
        } else {
            font.setColor(0.8f, 0.2f, 0.2f, 1.0f); // Красный для низкого здоровья
        }

        // Измеряем ширину текста для центрирования
        GlyphLayout layout = new GlyphLayout(font, healthText);
        float textX = GAME_WIDTH / 2 - layout.width / 2;

        // Отрисовываем текст здоровья босса
        font.draw(game.batch, healthText, textX, GAME_HEIGHT - 20);

        // Возвращаем цвет шрифта к белому
        font.setColor(1, 1, 1, 1);
    }

    /**
     * Обновляет снаряды босса
     */
    private void updateBossProjectiles(float delta) {
        // Обрабатываем каждый снаряд
        for (Iterator<BossProjectile> iter = bossProjectiles.iterator(); iter.hasNext();) {
            BossProjectile projectile = iter.next();

            // Обновляем позицию снаряда
            projectile.update(delta);

            // Проверяем столкновение с кораблем
            if (projectile.bounds.overlaps(ship)) {
                // Если нет щита, наносим урон
                if (!shieldActive) {
                    loseLife();
                    difficultySystem.registerFailure(); // Регистрируем неудачу
                }

                // Удаляем снаряд при попадании
                iter.remove();

                // Воспроизводим звук взрыва если звуковые эффекты включены
                if (game.soundManager.isSfxEnabled()) {
                    explosionSound.play(0.5f);
                }

                continue;
            }

            // Удаляем снаряды за пределами экрана
            if (projectile.isOutOfScreen()) {
                iter.remove();
            }
        }
    }

    /**
     * Босс стреляет снарядом в игрока
     */
    private void bossShoot() {
        // Создаем снаряд под боссом
        float projectileX = boss.x + boss.width/2 - BOSS_PROJECTILE_SIZE/2;
        float projectileY = boss.y - BOSS_PROJECTILE_SIZE;

        // Добавляем снаряд в коллекцию
        bossProjectiles.add(new BossProjectile(projectileX, projectileY));

        // Звук выстрела
        if (game.soundManager.isSfxEnabled()) {
            explosionSound.play(0.2f);
        }
    }

    /**
     * Активирует предупреждение о низком уровне топлива
     */
    private void showLowFuelWarning() {
        lowFuelWarningActive = true;
        lowFuelWarningTime = 0;
    }

    /**
     * Отрисовывает предупреждение о низком уровне топлива
     */
    private void drawLowFuelWarning(float delta) {
        // Обновляем таймер предупреждения
        lowFuelWarningTime += delta;

        // Проверяем, завершилось ли отображение предупреждения
        if (lowFuelWarningTime >= LOW_FUEL_WARNING_DURATION) {
            lowFuelWarningActive = false;
            return;
        }

        // Расчет пульсирующей прозрачности для привлечения внимания
        float blinkValue = (float)Math.sin(lowFuelWarningTime * 10) * 0.3f + 0.7f;

        // Вычисляем размеры оповещения относительно экрана с отступами
        float warningHeight = GAME_HEIGHT * 0.09f; // 9% от высоты экрана (уменьшено с 10%)
        float warningWidth = GAME_WIDTH * 0.7f; // 70% от ширины экрана
        float warningY = GAME_HEIGHT * 0.18f; // 18% от нижней части экрана (увеличено с 15%)
        float warningX = (GAME_WIDTH - warningWidth) / 2; // центрирование по горизонтали

        // Отрисовка черного полупрозрачного фона
        game.batch.setColor(0, 0, 0, blinkValue * 0.8f);
        game.batch.draw(backgroundImage, warningX, warningY, warningWidth, warningHeight);

        // Добавляем красную рамку для привлечения внимания
        float borderThickness = GAME_HEIGHT * 0.008f; // Толщина рамки (уменьшена с 0.01f)
        game.batch.setColor(0.9f, 0.1f, 0.1f, blinkValue);

        // Верхняя граница
        game.batch.draw(backgroundImage, warningX, warningY + warningHeight - borderThickness, warningWidth, borderThickness);
        // Нижняя граница
        game.batch.draw(backgroundImage, warningX, warningY, warningWidth, borderThickness);
        // Левая граница
        game.batch.draw(backgroundImage, warningX, warningY, borderThickness, warningHeight);
        // Правая граница
        game.batch.draw(backgroundImage, warningX + warningWidth - borderThickness, warningY, borderThickness, warningHeight);

        // Получаем шрифт для предупреждения
        BitmapFont warningFont = game.fontManager.getUIFont();
        warningFont.setColor(1, 0.3f, 0.3f, blinkValue); // Красноватый текст с пульсацией

        // Масштабируем шрифт относительно размера экрана (увеличенный)
        float fontScale = GAME_WIDTH / 1100f; // Увеличенный масштаб шрифта (было 1500f)
        warningFont.getData().setScale(fontScale);

        // Текст предупреждения
        String warningText = "ВНИМАНИЕ! НИЗКИЙ УРОВЕНЬ ТОПЛИВА!";

        // Измеряем ширину текста для центрирования
        GlyphLayout layout = new GlyphLayout(warningFont, warningText);
        float x = warningX + (warningWidth - layout.width) / 2;
        float y = warningY + (warningHeight + layout.height) / 2;

        // Отрисовка текста предупреждения
        warningFont.draw(game.batch, warningText, x, y);

        // Сбрасываем цвет и масштаб шрифта
        warningFont.setColor(1, 1, 1, 1);
        warningFont.getData().setScale(1f);
        game.batch.setColor(1, 1, 1, 1);
    }
}
