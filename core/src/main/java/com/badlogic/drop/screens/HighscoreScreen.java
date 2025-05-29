package com.badlogic.drop.screens;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.drop.utils.UIFactory;
import com.badlogic.drop.utils.SciFiDrawable;

/**
 * Экран рекордов игрока Space Courier, отображающий максимальное количество очков.
 */
public class HighscoreScreen implements Screen {
    // Константы для виртуального разрешения
    private static final float GAME_WIDTH = 720;
    private static final float GAME_HEIGHT = 1560;
    
    // Константы для хранения рекордов
    private static final String PREFS_NAME = "spacecourier_highscores";
    private static final String KEY_MAX_SCORE = "max_score";
    private static final String KEY_TOTAL_GAMES = "total_games";
    private static final String KEY_TOTAL_TIME = "total_time";
    private static final String KEY_LAST_GAME_SCORE = "last_game_score";
    
    private final SpaceCourierGame game;
    private OrthographicCamera camera;
    private Stage stage;
    private Skin skin;
    private Texture backgroundImage;
    private GlyphLayout titleLayout;
    
    // Элементы UI для отображения рекордов
    private Label highscoreLabel;
    private Label totalGamesLabel;
    private Label totalTimeLabel;
    private Label lastScoreLabel;
    
    // Данные рекордов
    private int maxScore = 0;
    private int totalGames = 0;
    private float totalGameTime = 0;
    private int lastGameScore = 0;
    
    // Время анимации
    private float stateTime = 0;
    
    /**
     * Конструктор экрана рекордов
     * @param game Основной класс игры
     */
    public HighscoreScreen(final SpaceCourierGame game) {
        this.game = game;
        
        // Сбрасываем цвета шрифтов
        game.fontManager.resetFontColors();
        
        // Инициализируем камеру
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        
        // Загружаем фон
        backgroundImage = new Texture("background.png");
        backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Создаем сцену
        stage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));
        Gdx.input.setInputProcessor(stage);
        
        // Устанавливаем размер текста заголовка
        titleLayout = new GlyphLayout();
        titleLayout.setText(game.fontManager.getTitleFont(), "РЕКОРДЫ");
        
        // Создаем скин для UI
        createSkin();
        
        // Загружаем рекорды
        loadHighscores();
        
        // Создаем интерфейс
        createUI();
    }
    
    /**
     * Создает скин для UI
     */
    private void createSkin() {
        skin = new Skin();
        
        // Добавляем шрифт
        BitmapFont uiFont = game.fontManager.getUIFont();
        uiFont.setColor(Color.WHITE);
        skin.add("default-font", uiFont);
        
        // Добавляем заголовочный шрифт
        BitmapFont titleFont = game.fontManager.getTitleFont();
        titleFont.getData().setScale(0.8f);
        skin.add("title-font", titleFont);
        
        // Добавляем цвета
        skin.add("white", new Color(1, 1, 1, 1));
        skin.add("black", new Color(0, 0, 0, 1));
        skin.add("gray", new Color(0.5f, 0.5f, 0.5f, 1));
        skin.add("blue", new Color(0.2f, 0.4f, 0.8f, 1));
        skin.add("gold", new Color(1f, 0.9f, 0.2f, 1));
        
        // Добавляем белый пиксель для фона
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white-pixel", new Texture(pixmap));
        pixmap.dispose();
        
        // Создаем единый стиль sci-fi кнопок - прямоугольный с голубым цветом
        TextButton.TextButtonStyle buttonStyle = UIFactory.createSciFiButtonStyle(
            skin, skin.getFont("default-font"), 
            SciFiDrawable.ButtonShape.RECTANGULAR, 
            new Color(0.1f, 0.4f, 0.7f, 1f));  // Более насыщенный темно-голубой цвет
        
        // Увеличиваем размер шрифта для кнопок
        buttonStyle.font.getData().setScale(1.2f);
        skin.add("default", buttonStyle);
        
        // Стиль для обычных меток
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = skin.getColor("white");
        labelStyle.font.getData().setScale(1.0f);
        skin.add("default", labelStyle);
        
        // Стиль для заголовков
        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        titleLabelStyle.font = skin.getFont("title-font");
        titleLabelStyle.fontColor = skin.getColor("gold");
        titleLabelStyle.font.getData().setScale(0.6f);
        skin.add("title", titleLabelStyle);
        
        // Стиль для рекордов (более крупный шрифт)
        Label.LabelStyle scoreLabelStyle = new Label.LabelStyle();
        scoreLabelStyle.font = skin.getFont("default-font");
        scoreLabelStyle.font.getData().setScale(1.0f);
        scoreLabelStyle.fontColor = skin.getColor("white");
        skin.add("score", scoreLabelStyle);
    }
    
    /**
     * Создает UI компоненты
     */
    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        
        // Таблица для рекордов
        Table recordsTable = new Table();
        recordsTable.setBackground(skin.newDrawable("white-pixel", new Color(0.1f, 0.2f, 0.4f, 0.8f)));
        
        // Заголовок для рекордов
        Label headerLabel = new Label("СТАТИСТИКА ИГРОКА", skin, "title");
        headerLabel.setAlignment(Align.center);
        
        // Метки для отображения рекордов
        highscoreLabel = new Label("Максимальный счет: " + maxScore, skin, "score");
        totalGamesLabel = new Label("Всего игр: " + totalGames, skin, "default");
        
        // Форматируем время
        String timeFormatted = formatGameTime(totalGameTime);
        totalTimeLabel = new Label("Общее время игры: " + timeFormatted, skin, "default");
        
        lastScoreLabel = new Label("Последний счет: " + lastGameScore, skin, "default");
        
        // Кнопка возврата в меню
        TextButton backButton = new TextButton("В меню", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        
        // Добавляем компоненты в таблицу рекордов
        recordsTable.pad(30);
        recordsTable.add(headerLabel).colspan(2).pad(20).width(490).row();
        recordsTable.add(highscoreLabel).colspan(2).pad(15).width(490).row();
        recordsTable.add(lastScoreLabel).colspan(2).pad(10).width(490).row();
        recordsTable.add(totalGamesLabel).colspan(2).pad(10).width(490).row();
        recordsTable.add(totalTimeLabel).colspan(2).pad(10).width(490).row();
        
        // Добавляем таблицу рекордов и кнопку назад в основную таблицу
        mainTable.add(recordsTable).width(550).padBottom(50).row();
        mainTable.add(backButton).width(450).height(120).padTop(30);
        
        // Добавляем таблицу на сцену
        stage.addActor(mainTable);
    }
    
    /**
     * Загружает рекорды из Preferences
     */
    private void loadHighscores() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        maxScore = prefs.getInteger(KEY_MAX_SCORE, 0);
        totalGames = prefs.getInteger(KEY_TOTAL_GAMES, 0);
        totalGameTime = prefs.getFloat(KEY_TOTAL_TIME, 0);
        lastGameScore = prefs.getInteger(KEY_LAST_GAME_SCORE, 0);
    }
    
    /**
     * Форматирует время игры в удобочитаемый формат (часы:минуты:секунды)
     * @param timeInSeconds Время в секундах
     * @return Отформатированная строка времени
     */
    private String formatGameTime(float timeInSeconds) {
        int totalSeconds = (int) timeInSeconds;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d ч %d мин %d сек", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d мин %d сек", minutes, seconds);
        } else {
            return String.format("%d сек", seconds);
        }
    }
    
    /**
     * Статический метод для обновления рекорда
     * @param score Текущий счет
     * @param gameTime Время игры в секундах
     */
    public static void updateHighscore(int score, float gameTime) {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Обновляем максимальный счет, если текущий счет больше
        int currentMaxScore = prefs.getInteger(KEY_MAX_SCORE, 0);
        if (score > currentMaxScore) {
            prefs.putInteger(KEY_MAX_SCORE, score);
        }
        
        // Увеличиваем счетчик игр
        int totalGames = prefs.getInteger(KEY_TOTAL_GAMES, 0);
        prefs.putInteger(KEY_TOTAL_GAMES, totalGames + 1);
        
        // Добавляем время игры
        float totalTime = prefs.getFloat(KEY_TOTAL_TIME, 0);
        prefs.putFloat(KEY_TOTAL_TIME, totalTime + gameTime);
        
        // Сохраняем последний счет
        prefs.putInteger(KEY_LAST_GAME_SCORE, score);
        
        // Сохраняем изменения
        prefs.flush();
    }

    @Override
    public void render(float delta) {
        // Очищаем экран
        ScreenUtils.clear(0, 0, 0.2f, 1);
        
        // Обновляем время для анимации
        stateTime += delta;
        
        // Обновляем камеру
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        // Рисуем фон
        game.batch.begin();
        
        // Рисуем фоновое изображение
        game.batch.draw(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        game.batch.end();
        
        // Рисуем UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        // Устанавливаем процессор ввода на сцену
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        // Ничего не делаем
    }

    @Override
    public void resume() {
        // Ничего не делаем
    }

    @Override
    public void dispose() {
        // Сначала восстанавливаем масштаб шрифтов перед освобождением ресурсов
        if (skin != null) {
            // Восстанавливаем масштабы всех шрифтов
            BitmapFont defaultFont = skin.getFont("default-font");
            if (defaultFont != null) {
                defaultFont.getData().setScale(1.0f);
            }
            
            BitmapFont titleFont = skin.getFont("title-font");
            if (titleFont != null) {
                titleFont.getData().setScale(1.0f);
            }
            
            // Сначала удаляем шрифт из стилей, чтобы избежать его освобождения при удалении стилей
            TextButton.TextButtonStyle buttonStyle = skin.get("default", TextButton.TextButtonStyle.class);
            if (buttonStyle != null) {
                buttonStyle.font = null;
            }
            
            // Удаляем шрифты из skin перед его освобождением
            skin.remove("default-font", BitmapFont.class);
            skin.remove("title-font", BitmapFont.class);
        }
        
        stage.dispose();
        if (skin != null) {
            skin.dispose();
        }
        backgroundImage.dispose();
    }
} 