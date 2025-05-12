package com.badlogic.drop.screens;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * Экран главного меню игры, адаптированный для Samsung Galaxy S24 Ultra
 */
public class MainMenuScreen implements Screen {
    // Константы для вертикальной ориентации, адаптированные для S24 Ultra
    // Используем виртуальное разрешение, которое будет масштабироваться
    private static final float GAME_WIDTH = 720; // Половина ширины S24 Ultra для улучшения производительности
    private static final float GAME_HEIGHT = 1560; // Масштабированная высота с сохранением пропорций
    
    private final SpaceCourierGame game;
    private OrthographicCamera camera;
    private Stage stage;
    private Skin skin;
    private Texture backgroundImage;
    private GlyphLayout titleLayout;

    public MainMenuScreen(final SpaceCourierGame game) {
        this.game = game;

        // Сбрасываем цвета всех шрифтов при создании экрана
        game.fontManager.resetFontColors();

        // Создаем камеру и настраиваем вьюпорт для вертикальной ориентации
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        
        // Загружаем фон
        backgroundImage = new Texture("background.png");
        backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Создаем и настраиваем сцену с вертикальной ориентацией
        stage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));
        Gdx.input.setInputProcessor(stage);
        
        // Устанавливаем размер текста заголовка
        titleLayout = new GlyphLayout();
        titleLayout.setText(game.fontManager.getTitleFont(), "SPACE COURIER");
        
        // Создаем скин для UI вручную
        createSkin();
        
        createUI();
    }
    
    private void createSkin() {
        skin = new Skin();
        
        // Добавляем шрифт в скин из FontManager
        BitmapFont uiFont = game.fontManager.getUIFont();
        // Явно устанавливаем белый цвет для шрифта
        uiFont.setColor(Color.WHITE);
        skin.add("default-font", uiFont);
        
        // Добавляем цвета
        skin.add("white", new Color(1, 1, 1, 1));
        skin.add("black", new Color(0, 0, 0, 1));
        skin.add("gray", new Color(0.5f, 0.5f, 0.5f, 1));
        skin.add("blue", new Color(0.2f, 0.4f, 0.8f, 1));
        
        // Добавляем белый пиксель для фона кнопок
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white-pixel", new Texture(pixmap));
        pixmap.dispose();
        
        // Создаем стиль кнопки с улучшенным визуальным стилем
        TextButtonStyle textButtonStyle = new TextButtonStyle();
        textButtonStyle.font = skin.getFont("default-font");
        // Гарантируем, что цвет текста остается белым для всех состояний кнопки
        textButtonStyle.fontColor = new Color(1, 1, 1, 1);
        textButtonStyle.downFontColor = new Color(0.9f, 0.9f, 0.9f, 1);
        textButtonStyle.overFontColor = new Color(0.8f, 0.8f, 1, 1);
        textButtonStyle.disabledFontColor = skin.getColor("gray");
        
        // Добавляем фоны для кнопок
        textButtonStyle.up = skin.newDrawable("white-pixel", new Color(0.2f, 0.3f, 0.5f, 0.8f));
        textButtonStyle.down = skin.newDrawable("white-pixel", new Color(0.1f, 0.2f, 0.4f, 0.9f));
        textButtonStyle.over = skin.newDrawable("white-pixel", new Color(0.3f, 0.4f, 0.6f, 0.8f));
        
        // Добавляем стиль в скин
        skin.add("default", textButtonStyle);
    }
    
    private void createUI() {
        // Создаем таблицу для размещения UI элементов
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        
        // Создаем кнопки (используем латиницу)
        TextButton playButton = new TextButton("PLAY", skin);
        TextButton achievementsButton = new TextButton("ACHIEVEMENTS", skin);
        TextButton optionsButton = new TextButton("OPTIONS", skin);
        TextButton exitButton = new TextButton("EXIT", skin);
        
        // Добавляем отступ сверху для заголовка (увеличен для больших экранов)
        table.add().height(400);
        table.row();
        
        // Настраиваем размеры кнопок и добавляем их в таблицу (увеличены для больших экранов)
        table.add(playButton).width(450).height(120).pad(20);
        table.row();
        table.add(achievementsButton).width(450).height(120).pad(20);
        table.row();
        table.add(optionsButton).width(450).height(120).pad(20);
        table.row();
        table.add(exitButton).width(450).height(120).pad(20);
        
        // Добавляем обработчики событий на кнопки
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        
        achievementsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new AchievementsScreen(game, game.achievementSystem));
                dispose();
            }
        });
        
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Здесь будет переход к экрану настроек
                // game.setScreen(new OptionsScreen(game));
            }
        });
        
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        
        // Добавляем таблицу на сцену
        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // Очищаем экран с более глубоким цветом для улучшения визуального эффекта
        ScreenUtils.clear(0.05f, 0.05f, 0.2f, 1);
        
        // Обновляем камеру
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        // Отрисовываем фон и заголовок
        game.batch.begin();
        // Растягиваем фон на весь экран в вертикальной ориентации
        game.batch.draw(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // Рисуем заголовок с улучшенным качеством через FontManager
        game.fontManager.getTitleFont().draw(
            game.batch,
            "SPACE COURIER",
            GAME_WIDTH / 2 - titleLayout.width / 2,
            GAME_HEIGHT - 150
        );
        game.batch.end();
        
        // Отрисовываем UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Обеспечиваем корректное масштабирование для всех типов экранов
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        // Метод вызывается, когда этот экран становится текущим
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        // Метод вызывается, когда этот экран перестает быть текущим
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        // Вызывается при остановке игры, например, когда пользователь переключается на другое приложение
    }

    @Override
    public void resume() {
        // Вызывается при возобновлении игры
    }

    @Override
    public void dispose() {
        // Освобождаем ресурсы
        stage.dispose();
        backgroundImage.dispose();
        
        if (skin != null) {
            // Перед освобождением скина, удаляем из него шрифты, чтобы они не были удалены
            skin.remove("default-font", BitmapFont.class);
            skin.dispose();
        }
        
        // Не требуется вручную освобождать шрифты, так как их освободит FontManager
        // при закрытии приложения
    }
} 