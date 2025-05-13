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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Экран настроек игры с управлением звуком и музыкой
 */
public class OptionsScreen implements Screen {
    // Константы для верстки экрана
    private static final float GAME_WIDTH = 720;
    private static final float GAME_HEIGHT = 1560;
    private static final float HEADER_HEIGHT = 300;
    private static final float CHECKBOX_WIDTH = 400;
    private static final float CHECKBOX_HEIGHT = 80;
    private static final float SLIDER_WIDTH = 400;
    private static final float SLIDER_HEIGHT = 50;
    private static final float BUTTON_WIDTH = 450;
    private static final float BUTTON_HEIGHT = 120;
    private static final float ELEMENT_PADDING = 20;
    private static final float SECTION_SPACING = 60;
    
    // Константы для цветов
    private static final Color BACKGROUND_COLOR = new Color(0.05f, 0.05f, 0.2f, 1);
    private static final Color SLIDER_BG_COLOR = new Color(0.15f, 0.15f, 0.3f, 1);
    private static final Color SLIDER_KNOB_COLOR = new Color(0.5f, 0.5f, 0.9f, 1);
    private static final Color TITLE_COLOR = new Color(1, 1, 1, 1);
    
    // Ссылка на основной класс игры
    private final SpaceCourierGame game;
    
    // Графические и UI компоненты
    private OrthographicCamera camera;
    private Stage stage;
    private Skin skin;
    private Texture backgroundImage;
    private GlyphLayout titleLayout;
    
    // UI элементы для настроек звука
    private CheckBox musicCheckbox;
    private CheckBox sfxCheckbox;
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    private Label musicVolumeLabel;
    private Label sfxVolumeLabel;

    /**
     * Создает экран настроек
     */
    public OptionsScreen(final SpaceCourierGame game) {
        this.game = game;
        initializeGraphics();
        createUI();
    }
    
    /**
     * Инициализирует графические компоненты
     */
    private void initializeGraphics() {
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
    }
    
    /**
     * Создает скин для UI элементов
     */
    private void createSkin() {
        skin = new Skin();
        
        // Добавляем шрифты в скин из FontManager
        BitmapFont uiFont = game.fontManager.getUIFont();
        uiFont.setColor(Color.WHITE);
        skin.add("default-font", uiFont);
        
        // Добавляем цвета
        skin.add("white", new Color(1, 1, 1, 1));
        skin.add("black", new Color(0, 0, 0, 1));
        skin.add("gray", new Color(0.5f, 0.5f, 0.5f, 1));
        skin.add("blue", new Color(0.2f, 0.4f, 0.8f, 1));
        
        // Добавляем ресурсы для UI элементов
        createPixmapTextures();
        
        // Создаем стили UI элементов
        createButtonStyle();
        createLabelStyle();
        createCheckBoxStyle();
        createSliderStyle();
    }
    
    /**
     * Создает текстуры из пиксельных данных
     */
    private void createPixmapTextures() {
        // Белый пиксель для кнопок и фонов
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white-pixel", new Texture(pixmap));
        
        // Чекбокс (пустой)
        Pixmap checkboxPixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        checkboxPixmap.setColor(Color.WHITE);
        checkboxPixmap.fill();
        Texture checkboxTexture = new Texture(checkboxPixmap);
        skin.add("checkbox", checkboxTexture);
        
        // Чекбокс (отмеченный)
        Pixmap checkboxCheckedPixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        checkboxCheckedPixmap.setColor(Color.BLUE);
        checkboxCheckedPixmap.fill();
        Texture checkboxCheckedTexture = new Texture(checkboxCheckedPixmap);
        skin.add("checkbox-checked", checkboxCheckedTexture);
        
        // Ползунок (фон)
        Pixmap sliderBgPixmap = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        sliderBgPixmap.setColor(SLIDER_BG_COLOR);
        sliderBgPixmap.fill();
        Texture sliderBgTexture = new Texture(sliderBgPixmap);
        skin.add("slider-bg", sliderBgTexture);
        
        // Ползунок (ручка)
        Pixmap sliderKnobPixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        sliderKnobPixmap.setColor(SLIDER_KNOB_COLOR);
        sliderKnobPixmap.fillCircle(12, 12, 12);
        Texture sliderKnobTexture = new Texture(sliderKnobPixmap);
        skin.add("slider-knob", sliderKnobTexture);
        
        // Освобождаем ресурсы
        pixmap.dispose();
        checkboxPixmap.dispose();
        checkboxCheckedPixmap.dispose();
        sliderBgPixmap.dispose();
        sliderKnobPixmap.dispose();
    }
    
    /**
     * Создает стиль для кнопок
     */
    private void createButtonStyle() {
        TextButtonStyle textButtonStyle = new TextButtonStyle();
        textButtonStyle.font = skin.getFont("default-font");
        textButtonStyle.fontColor = new Color(1, 1, 1, 1);
        textButtonStyle.downFontColor = new Color(0.9f, 0.9f, 0.9f, 1);
        textButtonStyle.overFontColor = new Color(0.8f, 0.8f, 1, 1);
        textButtonStyle.disabledFontColor = skin.getColor("gray");
        
        textButtonStyle.up = skin.newDrawable("white-pixel", new Color(0.2f, 0.3f, 0.5f, 0.8f));
        textButtonStyle.down = skin.newDrawable("white-pixel", new Color(0.1f, 0.2f, 0.4f, 0.9f));
        textButtonStyle.over = skin.newDrawable("white-pixel", new Color(0.3f, 0.4f, 0.6f, 0.8f));
        
        skin.add("default", textButtonStyle);
    }
    
    /**
     * Создает стиль для меток
     */
    private void createLabelStyle() {
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = skin.getColor("white");
        skin.add("default", labelStyle);
    }
    
    /**
     * Создает стиль для чекбоксов
     */
    private void createCheckBoxStyle() {
        CheckBoxStyle checkBoxStyle = new CheckBoxStyle();
        checkBoxStyle.checkboxOn = skin.newDrawable("checkbox-checked");
        checkBoxStyle.checkboxOff = skin.newDrawable("checkbox");
        checkBoxStyle.font = skin.getFont("default-font");
        checkBoxStyle.fontColor = skin.getColor("white");
        skin.add("default", checkBoxStyle);
    }
    
    /**
     * Создает стиль для ползунков
     */
    private void createSliderStyle() {
        SliderStyle sliderStyle = new SliderStyle();
        sliderStyle.background = skin.newDrawable("slider-bg");
        sliderStyle.knob = skin.newDrawable("slider-knob");
        skin.add("default-horizontal", sliderStyle);
    }
    
    /**
     * Создает пользовательский интерфейс экрана настроек
     */
    private void createUI() {
        // Основная таблица для размещения UI элементов
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        
        // Создаем кнопку возврата в главное меню
        TextButton backButton = new TextButton("НАЗАД", skin);
        
        // Создаем элементы управления звуком
        createSoundControls();
        
        // Добавляем отступ сверху для заголовка
        mainTable.add().height(HEADER_HEIGHT);
        mainTable.row();
        
        // Секция управления музыкой
        Table musicSection = new Table();
        musicSection.add(musicCheckbox).width(CHECKBOX_WIDTH).height(CHECKBOX_HEIGHT).left().padBottom(ELEMENT_PADDING);
        musicSection.row();
        
        Table musicSliderSection = new Table();
        musicSliderSection.add(new Label("Громкость:", skin)).padRight(ELEMENT_PADDING);
        musicSliderSection.add(musicVolumeSlider).width(SLIDER_WIDTH - 120);
        musicSliderSection.add(musicVolumeLabel).width(40).padLeft(ELEMENT_PADDING);
        
        musicSection.add(musicSliderSection).padLeft(20);
        mainTable.add(musicSection).width(CHECKBOX_WIDTH).padBottom(SECTION_SPACING);
        mainTable.row();
        
        // Секция управления звуковыми эффектами
        Table sfxSection = new Table();
        sfxSection.add(sfxCheckbox).width(CHECKBOX_WIDTH).height(CHECKBOX_HEIGHT).left().padBottom(ELEMENT_PADDING);
        sfxSection.row();
        
        Table sfxSliderSection = new Table();
        sfxSliderSection.add(new Label("Громкость:", skin)).padRight(ELEMENT_PADDING);
        sfxSliderSection.add(sfxVolumeSlider).width(SLIDER_WIDTH - 120);
        sfxSliderSection.add(sfxVolumeLabel).width(40).padLeft(ELEMENT_PADDING);
        
        sfxSection.add(sfxSliderSection).padLeft(20);
        mainTable.add(sfxSection).width(CHECKBOX_WIDTH).padBottom(SECTION_SPACING);
        mainTable.row();
        
        // Добавляем отступ перед кнопкой возврата
        mainTable.add().height(100);
        mainTable.row();
        
        // Добавляем кнопку возврата
        mainTable.add(backButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).pad(ELEMENT_PADDING);
        
        // Обработчик для кнопки возврата
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        
        // Добавляем таблицу на сцену
        stage.addActor(mainTable);
    }
    
    /**
     * Создает элементы управления звуком
     */
    private void createSoundControls() {
        // Создаем чекбоксы для настроек звука
        musicCheckbox = new CheckBox(" МУЗЫКА", skin);
        sfxCheckbox = new CheckBox(" SFX-ЗВУКИ", skin);
        
        // Устанавливаем начальные значения чекбоксов
        musicCheckbox.setChecked(game.soundManager.isMusicEnabled());
        sfxCheckbox.setChecked(game.soundManager.isSfxEnabled());
        
        // Создаем ползунки громкости
        musicVolumeSlider = new Slider(0, 100, 1, false, skin, "default-horizontal");
        sfxVolumeSlider = new Slider(0, 100, 1, false, skin, "default-horizontal");
        
        // Устанавливаем начальные значения ползунков
        musicVolumeSlider.setValue(game.soundManager.getMusicVolume() * 100);
        sfxVolumeSlider.setValue(game.soundManager.getSfxVolume() * 100);
        
        // Создаем метки для отображения значений громкости
        musicVolumeLabel = new Label(String.valueOf((int)musicVolumeSlider.getValue()), skin);
        sfxVolumeLabel = new Label(String.valueOf((int)sfxVolumeSlider.getValue()), skin);
        
        // Добавляем обработчики событий на чекбоксы
        musicCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = musicCheckbox.isChecked();
                game.soundManager.setMusicEnabled(enabled);
                musicVolumeSlider.setDisabled(!enabled);
            }
        });
        
        sfxCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = sfxCheckbox.isChecked();
                game.soundManager.setSfxEnabled(enabled);
                sfxVolumeSlider.setDisabled(!enabled);
            }
        });
        
        // Добавляем обработчики событий на ползунки
        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = musicVolumeSlider.getValue() / 100f;
                game.soundManager.setMusicVolume(value);
                musicVolumeLabel.setText(String.valueOf((int)musicVolumeSlider.getValue()));
            }
        });
        
        sfxVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = sfxVolumeSlider.getValue() / 100f;
                game.soundManager.setSfxVolume(value);
                sfxVolumeLabel.setText(String.valueOf((int)sfxVolumeSlider.getValue()));
            }
        });
        
        // Устанавливаем начальное состояние ползунков в зависимости от чекбоксов
        musicVolumeSlider.setDisabled(!musicCheckbox.isChecked());
        sfxVolumeSlider.setDisabled(!sfxCheckbox.isChecked());
    }

    @Override
    public void render(float delta) {
        // Очищаем экран
        ScreenUtils.clear(BACKGROUND_COLOR);
        
        // Обновляем камеру
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        // Отрисовываем фон и заголовок
        game.batch.begin();
        
        // Растягиваем фон на весь экран
        game.batch.draw(backgroundImage, 0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // Рисуем заголовок
        BitmapFont titleFont = game.fontManager.getTitleFont();
        titleFont.setColor(TITLE_COLOR);
        titleFont.draw(
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
            // Удаляем шрифты из скина, чтобы они не были удалены
            skin.remove("default-font", BitmapFont.class);
            skin.dispose();
        }
    }
} 