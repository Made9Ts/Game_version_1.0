package com.badlogic.drop.screens;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Экран настроек игры
 */
public class OptionsScreen implements Screen {
    // Константы для вертикальной ориентации, адаптированные для S24 Ultra
    private static final float GAME_WIDTH = 720;
    private static final float GAME_HEIGHT = 1560;
    
    private final SpaceCourierGame game;
    private OrthographicCamera camera;
    private Stage stage;
    private Skin skin;
    private Texture backgroundImage;
    private GlyphLayout titleLayout;
    
    // UI элементы для настроек звука
    private CheckBox musicCheckbox;
    private CheckBox sfxCheckbox;

    public OptionsScreen(final SpaceCourierGame game) {
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
        titleLayout.setText(game.fontManager.getTitleFont(), "НАСТРОЙКИ");
        
        // Создаем скин для UI вручную
        createSkin();
        
        createUI();
    }
    
    private void createSkin() {
        skin = new Skin();
        
        // Добавляем шрифты в скин из FontManager
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
        
        // Создаем пиксель для чекбокса
        Pixmap checkboxPixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        checkboxPixmap.setColor(Color.WHITE);
        checkboxPixmap.fill();
        Texture checkboxTexture = new Texture(checkboxPixmap);
        skin.add("checkbox", checkboxTexture);
        
        Pixmap checkboxCheckedPixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        checkboxCheckedPixmap.setColor(Color.BLUE);
        checkboxCheckedPixmap.fill();
        Texture checkboxCheckedTexture = new Texture(checkboxCheckedPixmap);
        skin.add("checkbox-checked", checkboxCheckedTexture);
        
        pixmap.dispose();
        checkboxPixmap.dispose();
        checkboxCheckedPixmap.dispose();
        
        // Создаем стиль кнопки
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
        
        // Создаем стиль метки
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = skin.getColor("white");
        skin.add("default", labelStyle);
        
        // Создаем стиль чекбокса
        CheckBoxStyle checkBoxStyle = new CheckBoxStyle();
        checkBoxStyle.checkboxOn = skin.newDrawable("checkbox-checked");
        checkBoxStyle.checkboxOff = skin.newDrawable("checkbox");
        checkBoxStyle.font = skin.getFont("default-font");
        checkBoxStyle.fontColor = skin.getColor("white");
        skin.add("default", checkBoxStyle);
    }
    
    private void createUI() {
        // Создаем таблицу для размещения UI элементов
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        
        // Создаем кнопку возврата в главное меню
        TextButton backButton = new TextButton("НАЗАД", skin);
        
        // Создаем чекбоксы для настроек звука
        musicCheckbox = new CheckBox(" МУЗЫКА", skin);
        sfxCheckbox = new CheckBox(" SFX-ЗВУКИ", skin);
        
        // Устанавливаем начальные значения чекбоксов
        musicCheckbox.setChecked(game.soundManager.isMusicEnabled());
        sfxCheckbox.setChecked(game.soundManager.isSfxEnabled());
        
        // Добавляем отступ сверху для заголовка
        table.add().height(300);
        table.row();
        
        // Добавляем чекбоксы с отступами
        table.add(musicCheckbox).width(400).height(80).pad(20).left();
        table.row();
        table.add(sfxCheckbox).width(400).height(80).pad(20).left();
        table.row();
        
        // Добавляем отступ перед кнопкой возврата
        table.add().height(100);
        table.row();
        
        // Добавляем кнопку возврата
        table.add(backButton).width(450).height(120).pad(20);
        
        // Добавляем обработчики событий на чекбоксы
        musicCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = musicCheckbox.isChecked();
                game.soundManager.setMusicEnabled(enabled);
            }
        });
        
        sfxCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = sfxCheckbox.isChecked();
                game.soundManager.setSfxEnabled(enabled);
            }
        });
        
        // Добавляем обработчик события на кнопку возврата
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        
        // Добавляем таблицу на сцену
        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // Очищаем экран
        ScreenUtils.clear(0.05f, 0.05f, 0.2f, 1);
        
        // Обновляем камеру
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        // Отрисовываем фон и заголовок
        game.batch.begin();
        // Растягиваем фон на весь экран в вертикальной ориентации
        game.batch.draw(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // Рисуем заголовок
        game.fontManager.getTitleFont().draw(
            game.batch,
            "НАСТРОЙКИ",
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
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        // Метод вызывается при остановке игры
    }

    @Override
    public void resume() {
        // Метод вызывается при возобновлении игры
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundImage.dispose();
        
        if (skin != null) {
            // Перед освобождением скина, удаляем из него шрифты
            skin.remove("default-font", BitmapFont.class);
            skin.dispose();
        }
    }
} 