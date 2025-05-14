package com.badlogic.drop;

import com.badlogic.drop.screens.MainMenuScreen;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.drop.util.FontManager;
import com.badlogic.drop.util.GoogleAuthInterface;
import com.badlogic.drop.util.SoundManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

/**
 * Основной класс игры Space Courier, адаптированный для Samsung Galaxy S24 Ultra.
 * Управляет жизненным циклом игры и основными ресурсами.
 */
public class SpaceCourierGame extends Game {
	// Константы разрешения для Samsung Galaxy S24 Ultra
	public static final float GAME_WIDTH = 720; // Виртуальная ширина (оптимизирована для производительности)
	public static final float GAME_HEIGHT = 1560; // Виртуальная высота с сохранением пропорций
	
	// Константы для элементов интерфейса
	public static final float UI_PADDING = 20f;
	public static final float UI_BUTTON_WIDTH = 450f;
	public static final float UI_BUTTON_HEIGHT = 120f;
	
	// Менеджеры ресурсов и систем
	public SpriteBatch batch;
	public FontManager fontManager;
	public AchievementSystem achievementSystem;
	public SoundManager soundManager;
	
	// Ресурсы для оптимизации производительности
	private TextureAtlas gameAtlas;
	
	// Интерфейс для работы с Google Auth
	private GoogleAuthInterface googleAuthInterface;
	
	// Данные пользователя Google
	private String googleUserName;
	private String googleUserEmail;
	private boolean isGoogleSignedIn;
	
	// Слушатели аутентификации
	private Array<GoogleAuthListener> authListeners;
	
	/**
	 * Интерфейс для получения уведомлений о событиях аутентификации Google
	 */
	public interface GoogleAuthListener {
		void onGoogleSignInSuccess(String userName, String email);
		void onGoogleSignInFailure(String errorMessage);
	}

	@Override
	public void create() {
		initializeResources();
		setScreen(new MainMenuScreen(this));
	}
	
	/**
	 * Инициализирует все необходимые ресурсы и системы игры.
	 * Создает менеджеры ресурсов и загружает необходимые ассеты.
	 */
	private void initializeResources() {
		// Инициализация основных ресурсов
		batch = new SpriteBatch();
		fontManager = new FontManager();
		achievementSystem = new AchievementSystem();
		soundManager = new SoundManager();
		authListeners = new Array<>();
		
		// Инициализация атласа текстур (если используется)
		// gameAtlas = new TextureAtlas("game_textures.atlas");
	}
	
	/**
	 * Устанавливает интерфейс для аутентификации Google
	 * @param googleAuthInterface Реализация интерфейса аутентификации
	 */
	public void setGoogleAuthInterface(GoogleAuthInterface googleAuthInterface) {
		this.googleAuthInterface = googleAuthInterface;
		// Проверяем текущий статус при установке интерфейса
		if (googleAuthInterface != null && googleAuthInterface.isSignedIn()) {
			this.isGoogleSignedIn = true;
			this.googleUserName = googleAuthInterface.getUserName();
			this.googleUserEmail = googleAuthInterface.getUserEmail();
		}
	}
	
	/**
	 * Добавляет слушателя событий аутентификации Google
	 * @param listener Слушатель для добавления
	 */
	public void addGoogleAuthListener(GoogleAuthListener listener) {
		if (listener != null && !authListeners.contains(listener, true)) {
			authListeners.add(listener);
		}
	}
	
	/**
	 * Удаляет слушателя событий аутентификации Google
	 * @param listener Слушатель для удаления
	 */
	public void removeGoogleAuthListener(GoogleAuthListener listener) {
		if (listener != null) {
			authListeners.removeValue(listener, true);
		}
	}
	
	/**
	 * Начинает процесс входа через Google аккаунт
	 */
	public void signInWithGoogle() {
		if (googleAuthInterface != null) {
			googleAuthInterface.signIn();
		} else {
			Gdx.app.log("SpaceCourierGame", "GoogleAuthInterface не установлен");
		}
	}
	
	/**
	 * Выполняет выход из аккаунта Google
	 */
	public void signOutFromGoogle() {
		if (googleAuthInterface != null) {
			googleAuthInterface.signOut();
			isGoogleSignedIn = false;
			googleUserName = null;
			googleUserEmail = null;
		}
	}
	
	/**
	 * Проверяет, вошел ли пользователь через Google
	 * @return true, если пользователь авторизован
	 */
	public boolean isGoogleSignedIn() {
		return googleAuthInterface != null && googleAuthInterface.isSignedIn();
	}
	
	/**
	 * Возвращает имя пользователя Google
	 * @return Имя пользователя или null
	 */
	public String getGoogleUserName() {
		return googleUserName;
	}
	
	/**
	 * Возвращает email пользователя Google
	 * @return Email пользователя или null
	 */
	public String getGoogleUserEmail() {
		return googleUserEmail;
	}
	
	/**
	 * Вызывается при успешном входе в аккаунт Google
	 * @param userName Имя пользователя
	 * @param email Email пользователя
	 */
	public void onGoogleSignInSuccess(String userName, String email) {
		this.googleUserName = userName;
		this.googleUserEmail = email;
		this.isGoogleSignedIn = true;
		
		// Уведомляем всех слушателей
		for (GoogleAuthListener listener : authListeners) {
			listener.onGoogleSignInSuccess(userName, email);
		}
	}
	
	/**
	 * Вызывается при ошибке входа в аккаунт Google
	 * @param errorMessage Сообщение об ошибке
	 */
	public void onGoogleSignInFailure(String errorMessage) {
		this.isGoogleSignedIn = false;
		
		// Уведомляем всех слушателей
		for (GoogleAuthListener listener : authListeners) {
			listener.onGoogleSignInFailure(errorMessage);
		}
	}

	@Override
	public void render() {
		// Вызов метода render активного экрана
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		// Передаем событие изменения размеров активному экрану
		super.resize(width, height);
	}

	/**
	 * Пересоздает менеджер шрифтов с нуля.
	 * Используется для решения проблем с сохранением состояний шрифтов
	 * при переключении между экранами.
	 */
	public void recreateFontManager() {
		if (fontManager != null) {
			fontManager.dispose();
		}
		fontManager = new FontManager();
	}

	@Override
	public void dispose() {
		disposeResources();
		disposeCurrentScreen();
	}
	
	/**
	 * Освобождает все ресурсы, используемые игрой.
	 * Вызывается при закрытии приложения.
	 */
	private void disposeResources() {
		if (batch != null) batch.dispose();
		if (gameAtlas != null) gameAtlas.dispose();
		if (fontManager != null) fontManager.dispose();
		if (soundManager != null) soundManager.dispose();
		if (achievementSystem != null) achievementSystem.dispose();
		authListeners.clear();
	}
	
	/**
	 * Освобождает ресурсы текущего экрана.
	 */
	private void disposeCurrentScreen() {
		if (getScreen() != null) {
			getScreen().dispose();
		}
	}
} 