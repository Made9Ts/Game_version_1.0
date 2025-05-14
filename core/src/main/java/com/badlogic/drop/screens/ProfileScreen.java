package com.badlogic.drop.screens;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.drop.SpaceCourierGame.GoogleAuthListener;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Экран профиля пользователя с возможностью авторизации через Google
 * и просмотра достижений. Адаптирован для различных размеров экрана.
 */
public class ProfileScreen implements Screen, GoogleAuthListener {
    // Базовые константы для виртуального разрешения
    private static final float BASE_WIDTH = 720;
    private static final float BASE_HEIGHT = 1560;
    
    // Множители для размеров UI на различных типах устройств
    private static final float PHONE_SCALE = 0.85f;
    private static final float SMALL_TABLET_SCALE = 1.1f;
    private static final float LARGE_TABLET_SCALE = 1.35f;
    private static final float ULTRA_PHONE_SCALE = 1.0f;
    
    // Константы разрешений для определения устройств
    private static final int S24_ULTRA_WIDTH = 1440;
    private static final int S24_ULTRA_HEIGHT = 3120;
    
    // Константы для разметки, которые будут масштабироваться
    private float buttonWidth = 400;
    private float buttonHeight = 90;
    private float padding = 15;
    private float sectionSpacing = 30;
    private float titlePadding = 60;
    
    private final SpaceCourierGame game;
    private OrthographicCamera camera;
    private Stage stage;
    private Skin skin;
    private Texture backgroundImage;
    private GlyphLayout titleLayout;
    private TextButton googleAuthButton;
    private Table profileTable;
    private Table achievementsTable;
    private boolean isSignedIn;
    private float uiScale; // Масштаб UI в зависимости от типа устройства
    
    // Компоненты профиля
    private Label userNameLabel;
    private Label userEmailLabel;
    
    public ProfileScreen(final SpaceCourierGame game) {
        this.game = game;
        
        // Сбрасываем цвета шрифтов
        game.fontManager.resetFontColors();
        
        // Определяем масштаб UI в зависимости от размера экрана устройства
        determineUiScale();
        
        // Применяем масштаб к константам UI
        applyUiScale();
        
        // Инициализируем камеру
        camera = new OrthographicCamera();
        camera.setToOrtho(false, BASE_WIDTH, BASE_HEIGHT);
        
        // Загружаем фон
        backgroundImage = new Texture("background.png");
        backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Создаем сцену с адаптивным viewport
        stage = new Stage(new ExtendViewport(BASE_WIDTH, BASE_HEIGHT, camera));
        Gdx.input.setInputProcessor(stage);
        
        // Устанавливаем размер текста заголовка
        titleLayout = new GlyphLayout();
        titleLayout.setText(game.fontManager.getTitleFont(), "ПРОФИЛЬ");
        
        // Регистрируемся как слушатель событий аутентификации
        game.addGoogleAuthListener(this);
        
        // Проверяем текущее состояние авторизации
        isSignedIn = game.isGoogleSignedIn();
        
        // Создаем скин для UI
        createSkin();
        
        // Создаем интерфейс
        createUI();
    }
    
    /**
     * Определяет масштаб UI на основе размера экрана устройства
     */
    private void determineUiScale() {
        float screenRatio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        
        // Проверяем, является ли устройство Samsung S24 Ultra
        boolean isS24Ultra = (screenWidth == S24_ULTRA_WIDTH && screenHeight == S24_ULTRA_HEIGHT) || 
                             (screenHeight == S24_ULTRA_WIDTH && screenWidth == S24_ULTRA_HEIGHT);
        
        if (isS24Ultra) {
            // Специальный масштаб для Samsung S24 Ultra
            uiScale = ULTRA_PHONE_SCALE;
            Gdx.app.log("ProfileScreen", "Определен Samsung S24 Ultra, применяем специальный масштаб: " + uiScale);
        }
        // Определяем тип устройства на основе ширины экрана и соотношения сторон
        else if (screenWidth >= 1200 || (screenWidth >= 800 && screenRatio >= 1.5f)) {
            // Большой планшет или широкое устройство
            uiScale = LARGE_TABLET_SCALE;
        } else if (screenWidth >= 800 || screenRatio >= 1.3f) {
            // Маленький планшет
            uiScale = SMALL_TABLET_SCALE;
        } else {
            // Телефон
            uiScale = PHONE_SCALE;
        }
        
        Gdx.app.log("ProfileScreen", "Screen size: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight() 
                + ", ratio: " + screenRatio + ", scale: " + uiScale);
    }
    
    /**
     * Применяет определенный масштаб к UI элементам
     */
    private void applyUiScale() {
        // Адаптируем размеры UI под текущий масштаб
        buttonWidth *= uiScale;
        buttonHeight *= uiScale;
        padding *= uiScale;
        sectionSpacing *= uiScale;
        titlePadding *= uiScale;
    }
    
    private void createSkin() {
        skin = new Skin();
        
        // Добавляем шрифт в скин с уменьшенным размером
        BitmapFont uiFont = game.fontManager.getUIFont();
        uiFont.getData().setScale(0.85f);
        uiFont.setColor(Color.WHITE);
        skin.add("default-font", uiFont);
        
        // Добавляем цвета
        skin.add("white", new Color(1, 1, 1, 1));
        skin.add("black", new Color(0, 0, 0, 1));
        skin.add("gray", new Color(0.5f, 0.5f, 0.5f, 1));
        skin.add("blue", new Color(0.2f, 0.4f, 0.8f, 1));
        skin.add("green", new Color(0.2f, 0.8f, 0.2f, 1));
        skin.add("gold", new Color(1f, 0.9f, 0.2f, 1));
        skin.add("light-blue", new Color(0.4f, 0.6f, 1.0f, 1)); // Добавляем светло-синий для фона заблокированных достижений
        skin.add("dark-green", new Color(0.1f, 0.5f, 0.1f, 1)); // Темно-зеленый для фона разблокированных достижений
        
        // Добавляем пиксель для фонов
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white-pixel", new Texture(pixmap));
        pixmap.dispose();
        
        // Стиль кнопки
        TextButtonStyle textButtonStyle = new TextButtonStyle();
        textButtonStyle.font = skin.getFont("default-font");
        textButtonStyle.fontColor = skin.getColor("white");
        textButtonStyle.downFontColor = new Color(0.9f, 0.9f, 0.9f, 1);
        textButtonStyle.overFontColor = new Color(0.8f, 0.8f, 1, 1);
        textButtonStyle.up = skin.newDrawable("white-pixel", new Color(0.2f, 0.3f, 0.5f, 0.8f));
        textButtonStyle.down = skin.newDrawable("white-pixel", new Color(0.1f, 0.2f, 0.4f, 0.9f));
        textButtonStyle.over = skin.newDrawable("white-pixel", new Color(0.3f, 0.4f, 0.6f, 0.8f));
        skin.add("default", textButtonStyle);
        
        // Стиль метки
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = skin.getColor("white");
        skin.add("default", labelStyle);
        
        // Стиль метки для заголовков
        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        titleLabelStyle.font = game.fontManager.getUIFont();
        titleLabelStyle.fontColor = skin.getColor("gold");
        skin.add("title", titleLabelStyle);
        
        // Стиль метки для разблокированных достижений
        Label.LabelStyle achievementLabelStyle = new Label.LabelStyle();
        achievementLabelStyle.font = game.fontManager.getUIFont();
        achievementLabelStyle.fontColor = skin.getColor("green");
        skin.add("achievement", achievementLabelStyle);
        
        // Стиль для заблокированных достижений
        Label.LabelStyle lockedLabelStyle = new Label.LabelStyle();
        lockedLabelStyle.font = game.fontManager.getUIFont();
        lockedLabelStyle.fontColor = skin.getColor("gray");
        skin.add("locked", lockedLabelStyle);
        
        // Стиль для ScrollPane
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = skin.newDrawable("white-pixel", new Color(0.1f, 0.1f, 0.2f, 0.5f));
        scrollPaneStyle.vScroll = skin.newDrawable("white-pixel", new Color(0.3f, 0.3f, 0.6f, 0.7f));
        scrollPaneStyle.vScrollKnob = skin.newDrawable("white-pixel", new Color(0.4f, 0.4f, 0.8f, 0.7f));
        skin.add("default", scrollPaneStyle);
    }
    
    private void createUI() {
        // Главная таблица для всего экрана с адаптивной компоновкой
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top();
        
        // Создаем кнопку назад
        TextButton backButton = new TextButton("НАЗАД", skin);
        
        // Кнопка для Google авторизации
        googleAuthButton = new TextButton(isSignedIn ? "ВЫЙТИ" : "ВОЙТИ ЧЕРЕЗ GOOGLE", skin);
        
        // Создаем таблицу профиля
        profileTable = new Table();
        
        // Создаем таблицу достижений
        achievementsTable = new Table();
        achievementsTable.top().left();
        
        // Добавляем заголовок профиля
        Label profileHeaderLabel = new Label("ИНФОРМАЦИЯ О ПРОФИЛЕ", skin, "title");
        
        // Создаем метки для информации о пользователе
        userNameLabel = new Label(isSignedIn ? "Имя: " + game.getGoogleUserName() : "Войдите, чтобы увидеть данные профиля", skin);
        userEmailLabel = new Label(isSignedIn ? "Email: " + game.getGoogleUserEmail() : "", skin);
        
        // Проверка, является ли устройство Samsung S24 Ultra для дополнительной адаптации
        boolean isS24Ultra = Gdx.graphics.getWidth() == S24_ULTRA_WIDTH || Gdx.graphics.getHeight() == S24_ULTRA_WIDTH;
        float buttonWidthAdjusted = isS24Ultra ? buttonWidth * 1.05f : buttonWidth;
        
        // Обновляем секцию профиля с учетом масштабирования
        profileTable.add(profileHeaderLabel).pad(padding).colspan(2).left();
        profileTable.row();
        profileTable.add(userNameLabel).pad(padding).left();
        profileTable.row();
        if (isSignedIn) {
            profileTable.add(userEmailLabel).pad(padding).left();
            profileTable.row();
        }
        profileTable.add(googleAuthButton).width(buttonWidthAdjusted).height(buttonHeight).pad(padding);
        profileTable.row();
        
        // Добавляем заголовок достижений
        Label achievementsHeaderLabel = new Label("ДОСТИЖЕНИЯ", skin, "title");
        achievementsTable.add(achievementsHeaderLabel).pad(padding).colspan(1).left();
        achievementsTable.row();
        
        // Получаем достижения из системы достижений
        updateAchievementsList();
        
        // Создаем фон для ScrollPane
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0.1f, 0.1f, 0.2f, 0.5f);
        bgPixmap.fill();
        bgPixmap.dispose();
        
        // Создаем скролл-панель для достижений
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = skin.newDrawable("white-pixel", new Color(0.1f, 0.1f, 0.2f, 0.5f));
        scrollStyle.vScroll = skin.newDrawable("white-pixel", new Color(0.3f, 0.3f, 0.6f, 0.7f));
        scrollStyle.vScrollKnob = skin.newDrawable("white-pixel", new Color(0.4f, 0.4f, 0.8f, 0.7f));
        ScrollPane achievementsScrollPane = new ScrollPane(achievementsTable, scrollStyle);
        achievementsScrollPane.setFadeScrollBars(false);
        achievementsScrollPane.setScrollingDisabled(true, false);
        
        // Адаптивная компоновка UI с учетом типа устройства
        float buttonBackWidth = 180 * uiScale;
        float buttonBackHeight = 65 * uiScale;
        float contentWidth = BASE_WIDTH - (padding * 2);
        float edgeMargin = 15; // Отступ от края экрана в 15 пикселей
        float bottomOffset = 120; // Увеличиваем подъем нижней границы окна достижений со значения 50 до 120 пикселей
        
        // Отступ для кнопки назад (10 пикселей)
        float cornerPadding = 10;
        
        // Учитываем ориентацию экрана для планшетов
        if (uiScale > PHONE_SCALE && Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
            // Горизонтальная ориентация на планшете - разделяем экран на две части
            // Добавляем кнопку назад в левый верхний угол с отступами 10 пикселей
            mainTable.add(backButton).width(buttonBackWidth).height(buttonBackHeight).pad(cornerPadding).left().colspan(2);
            mainTable.row();
            
            // Добавляем отступ между кнопкой и контентом
            mainTable.add().height(padding * 2).colspan(2);
            mainTable.row();
            
            // Добавляем профиль слева
            mainTable.add(profileTable).width(contentWidth * 0.45f).pad(padding).top();
            
            // Добавляем достижения справа (с отступом снизу точно 15 пикселей и значительным подъемом)
            float achievementsHeight = BASE_HEIGHT * 0.7f - cornerPadding - padding * 2 - edgeMargin;
            mainTable.add(achievementsScrollPane).width(contentWidth * 0.55f)
                .height(achievementsHeight)
                .pad(padding, padding, edgeMargin, padding).top().fill();
        } else {
            // Вертикальная ориентация или телефон - стандартная компоновка
            // Добавляем кнопку назад в левый верхний угол с отступами 10 пикселей
            mainTable.add(backButton).width(buttonBackWidth).height(buttonBackHeight).pad(cornerPadding).left();
            mainTable.row();
            
            // Добавляем отступ между кнопкой и контентом - уменьшаем для более компактного вида
            mainTable.add().height(padding);
            mainTable.row();
            
            mainTable.add(profileTable).width(contentWidth).pad(padding * 0.7f); // Уменьшаем отступы
            mainTable.row();
            
            // Вычисляем оставшуюся высоту экрана для достижений
            float profileHeight = buttonBackHeight + cornerPadding * 2 + padding * 1.5f; // Уменьшаем отступы
            if (isSignedIn) {
                profileHeight += userNameLabel.getHeight() + userEmailLabel.getHeight() + googleAuthButton.getHeight() + padding * 4;
            } else {
                profileHeight += userNameLabel.getHeight() + googleAuthButton.getHeight() + padding * 3;
            }
            
            // Проверяем высоту заголовка профиля
            if (profileHeaderLabel != null) {
                profileHeight += profileHeaderLabel.getHeight() + padding * 0.7f;
            }
            
            // Растягиваем окно достижений на оставшуюся часть экрана минус отступы снизу и подъем
            float achievementsHeightValue = BASE_HEIGHT - profileHeight - edgeMargin - bottomOffset;
            
            // Ограничиваем минимальную высоту окна достижений
            achievementsHeightValue = Math.max(achievementsHeightValue, BASE_HEIGHT * 0.25f);
            
            // Устанавливаем точно 15 пикселей для нижнего отступа
            mainTable.add(achievementsScrollPane).width(contentWidth)
                .height(achievementsHeightValue)
                .pad(padding * 0.5f, padding, edgeMargin, padding).expand().fill(); // Уменьшаем верхний отступ для плотности
        }
        
        // Обработчик для кнопки назад
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        
        // Обработчик для кнопки Google авторизации
        googleAuthButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isSignedIn) {
                    // Выходим из аккаунта
                    game.signOutFromGoogle();
                    isSignedIn = false;
                    googleAuthButton.setText("ВОЙТИ ЧЕРЕЗ GOOGLE");
                    updateProfileInfo();
                } else {
                    // Начинаем процесс входа
                    game.signInWithGoogle();
                    // Кнопка будет обновлена после успешной авторизации
                }
            }
        });
        
        // Добавляем главную таблицу на сцену
        stage.addActor(mainTable);
    }
    
    /**
     * Обновляет информацию о профиле пользователя
     */
    private void updateProfileInfo() {
        // Защищаемся от возможных null-указателей
        if (userNameLabel == null || (isSignedIn && userEmailLabel == null)) {
            return;
        }
        
        if (isSignedIn && game.getGoogleUserName() != null) {
            userNameLabel.setText("Имя: " + game.getGoogleUserName());
            userEmailLabel.setText("Email: " + game.getGoogleUserEmail());
            userEmailLabel.setVisible(true);
        } else {
            userNameLabel.setText("Войдите, чтобы увидеть данные профиля");
            if (userEmailLabel != null) {
                userEmailLabel.setVisible(false);
            }
        }
    }
    
    /**
     * Обновляет список достижений
     */
    private void updateAchievementsList() {
        // Защищаемся от возможных null-указателей
        if (achievementsTable == null || skin == null) {
            return;
        }
        
        // Очищаем таблицу достижений, кроме заголовка
        int childrenCount = achievementsTable.getChildren().size;
        // Сохраняем заголовок
        if (childrenCount > 1) {
            // Удаляем все дочерние элементы, кроме заголовка
            achievementsTable.clearChildren();
            // Восстанавливаем заголовок
            Label achievementsHeaderLabel = new Label("ДОСТИЖЕНИЯ", skin, "title");
            achievementsTable.add(achievementsHeaderLabel).pad(padding).colspan(1).left(); // colspan=1, теперь всего одна колонка
            achievementsTable.row();
        }
        
        // Проверяем, инициализирована ли система достижений
        if (game.achievementSystem == null) {
            // Система достижений ещё не инициализирована, добавляем сообщение об этом
            Label noAchievementsLabel = new Label("Система достижений загружается...", skin);
            achievementsTable.add(noAchievementsLabel).pad(padding).left();
            return;
        }
        
        // Проверяем, идет ли загрузка достижений
        if (game.achievementSystem.isLoading()) {
            Label loadingLabel = new Label("Загрузка достижений...", skin);
            achievementsTable.add(loadingLabel).pad(padding).left();
            return;
        }
        
        // Получаем все достижения
        ObjectMap<String, AchievementSystem.Achievement> allAchievements = game.achievementSystem.getAllAchievements();
        
        // Проверяем, является ли устройство Samsung S24 Ultra
        boolean isS24Ultra = Gdx.graphics.getWidth() == S24_ULTRA_WIDTH || Gdx.graphics.getHeight() == S24_ULTRA_WIDTH;
        
        // Адаптивная ширина для всей строки
        float columnPadding = padding * 0.7f; // Отступ между элементами
        float totalWidth = BASE_WIDTH - (padding * 2);
        
        // Не добавляем заголовки колонок, так как теперь одна колонка с вложенными строками
        
        // Добавляем достижения в таблицу
        if (allAchievements != null && allAchievements.size > 0) {
            for (ObjectMap.Entry<String, AchievementSystem.Achievement> entry : allAchievements.entries()) {
                if (entry == null || entry.value == null) continue;
                
                AchievementSystem.Achievement achievement = entry.value;
                boolean isUnlocked = achievement.unlocked;
                
                // Создаем таблицу для строки достижения с фоном
                Table achievementRow = new Table();
                Color bgColor = isUnlocked ? 
                    new Color(0.1f, 0.5f, 0.1f, 0.4f) : // Зеленый фон для разблокированных
                    new Color(0.2f, 0.2f, 0.3f, 0.3f);  // Серый фон для заблокированных
                achievementRow.setBackground(skin.newDrawable("white-pixel", bgColor));
                
                // Статус отображаем в названии (перед ним)
                String statusText = isUnlocked ? "✓ " : "✗ ";
                
                // Название с индикатором статуса
                Label nameLabel = new Label(statusText + achievement.title, skin, isUnlocked ? "achievement" : "locked");
                
                // Описание достижения
                Label descLabel = new Label(achievement.description, skin);
                
                // Для лучшей читаемости на малых экранах применяем перенос текста
                descLabel.setWrap(true);
                
                // Создаем вертикальное размещение (название сверху, описание снизу)
                // Название занимает всю ширину и выравнивается по левому краю
                achievementRow.add(nameLabel).width(totalWidth - columnPadding * 2).pad(columnPadding, columnPadding, columnPadding/2, columnPadding).left().fillX();
                achievementRow.row();
                
                // Добавляем тонкую разделительную линию между названием и описанием
                Table separator = new Table();
                separator.setBackground(skin.newDrawable("white-pixel", new Color(1, 1, 1, 0.2f)));
                achievementRow.add(separator).height(1).pad(0, columnPadding * 2, columnPadding/2, columnPadding * 2).fillX();
                achievementRow.row();
                
                // Описание занимает всю ширину и выравнивается по левому краю
                achievementRow.add(descLabel).width(totalWidth - columnPadding * 2).pad(columnPadding/2, columnPadding, columnPadding, columnPadding).left().fillX();
                
                // Добавляем строку достижения в таблицу достижений
                achievementsTable.add(achievementRow).fillX().pad(columnPadding / 2);
                achievementsTable.row();
            }
        } else {
            // Если нет доступных достижений, показываем соответствующее сообщение
            Label noAchievementsLabel = new Label("Нет доступных достижений", skin);
            achievementsTable.add(noAchievementsLabel).pad(padding).left();
        }
    }

    @Override
    public void render(float delta) {
        // Очищаем экран
        ScreenUtils.clear(0.05f, 0.05f, 0.2f, 1);
        
        // Обновляем камеру
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        // Проверяем, является ли устройство Samsung S24 Ultra
        boolean isS24Ultra = Gdx.graphics.getWidth() == S24_ULTRA_WIDTH || Gdx.graphics.getHeight() == S24_ULTRA_WIDTH;
        
        // Отрисовываем фон
        game.batch.begin();
        
        // Фон с учетом orientation и типа устройства
        if (isS24Ultra && Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
            // Специальная отрисовка для Samsung S24 Ultra в горизонтальной ориентации
            float aspectRatio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
            float bgWidth = BASE_HEIGHT * aspectRatio * 1.05f; // Увеличиваем немного для лучшего покрытия
            float bgX = (BASE_WIDTH - bgWidth) / 2;
            game.batch.draw(backgroundImage, bgX, 0, bgWidth, BASE_HEIGHT);
        } else if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight() && uiScale > PHONE_SCALE) {
            // Для горизонтальной ориентации планшетов
            float aspectRatio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
            float bgWidth = BASE_HEIGHT * aspectRatio;
            float bgX = (BASE_WIDTH - bgWidth) / 2;
            game.batch.draw(backgroundImage, bgX, 0, bgWidth, BASE_HEIGHT);
        } else {
            // Для вертикальной ориентации
            game.batch.draw(backgroundImage, 0, 0, BASE_WIDTH, BASE_HEIGHT);
        }
        
        game.batch.end();
        
        // Проверяем, не началась ли загрузка достижений, и запускаем опрос
        if (game.achievementSystem != null && game.achievementSystem.isLoading() && !isPolling) {
            startLoadingPolling();
        }
        
        // Отрисовываем UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Сохраняем текущее состояние UI
        boolean wasSignedIn = isSignedIn;
        
        // Определяем новый масштаб UI
        determineUiScale();
        applyUiScale();
        
        // Обновляем viewport с учетом особенностей экрана Samsung S24 Ultra
        float screenRatio = (float) width / height;
        stage.getViewport().update(width, height, true);
        
        // Для Samsung S24 Ultra и подобных устройств с высоким соотношением сторон
        // адаптируем положение элементов
        boolean isUltraPhone = (width == S24_ULTRA_WIDTH && height == S24_ULTRA_HEIGHT) || 
                              (height == S24_ULTRA_WIDTH && width == S24_ULTRA_HEIGHT);
        
        // Если размер экрана изменился значительно или это Samsung S24 Ultra, пересоздаем UI для адаптации
        if (Math.abs(stage.getViewport().getWorldWidth() / stage.getViewport().getWorldHeight() - 
                screenRatio) > 0.2f || isUltraPhone) {
            
            // Сохраняем состояние
            isSignedIn = wasSignedIn;
            
            // Удаляем существующие акторы
            stage.clear();
            
            // Пересоздаем UI с новыми пропорциями
            createUI();
        }
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
        // Ничего не делаем при паузе
    }

    @Override
    public void resume() {
        // Ничего не делаем при возобновлении
    }

    @Override
    public void dispose() {
        // Останавливаем опрос загрузки
        isPolling = false;
        
        // Отписываемся от событий
        game.removeGoogleAuthListener(this);
        
        // Освобождаем ресурсы
        stage.dispose();
        backgroundImage.dispose();
        
        if (skin != null) {
            // Удаляем шрифты из скина для предотвращения их освобождения
            skin.remove("default-font", BitmapFont.class);
            skin.dispose();
        }
    }
    
    // --- Реализация интерфейса GoogleAuthListener ---
    
    @Override
    public void onGoogleSignInSuccess(String userName, String email, String userId) {
        // Обеспечиваем выполнение обновления UI только в главном потоке рендеринга
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Обновляем UI после успешной авторизации
                isSignedIn = true;
                if (googleAuthButton != null) {
                    googleAuthButton.setText("ВЫЙТИ");
                }
                // Обновляем информацию профиля
                updateProfileInfo();
                
                // Обновляем список достижений после входа, когда система достижений должна быть уже инициализирована
                if (game.achievementSystem != null) {
                    updateAchievementsList();
                    
                    // Запускаем периодическое обновление, если достижения загружаются
                    if (game.achievementSystem.isLoading()) {
                        startLoadingPolling();
                    }
                }
            }
        });
    }
    
    // Периодическое обновление при загрузке
    private boolean isPolling = false;
    
    /**
     * Запускает периодическое обновление списка достижений до окончания загрузки
     */
    private void startLoadingPolling() {
        if (isPolling) return;
        
        isPolling = true;
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Проверяем состояние загрузки каждые 500 мс
                    while (isPolling && game.achievementSystem != null && game.achievementSystem.isLoading()) {
                        Thread.sleep(500);
                    }
                    
                    // Когда загрузка завершена, обновляем UI в главном потоке
                    if (isPolling) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                if (game.achievementSystem != null) {
                                    updateAchievementsList();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Gdx.app.error("ProfileScreen", "Ошибка при ожидании загрузки достижений", e);
                } finally {
                    isPolling = false;
                }
            }
        }).start();
    }
    
    @Override
    public void onGoogleSignInFailure(String errorMessage) {
        // При ошибке авторизации просто не меняем состояние
        isSignedIn = false;
    }
} 