package com.badlogic.drop;

import com.badlogic.drop.screens.MainMenuScreen;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.drop.util.FontManager;
import com.badlogic.drop.util.SoundManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Основной класс игры, адаптированный для Samsung Galaxy S24 Ultra
 */
public class SpaceCourierGame extends Game {
	// Константы разрешения для S24 Ultra
	public static final float GAME_WIDTH = 720; // Виртуальная ширина (половина реального разрешения для производительности)
	public static final float GAME_HEIGHT = 1560; // Виртуальная высота с сохранением пропорций
	
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
	 * Инициализирует все необходимые ресурсы и системы игры
	 */
	private void initializeResources() {
		batch = new SpriteBatch();
		fontManager = new FontManager();
		achievementSystem = new AchievementSystem();
		soundManager = new SoundManager();
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
	 * Пересоздает менеджер шрифтов с нуля
	 * Используется для решения проблем с сохранением состояний шрифтов
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
	 * Освобождает все ресурсы, используемые игрой
	 */
	private void disposeResources() {
		if (batch != null) batch.dispose();
		if (gameAtlas != null) gameAtlas.dispose();
		if (fontManager != null) fontManager.dispose();
	}
	
	/**
	 * Освобождает ресурсы текущего экрана
	 */
	private void disposeCurrentScreen() {
		if (getScreen() != null) {
			getScreen().dispose();
		}
	}
} 