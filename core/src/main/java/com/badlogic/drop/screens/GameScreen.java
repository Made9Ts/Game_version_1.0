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
    
    // Константы игрового процесса
    private static final int SHIP_SPEED = 500;
    private static final float SPEED_BOOST_MULTIPLIER = 1.5f;
    private static final float MAX_FUEL = 100f;
    private static final float FUEL_CONSUMPTION = 2.4f;
    private static final int MAX_LIVES = 3;
    
    // Константы интервалов появления объектов
    private static final long GROUP_PAUSE = 3000000000L;
    
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
    
    // Статистика для достижений
    private float gameTime;
    private int fuelCollected;
    private boolean damageTaken;
    private boolean achievementNotificationActive;
    private float achievementNotificationTime;
    private String achievementNotificationText;

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
    private int maxAsteroidsPerGroup = 3;
    private int enemiesInGroup = 0;
    private int maxEnemiesPerGroup = 2;
    
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

        // Создание первых объектов
        spawnAsteroid();
        spawnFuelCanister();
    }

    private void spawnAsteroid() {
        Rectangle asteroid = new Rectangle();
        asteroid.x = MathUtils.random(0, GAME_WIDTH - ASTEROID_SIZE);
        asteroid.y = GAME_HEIGHT; // Сверху экрана
        asteroid.width = ASTEROID_SIZE;
        asteroid.height = ASTEROID_SIZE;
        asteroids.add(asteroid);
        lastAsteroidTime = TimeUtils.nanoTime();
    }

    private void spawnEnemy() {
        Rectangle enemy = new Rectangle();
        enemy.x = MathUtils.random(0, GAME_WIDTH - ENEMY_SIZE);
        enemy.y = GAME_HEIGHT; // Сверху экрана
        enemy.width = ENEMY_SIZE;
        enemy.height = ENEMY_SIZE;
        enemies.add(enemy);
        lastEnemyTime = TimeUtils.nanoTime();
    }

    private void spawnFuelCanister() {
        Rectangle fuelCanister = new Rectangle();
        fuelCanister.x = MathUtils.random(0, GAME_WIDTH - FUEL_SIZE);
        fuelCanister.y = GAME_HEIGHT; // Сверху экрана
        fuelCanister.width = FUEL_SIZE;
        fuelCanister.height = FUEL_SIZE;
        fuelCanisters.add(fuelCanister);
        lastFuelTime = TimeUtils.nanoTime();
    }

    /**
     * Создает новое сердечко для восстановления жизни
     */
    private void spawnHeart() {
        Rectangle heart = new Rectangle();
        heart.x = MathUtils.random(0, GAME_WIDTH - HEART_SIZE);
        heart.y = GAME_HEIGHT; // Сверху экрана
        heart.width = HEART_SIZE;
        heart.height = HEART_SIZE;
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
        
        // Отображение оставшихся жизней
        font.draw(game.batch, "Lives: " + lives, 20, GAME_HEIGHT - 160);
        
        // Отображение уровня топлива в процентах
        int fuelPercent = (int)((fuel / MAX_FUEL) * 100);
        
        // Меняем цвет текста в зависимости от уровня топлива
        if (fuelPercent > 60) {
            font.setColor(0.2f, 0.8f, 0.2f, 1.0f); // Зеленый для безопасного уровня
        } else if (fuelPercent > 30) {
            font.setColor(0.8f, 0.8f, 0.2f, 1.0f); // Желтый для среднего уровня
        } else {
            font.setColor(0.8f, 0.2f, 0.2f, 1.0f); // Красный для опасного уровня
        }
        
        font.draw(game.batch, "Fuel: " + fuelPercent + "%", 20, GAME_HEIGHT - 200);
        
        // Показываем текущее комбо, если оно есть
        int comboCount = difficultySystem.getComboCount();
        if (comboCount > 0) {
            // Определяем цвет комбо: от желтого к красному по мере роста
            float comboRatio = Math.min(1.0f, comboCount / 20.0f);
            font.setColor(1.0f, 1.0f - comboRatio * 0.7f, 0.3f, 1.0f);
            font.draw(game.batch, "Combo: x" + comboCount, 20, GAME_HEIGHT - 240);
        }
        
        // Возвращаем цвет шрифта к белому
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
        fuel -= FUEL_CONSUMPTION * delta;
        if (fuel <= 0) {
            loseLife();
            fuel = MAX_FUEL / 2; // Дадим половину бака при потере жизни
        }

        // Создание новых объектов (сложность влияет на частоту появления)
        // Используем систему групп для астероидов
        if (TimeUtils.nanoTime() - lastAsteroidTime > 2500000000L / difficulty) {
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
        if (score > 500 && TimeUtils.nanoTime() - lastEnemyTime > 5000000000L / difficulty) {
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

        // Топливо появляется очень редко (интервал увеличен в 1.5 раза)
        if (TimeUtils.nanoTime() - lastFuelTime > 9000000000L) {
            spawnFuelCanister();
        }
        
        // Сердечки появляются только если игрок потерял жизнь и они нужны (флаг needHeart)
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
    }
    
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

    private void updateAsteroids(float delta, float difficulty) {
        for (Iterator<Rectangle> iter = asteroids.iterator(); iter.hasNext(); ) {
            Rectangle asteroid = iter.next();
            // Падение астероидов сверху вниз с учетом сложности
            // Уменьшаем базовую скорость астероидов (было 130)
            asteroid.y -= (100 + 25 * difficulty) * delta;
            
            // Удаление астероидов, вышедших за пределы экрана
            if (asteroid.y + ASTEROID_SIZE < 0) {
                iter.remove();
                score += 10; // Очки за уклонение от астероида
                // Используем специализированный метод для уклонения
                difficultySystem.registerDodge();
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
            // Уменьшаем базовую скорость врагов (было 150)
            enemy.y -= (120 + 35 * difficulty) * delta;
            
            // Движение врагов влево-вправо, следуя за кораблем (более разумное преследование)
            // Уменьшаем скорость преследования (было 60 + 8)
            if (enemy.x < ship.x) enemy.x += (40 + 6 * difficulty) * delta;
            if (enemy.x > ship.x) enemy.x -= (40 + 6 * difficulty) * delta;
            
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
        
        // Создаем бонус в случайном месте экрана
        float x = MathUtils.random(0, GAME_WIDTH - POWERUP_SIZE);
        float y = GAME_HEIGHT; // Сверху экрана
        
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

    // Отрисовка уведомления о достижении
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
        
        // Отрисовка полупрозрачного фона
        game.batch.setColor(0, 0, 0, alpha * 0.7f);
        game.batch.draw(backgroundImage, 0, GAME_HEIGHT - 180, GAME_WIDTH, 100);
        
        // Получаем шрифт для уведомления
        BitmapFont notificationFont = game.fontManager.getUIFont();
        notificationFont.setColor(1, 1, 0.3f, alpha); // Желтоватый цвет для достижений
        
        // Измеряем ширину текста для центрирования
        GlyphLayout layout = new GlyphLayout(notificationFont, achievementNotificationText);
        float x = (GAME_WIDTH - layout.width) / 2;
        float y = GAME_HEIGHT - 130;
        
        // Отрисовка текста уведомления
        notificationFont.draw(game.batch, achievementNotificationText, x, y);
        
        // Сбрасываем цвет
        notificationFont.setColor(1, 1, 1, 1);
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
    }

    /**
     * Проверяет состояние аудио настроек и применяет их
     */
    private void checkAudioSettings() {
        // Инициализация Stage для UI (только для экрана окончания игры)
        gameOverStage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));
    }
}
