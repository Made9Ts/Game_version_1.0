package com.badlogic.drop;

import com.badlogic.drop.screens.MainMenuScreen;
import com.badlogic.drop.systems.AchievementSystem;
import com.badlogic.drop.util.FontManager;
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
	
	// Основной SpriteBatch для отрисовки всех объектов
	public SpriteBatch batch;
	
	// Атлас текстур (для оптимизации производительности)
	private TextureAtlas gameAtlas;
	
	// Менеджер шрифтов для улучшенного качества текста
	public FontManager fontManager;
	
	// Система достижений
	public AchievementSystem achievementSystem;

	@Override
	public void create() {
		// Инициализация основных компонентов
		batch = new SpriteBatch();
		
		// Создаем менеджер шрифтов для высокого качества текста
		fontManager = new FontManager();
		
		// Создаем систему достижений
		achievementSystem = new AchievementSystem();
		
		// Загрузка экрана меню
		setScreen(new MainMenuScreen(this));
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
		// Сначала освобождаем ресурсы текущего менеджера шрифтов
		if (fontManager != null) {
			fontManager.dispose();
		}
		
		// Создаем новый менеджер шрифтов
		fontManager = new FontManager();
	}

	@Override
	public void dispose() {
		// Освобождаем ресурсы
		batch.dispose();
		if (gameAtlas != null) {
			gameAtlas.dispose();
		}
		
		// Освобождаем ресурсы шрифтов
		if (fontManager != null) {
			fontManager.dispose();
		}
		
		// Освобождаем ресурсы текущего экрана
		if (getScreen() != null) {
			getScreen().dispose();
		}
	}
} 