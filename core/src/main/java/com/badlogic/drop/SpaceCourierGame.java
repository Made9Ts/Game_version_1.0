package com.badlogic.drop;

import com.badlogic.drop.screens.MainMenuScreen;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.drop.util.FontManager;
import com.badlogic.drop.util.SoundManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

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
		
		// Инициализация атласа текстур (если используется)
		// gameAtlas = new TextureAtlas("game_textures.atlas");
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