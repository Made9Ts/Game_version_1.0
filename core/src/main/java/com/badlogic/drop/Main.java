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

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
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
    private Rectangle ship;
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

    // Константы
    private static final int SHIP_SPEED = 300;
    private static final float MAX_FUEL = 100f;
    private static final float FUEL_CONSUMPTION = 2f; // единиц в секунду

    @Override
    public void create() {
        // Инициализация графики
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        font = new BitmapFont();

        // Загрузка текстур
        shipImage = new Texture("ship.png");
        asteroidImage = new Texture("asteroid.png");
        enemyImage = new Texture("enemy.png");
        fuelImage = new Texture("fuel.png");
        backgroundImage = new Texture("background.png");

        // Загрузка звуков
        collectSound = Gdx.audio.newSound(Gdx.files.internal("collect.wav"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("gamemusic.mp3"));

        // Настройка музыки
        gameMusic.setLooping(true);
        gameMusic.play();

        // Инициализация игровых объектов
        initGame();
    }

    private void initGame() {
        // Создание корабля игрока
        ship = new Rectangle();
        ship.x = 800 / 2 - 64 / 2;
        ship.y = 20;
        ship.width = 64;
        ship.height = 64;

        // Инициализация игровых коллекций
        asteroids = new Array<Rectangle>();
        enemies = new Array<Rectangle>();
        fuelCanisters = new Array<Rectangle>();

        // Сброс игровых параметров
        score = 0;
        lives = 3;
        fuel = MAX_FUEL;
        difficulty = 1.0f;
        playerSkill = 1.0f;
        gameOver = false;

        // Создание первых объектов
        spawnAsteroid();
        spawnFuelCanister();
    }

    private void spawnAsteroid() {
        Rectangle asteroid = new Rectangle();
        asteroid.x = MathUtils.random(0, 800 - 64);
        asteroid.y = 480;
        asteroid.width = 64;
        asteroid.height = 64;
        asteroids.add(asteroid);
        lastAsteroidTime = TimeUtils.nanoTime();
    }

    private void spawnEnemy() {
        Rectangle enemy = new Rectangle();
        enemy.x = MathUtils.random(0, 800 - 64);
        enemy.y = 480;
        enemy.width = 64;
        enemy.height = 64;
        enemies.add(enemy);
        lastEnemyTime = TimeUtils.nanoTime();
    }

    private void spawnFuelCanister() {
        Rectangle fuelCanister = new Rectangle();
        fuelCanister.x = MathUtils.random(0, 800 - 32);
        fuelCanister.y = 480;
        fuelCanister.width = 32;
        fuelCanister.height = 32;
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
        batch.begin();

        // Фон
        batch.draw(backgroundImage, 0, 0, 800, 480);

        // Игровые объекты
        batch.draw(shipImage, ship.x, ship.y);

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
        font.draw(batch, "Очки: " + score, 10, 470);
        font.draw(batch, "Жизни: " + lives, 10, 450);
        font.draw(batch, "Топливо: " + (int)fuel, 10, 430);

        if (gameOver) {
            font.draw(batch, "КОНЕЦ ИГРЫ - Нажмите Enter для перезапуска", 200, 240);
        }

        batch.end();

        // Обработка перезапуска игры
        if (gameOver && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            initGame();
        }
    }

    private void updateGame() {
        // Обработка ввода
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            ship.x = touchPos.x - 64 / 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) ship.x -= SHIP_SPEED * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) ship.x += SHIP_SPEED * Gdx.graphics.getDeltaTime();

        // Ограничения положения корабля
        if (ship.x < 0) ship.x = 0;
        if (ship.x > 800 - 64) ship.x = 800 - 64;

        // Расход топлива
        fuel -= FUEL_CONSUMPTION * Gdx.graphics.getDeltaTime();
        if (fuel <= 0) {
            loseLife();
            fuel = MAX_FUEL / 2; // Дадим половину бака при потере жизни
        }

        // Создание новых объектов
        if (TimeUtils.nanoTime() - lastAsteroidTime > 1000000000 / difficulty) {
            spawnAsteroid();
        }

        if (score > 500 && TimeUtils.nanoTime() - lastEnemyTime > 2000000000 / difficulty) {
            spawnEnemy();
        }

        if (TimeUtils.nanoTime() - lastFuelTime > 30000000) {
            spawnFuelCanister();
        }

        // Обновление движения астероидов
        for (Array.ArrayIterator<Rectangle> iter = new Array.ArrayIterator<Rectangle>(asteroids); iter.hasNext();) {
            Rectangle asteroid = iter.next();
            asteroid.y -= (150 + 20 * difficulty) * Gdx.graphics.getDeltaTime();

            // Удаление астероидов за пределами экрана
            if (asteroid.y + 64 < 0) {
                iter.remove();
                score += 10; // Очки за уклонение
                updatePlayerSkill(true); // Уклонился - молодец
            }

            // Обработка столкновений
            if (asteroid.overlaps(ship)) {
                explosionSound.play();
                iter.remove();
                loseLife();
                updatePlayerSkill(false); // Столкнулся - плохо
            }
        }

        // Обновление движения врагов
        for (Array.ArrayIterator<Rectangle> iter = new Array.ArrayIterator<Rectangle>(enemies); iter.hasNext();) {
            Rectangle enemy = iter.next();
            // Враги двигаются быстрее и следуют за игроком
            float targetX = ship.x;
            enemy.x += (targetX > enemy.x ? 1 : -1) * 30 * Gdx.graphics.getDeltaTime();
            enemy.y -= (180 + 20 * difficulty) * Gdx.graphics.getDeltaTime();

            // Удаление врагов за пределами экрана
            if (enemy.y + 64 < 0) {
                iter.remove();
                score += 20; // Больше очков за уклонение от более опасного врага
                updatePlayerSkill(true);
            }

            // Обработка столкновений
            if (enemy.overlaps(ship)) {
                explosionSound.play();
                iter.remove();
                loseLife();
                updatePlayerSkill(false);
            }
        }

        // Обновление движения топлива
        for (Array.ArrayIterator<Rectangle> iter = new Array.ArrayIterator<Rectangle>(fuelCanisters); iter.hasNext();) {
            Rectangle fuelCanister = iter.next();
            fuelCanister.y -= 100 * Gdx.graphics.getDeltaTime();

            // Удаление топлива за пределами экрана
            if (fuelCanister.y + 32 < 0) {
                iter.remove();
            }

            // Обработка сбора топлива
            if (fuelCanister.overlaps(ship)) {
                collectSound.play();
                iter.remove();
                fuel = Math.min(MAX_FUEL, fuel + 25);
                score += 5;
                updatePlayerSkill(true);
            }
        }

        // Адаптация сложности к умению игрока
        adaptDifficulty();
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
            gameMusic.stop();
        }
    }

    private void updatePlayerSkill(boolean success) {
        // Обновляем оценку умения игрока на основе успехов/неудач
        if (success) {
            playerSkill += 0.05f;
        } else {
            playerSkill -= 0.1f;
        }

        // Ограничиваем в разумных пределах
        playerSkill = MathUtils.clamp(playerSkill, 0.5f, 2.0f);
    }

    private void adaptDifficulty() {
        // Адаптируем сложность к умению игрока
        // Более умелые игроки получают более сложный геймплей
        difficulty = 1.0f + (score / 1000f) * (playerSkill / 1.5f);

        // Минимальная сложность растет с очками, но медленнее для менее умелых игроков
        float minDifficulty = 1.0f + (score / 2000f);
        difficulty = Math.max(difficulty, minDifficulty);

        // Максимально допустимая сложность
        difficulty = Math.min(difficulty, 5.0f);
    }

    @Override
    public void dispose() {
        shipImage.dispose();
        asteroidImage.dispose();
        enemyImage.dispose();
        fuelImage.dispose();
        backgroundImage.dispose();
        collectSound.dispose();
        explosionSound.dispose();
        gameMusic.dispose();
        batch.dispose();
        font.dispose();
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
