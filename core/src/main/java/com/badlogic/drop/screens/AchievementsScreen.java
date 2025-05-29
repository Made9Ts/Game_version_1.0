package com.badlogic.drop.screens;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.drop.utils.UIFactory;

/**
 * Экран достижений игрока
 */
public class AchievementsScreen implements Screen {
    // Константы для вертикальной ориентации
    private static final float GAME_WIDTH = 720;
    private static final float GAME_HEIGHT = 1560;
    
    private final SpaceCourierGame game;
    private final AchievementSystem achievementSystem;
    private OrthographicCamera camera;
    private Stage stage;
    private Skin skin;
    private Texture backgroundImage;
    
    /**
     * Конструктор экрана достижений
     * @param game экземпляр игры
     * @param achievementSystem система достижений
     */
    public AchievementsScreen(final SpaceCourierGame game, final AchievementSystem achievementSystem) {
        this.game = game;
        this.achievementSystem = achievementSystem;
        
        // Создаем камеру и настраиваем вьюпорт для вертикальной ориентации
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        
        // Загружаем фон
        backgroundImage = new Texture("background.png");
        backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Создаем и настраиваем сцену с вертикальной ориентацией
        stage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));
        Gdx.input.setInputProcessor(stage);
        
        // Создаем скин для UI
        createSkin();
        
        // Создаем UI
        createUI();
    }
    
    /**
     * Создает скин для UI
     */
    private void createSkin() {
        skin = new Skin();
        
        // Добавляем шрифты в скин
        skin.add("default-font", game.fontManager.getGameFont());
        skin.add("title-font", game.fontManager.getTitleFont());
        
        // Добавляем цвета
        skin.add("white", new Color(1, 1, 1, 1));
        skin.add("black", new Color(0, 0, 0, 1));
        skin.add("gray", new Color(0.5f, 0.5f, 0.5f, 1));
        skin.add("blue", new Color(0.2f, 0.4f, 0.8f, 1));
        skin.add("green", new Color(0.2f, 0.8f, 0.2f, 1));
        skin.add("yellow", new Color(0.8f, 0.8f, 0.2f, 1));
        skin.add("red", new Color(0.8f, 0.2f, 0.2f, 1));
        
        // Добавляем белый пиксель для использования в качестве фона
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();
        
        // Создаем стиль кнопки
        TextButtonStyle textButtonStyle = UIFactory.createSciFiBlueButtonStyle(skin, skin.getFont("default-font"));
        skin.add("default", textButtonStyle);
        
        // Создаем стиль для заголовка
        LabelStyle titleStyle = new LabelStyle();
        titleStyle.font = skin.getFont("title-font");
        titleStyle.fontColor = skin.getColor("white");
        skin.add("title", titleStyle);
        
        // Создаем стиль для обычного текста
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = skin.getColor("white");
        skin.add("default", labelStyle);
        
        // Создаем стиль для заблокированного достижения
        LabelStyle lockedStyle = new LabelStyle();
        lockedStyle.font = skin.getFont("default-font");
        lockedStyle.fontColor = skin.getColor("gray");
        skin.add("locked", lockedStyle);
        
        // Создаем стиль для разблокированного достижения
        LabelStyle unlockedStyle = new LabelStyle();
        unlockedStyle.font = skin.getFont("default-font");
        unlockedStyle.fontColor = skin.getColor("green");
        skin.add("unlocked", unlockedStyle);
    }
    
    /**
     * Создает UI экрана
     */
    private void createUI() {
        // Создаем корневую таблицу для размещения всех элементов
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(20);
        
        // Добавляем заголовок
        Label titleLabel = new Label("ДОСТИЖЕНИЯ", skin, "title");
        rootTable.add(titleLabel).padBottom(50).row();
        
        // Выводим отладочную информацию о шрифте
        Gdx.app.log("Achievements", "Шрифт заголовка поддерживает кириллицу: " + 
            game.fontManager.getTitleFont().getData().hasGlyph('Я'));
        
        // Создаем таблицу для списка достижений
        Table achievementsTable = new Table();
        achievementsTable.pad(10);
        
        // Получаем все достижения
        ObjectMap<String, AchievementSystem.Achievement> achievements = achievementSystem.getAllAchievements();
        Array<String> ids = achievements.keys().toArray();
        
        // Добавляем каждое достижение в таблицу
        for (String id : ids) {
            AchievementSystem.Achievement achievement = achievements.get(id);
            
            // Создаем строку таблицы
            Table achievementRow = new Table();
            achievementRow.pad(10);
            
            // Определяем стиль в зависимости от состояния достижения
            String titleStyle = achievement.unlocked ? "unlocked" : "locked";
            String descStyle = achievement.unlocked ? "default" : "locked";
            
            // Заголовок достижения
            Label achievementTitleLabel = new Label(achievement.title, skin, titleStyle);
            achievementRow.add(achievementTitleLabel).left().expandX().row();
            
            // Описание достижения
            Label descLabel = new Label(achievement.description, skin, descStyle);
            achievementRow.add(descLabel).left().expandX().padTop(5).row();
            
            // Прогресс достижения
            String progressText = achievement.unlocked ? 
                "Разблокировано!" : 
                "Прогресс: " + achievement.progress + "/" + achievement.maxProgress;
            Label progressLabel = new Label(progressText, skin, descStyle);
            achievementRow.add(progressLabel).left().expandX().padTop(5);
            
            // Добавляем отладочную информацию для каждого достижения
            Gdx.app.log("Achievement", id + ": " + achievement.title + 
                " - шрифт поддерживает кириллицу: " + 
                skin.getFont("default-font").getData().hasGlyph('Я'));
            
            // Добавляем строку в таблицу достижений
            achievementsTable.add(achievementRow).expandX().fillX().row();
            
            // Добавляем разделитель, используя Drawable из скина
            if (ids.indexOf(id, true) < ids.size - 1) {
                Table separator = new Table();
                separator.pad(5);
                separator.setBackground(skin.newDrawable("white", new Color(0.5f, 0.5f, 0.5f, 0.5f)));
                achievementsTable.add(separator).expandX().fillX().height(1).padTop(10).padBottom(10).row();
            }
        }
        
        // Создаем скролл-панель для списка достижений
        ScrollPane scrollPane = new ScrollPane(achievementsTable);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        
        // Добавляем скролл-панель в корневую таблицу
        rootTable.add(scrollPane).expand().fill().padBottom(20).row();
        
        // Добавляем кнопку "Назад"
        TextButton backButton = new TextButton("НАЗАД", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Пересоздаем FontManager перед переходом обратно в главное меню
                game.recreateFontManager();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        rootTable.add(backButton).width(220).height(90).padBottom(20);
        
        // Добавляем корневую таблицу на сцену
        stage.addActor(rootTable);
    }
    
    @Override
    public void render(float delta) {
        // Очищаем экран
        ScreenUtils.clear(0, 0, 0.2f, 1);
        
        // Обновляем камеру
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        // Отрисовываем фон
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT);
        game.batch.end();
        
        // Отрисовываем UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        // Обеспечиваем корректное масштабирование
        stage.getViewport().update(width, height, true);
    }
    
    @Override
    public void show() {
        // Устанавливаем обработчик ввода
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void hide() {
        // Сбрасываем обработчик ввода
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
        // Освобождаем ресурсы
        stage.dispose();
        
        // Перед освобождением скина, удаляем из него шрифты, чтобы они не были удалены
        // LibGDX освобождает все ресурсы, добавленные в скин, поэтому нужно удалить ссылки на шрифты
        // перед вызовом skin.dispose()
        if (skin != null) {
            skin.remove("default-font", BitmapFont.class);
            skin.remove("title-font", BitmapFont.class);
            skin.dispose();
        }
        
        backgroundImage.dispose();
    }
} 