package com.badlogic.drop;

import com.badlogic.drop.screens.MainMenuScreen;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.drop.util.FontManager;
import com.badlogic.drop.util.GoogleAuthInterface;
import com.badlogic.drop.util.SoundManager;
import com.badlogic.drop.firebase.FirebaseInterface;
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
	
	// Интерфейс для работы с Firebase
	private FirebaseInterface firebaseInterface;
	
	// Данные пользователя Google
	private String googleUserName;
	private String googleUserEmail;
	private String googleUserId;
	private boolean isGoogleSignedIn;
	
	// Слушатели аутентификации
	private Array<GoogleAuthListener> authListeners;
	
	/**
	 * Интерфейс для получения уведомлений о событиях аутентификации Google
	 */
	public interface GoogleAuthListener {
		void onGoogleSignInSuccess(String userName, String email, String userId);
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
		soundManager = new SoundManager();
		authListeners = new Array<>();
		
		// Создаем базовую систему достижений, которая будет использоваться, 
		// пока не будет установлен Firebase
		achievementSystem = new AchievementSystem();
		Gdx.app.log("SpaceCourierGame", "Базовая система достижений инициализирована");
	}
	
	/**
	 * Устанавливает интерфейс Firebase
	 * @param firebaseInterface Реализация интерфейса Firebase
	 */
	public void setFirebaseInterface(FirebaseInterface firebaseInterface) {
		this.firebaseInterface = firebaseInterface;
		
		// Проверяем, что Gdx.app инициализирован
		if (Gdx.app == null) {
			// Не можем использовать Gdx.app.error, так как Gdx.app == null
			System.err.println("SpaceCourierGame: Gdx.app не инициализирован! Firebase будет установлен позже.");
			return;
		}
		
		// Инициализируем систему достижений с Firebase
		if (this.firebaseInterface != null) {
			// Если система уже была создана, освобождаем ресурсы
			if (achievementSystem != null) {
				achievementSystem.dispose();
			}
			// Создаем систему достижений с поддержкой Firebase
			achievementSystem = new AchievementSystem(this, firebaseInterface);
			Gdx.app.log("SpaceCourierGame", "Система достижений с Firebase инициализирована");
		} else {
			// Создаем стандартную систему достижений без Firebase
			if (achievementSystem == null) {
				achievementSystem = new AchievementSystem();
				Gdx.app.log("SpaceCourierGame", "Система достижений без Firebase инициализирована");
			}
		}
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
			this.googleUserId = googleAuthInterface.getUserId();
			
			// Если Firebase инициализирован, привязываем достижения к пользователю
			if (firebaseInterface != null && achievementSystem != null) {
				achievementSystem.setUser(googleUserId);
			}
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
			Gdx.app.log("SpaceCourierGame", "Выход пользователя: " + 
						(googleUserName != null ? googleUserName : "unknown") +
						" (userId: " + (googleUserId != null ? googleUserId : "null") + ")");
			
			// Сохраняем предыдущий userID для логирования
			final String oldUserId = googleUserId;
			
			// Убедимся, что все достижения сохранены перед выходом
			if (achievementSystem != null && isGoogleSignedIn) {
				Gdx.app.log("SpaceCourierGame", "Синхронизация достижений перед выходом");
				// Принудительная синхронизация достижений перед выходом
				achievementSystem.syncProgress();
				
				// Отвязываем пользователя от системы достижений и сбрасываем его достижения
				Gdx.app.log("SpaceCourierGame", "Отвязываем пользователя от системы достижений");
				// Устанавливаем userId как null, чтобы сбросить достижения
				achievementSystem.setUser(null);
			}
			
			// Сбрасываем данные пользователя
			googleUserName = null;
			googleUserEmail = null;
			googleUserId = null;
			isGoogleSignedIn = false;
			
			// Запускаем выход из аккаунта Google
			googleAuthInterface.signOut();
			
			Gdx.app.log("SpaceCourierGame", "Выход пользователя " + oldUserId + " выполнен успешно");
		} else {
			Gdx.app.error("SpaceCourierGame", "Невозможно выполнить выход: GoogleAuthInterface не инициализирован");
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
	 * Возвращает ID пользователя Google
	 * @return ID пользователя или null
	 */
	public String getGoogleUserId() {
		return googleUserId;
	}
	
	/**
	 * Вызывается при успешном входе в аккаунт Google
	 * @param userName Имя пользователя
	 * @param email Email пользователя
	 */
	public void onGoogleSignInSuccess(String userName, String email, String userId) {
		Gdx.app.log("SpaceCourierGame", "Успешный вход: " + userName + " (" + email + "), userId: " + userId);
		
		// Обновляем данные пользователя
		this.googleUserName = userName;
		this.googleUserEmail = email;
		this.googleUserId = userId;
		this.isGoogleSignedIn = true;
		
		// Если Firebase инициализирован, но система достижений не создана с ним,
		// пересоздаем систему достижений
		if (firebaseInterface != null) {
			boolean needReinitialization = false;
			
			// Если система не создана или создана без Firebase
			if (achievementSystem == null || !(achievementSystem instanceof AchievementSystem)) {
				needReinitialization = true;
			}
			
			if (needReinitialization) {
				Gdx.app.log("SpaceCourierGame", "Пересоздаем систему достижений с Firebase");
				if (achievementSystem != null) {
					achievementSystem.dispose();
				}
				achievementSystem = new AchievementSystem(this, firebaseInterface);
			}
			
			// Привязываем достижения к пользователю
			if (achievementSystem != null) {
				Gdx.app.log("SpaceCourierGame", "Привязываем достижения к пользователю: " + userId);
				achievementSystem.setUser(userId);
			} else {
				Gdx.app.error("SpaceCourierGame", "Не удалось инициализировать систему достижений!");
			}
		} else {
			Gdx.app.log("SpaceCourierGame", "Firebase не инициализирован, используем локальные достижения");
		}
		
		// Уведомляем всех слушателей, но в главном потоке рендеринга
		final String finalUserName = userName;
		final String finalEmail = email;
		final String finalUserId = userId;
		
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				for (GoogleAuthListener listener : authListeners) {
					if (listener != null) {
						listener.onGoogleSignInSuccess(finalUserName, finalEmail, finalUserId);
					}
				}
			}
		});
	}
	
	/**
	 * Вызывается при ошибке входа в аккаунт Google
	 * @param errorMessage Сообщение об ошибке
	 */
	public void onGoogleSignInFailure(String errorMessage) {
		this.isGoogleSignedIn = false;
		
		// Уведомляем всех слушателей, но в главном потоке рендеринга
		final String finalErrorMessage = errorMessage;
		
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				for (GoogleAuthListener listener : authListeners) {
					if (listener != null) {
						listener.onGoogleSignInFailure(finalErrorMessage);
					}
				}
			}
		});
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