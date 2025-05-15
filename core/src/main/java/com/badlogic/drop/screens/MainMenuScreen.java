package com.badlogic.drop.screens;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.drop.util.StarField;
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
    private Texture shipImage;
    private GlyphLayout titleLayout;
    private StarField starField;
    
    // Переменная для отслеживания времени (для эффекта парения)
    private float stateTime = 0;

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
        
        // Загружаем изображение корабля
        shipImage = new Texture("ship.png");
        shipImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Создаем и настраиваем сцену с вертикальной ориентацией
        stage = new Stage(new FitViewport(GAME_WIDTH, GAME_HEIGHT, camera));
        Gdx.input.setInputProcessor(stage);
        
        // Устанавливаем размер текста заголовка
        titleLayout = new GlyphLayout();
        // Используем две отдельные строки для заголовка с уменьшенным размером
        BitmapFont titleFont = game.fontManager.getTitleFont();
        titleFont.getData().setScale(0.7f);
        
        // Разбиваем текст на строки для заголовка
        String line1 = "КОСМИЧЕСКИЙ";
        String line2 = "ПУТЕШЕСТВЕННИК";
        
        // Вычисляем общую высоту заголовка
        GlyphLayout layout1 = new GlyphLayout(titleFont, line1);
        GlyphLayout layout2 = new GlyphLayout(titleFont, line2);
        float totalHeight = layout1.height + 20 + layout2.height; // 20 - расстояние между строками
        float maxWidth = Math.max(layout1.width, layout2.width);
        
        // Обновляем titleLayout с максимальной шириной и общей высотой
        titleLayout.width = maxWidth;
        titleLayout.height = totalHeight;
        
        titleFont.getData().setScale(1.0f);
        
        // Создаем скин для UI вручную
        createSkin();
        
        // Создаем звездное поле
        starField = new StarField(GAME_WIDTH, GAME_HEIGHT);
        
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
        TextButton playButton = new TextButton("Играть", skin);
        TextButton profileButton = new TextButton("Профиль", skin);
        TextButton optionsButton = new TextButton("Настройки", skin);
        TextButton exitButton = new TextButton("Выход", skin);
        
        // Добавляем отступ сверху для заголовка (увеличен для больших экранов)
        table.add().height(120);
        table.row();
        
        // Настраиваем размеры кнопок и добавляем их в таблицу (увеличены для больших экранов)
        table.add(playButton).width(450).height(120).pad(20);
        table.row();
        table.add(profileButton).width(450).height(120).pad(20);
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
        
        profileButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ProfileScreen(game));
                dispose();
            }
        });
        
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Переход к экрану настроек
                game.setScreen(new OptionsScreen(game));
                dispose();
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
        // Обновляем время
        stateTime += delta;
        
        // Очищаем экран с более глубоким цветом для улучшения визуального эффекта
        ScreenUtils.clear(0.05f, 0.05f, 0.2f, 1);
        
        // Обновляем камеру
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        // Отрисовываем фон и заголовок
        game.batch.begin();
        // Растягиваем фон на весь экран в вертикальной ориентации
        game.batch.draw(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // Отрисовываем и обновляем звездное поле
        starField.update(delta);
        starField.render(game.batch);
        
        // Рисуем заголовок
        BitmapFont titleFont = game.fontManager.getTitleFont();
        // Временно уменьшаем размер шрифта для заголовка
        titleFont.getData().setScale(0.7f);
        
        // Разбиваем текст на две строки
        String line1 = "КОСМИЧЕСКИЙ";
        String line2 = "ПУТЕШЕСТВЕННИК";
        
        // Измеряем ширину каждой строки
        GlyphLayout layout1 = new GlyphLayout(titleFont, line1);
        GlyphLayout layout2 = new GlyphLayout(titleFont, line2);
        
        // Отрисовываем каждую строку центрированно
        titleFont.draw(
            game.batch,
            line1,
            GAME_WIDTH / 2 - layout1.width / 2,
            GAME_HEIGHT - 120 // Поднимаем заголовок выше
        );
        
        titleFont.draw(
            game.batch,
            line2,
            GAME_WIDTH / 2 - layout2.width / 2,
            GAME_HEIGHT - 120 - layout1.height - 20 // Расстояние между строками
        );
        
        // Возвращаем исходный размер шрифта
        titleFont.getData().setScale(1.0f);
        
        // Отрисовываем корабль между заголовком и кнопками
        float shipWidth = 160; // Ширина корабля на экране
        float shipHeight = 160; // Высота корабля на экране
        float shipX = GAME_WIDTH / 2 - shipWidth / 2; // Центрируем корабль по горизонтали
        float shipY = GAME_HEIGHT - 450; // Позиция корабля по вертикали под заголовком (опущен ниже)
        
        // Добавляем небольшой эффект парения для корабля
        float hoverOffset = (float) Math.sin(stateTime * 3) * 10;
        // Добавляем легкий эффект вращения
        float rotation = (float) Math.sin(stateTime * 2) * 5; // Вращение на ±5 градусов
        // Добавляем эффект пульсации
        float scale = 1.0f + (float) Math.sin(stateTime * 1.5f) * 0.05f; // Пульсация размера ±5%
        
        // Рисуем корабль
        game.batch.draw(
            shipImage, 
            shipX, 
            shipY + hoverOffset, 
            shipWidth / 2, // Центр вращения по X
            shipHeight / 2, // Центр вращения по Y
            shipWidth, 
            shipHeight, 
            scale, scale, // Масштаб с пульсацией
            rotation, // Угол вращения
            0, 0, // Исходная позиция в текстуре
            shipImage.getWidth(), 
            shipImage.getHeight(), 
            false, false // Отражение
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
        shipImage.dispose();
        
        if (skin != null) {
            // Перед освобождением скина, удаляем из него шрифты, чтобы они не были удалены
            skin.remove("default-font", BitmapFont.class);
            skin.dispose();
        }
        
        // Не требуется вручную освобождать шрифты, так как их освободит FontManager
        // при закрытии приложения
        
        if (starField != null) {
            starField.dispose();
        }
    }
} 