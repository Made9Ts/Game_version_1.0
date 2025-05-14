package com.badlogic.drop.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Система достижений для игры Space Courier.
 * Отслеживает прогресс игрока и выдает награды за выполнение определенных условий.
 * Хранит состояние разблокированных достижений между сессиями игры.
 */
public class AchievementSystem implements Disposable {
    
    // Состояние достижений
    private ObjectMap<String, Achievement> achievements;
    private Array<String> unlockedThisSession;
    
    // Константы для имен достижений
    public static final String ACHIEVEMENT_FIRST_FLIGHT = "first_flight";
    public static final String ACHIEVEMENT_LEVEL_5 = "level_5";
    public static final String ACHIEVEMENT_LEVEL_10 = "level_10";
    public static final String ACHIEVEMENT_SCORE_10000 = "score_10000";
    public static final String ACHIEVEMENT_NO_DAMAGE = "no_damage";
    public static final String ACHIEVEMENT_COLLECT_50_FUEL = "collect_50_fuel";
    public static final String ACHIEVEMENT_SURVIVE_5_MIN = "survive_5_min";
    
    // Новые достижения, похожие на существующие
    public static final String ACHIEVEMENT_LEVEL_15 = "level_15";
    public static final String ACHIEVEMENT_LEVEL_20 = "level_20";
    public static final String ACHIEVEMENT_SCORE_25000 = "score_25000";
    public static final String ACHIEVEMENT_COLLECT_100_FUEL = "collect_100_fuel";
    public static final String ACHIEVEMENT_SURVIVE_10_MIN = "survive_10_min";
    public static final String ACHIEVEMENT_TOTAL_FLIGHTS_10 = "total_flights_10";
    public static final String ACHIEVEMENT_TOTAL_SCORE_50000 = "total_score_50000";
    
    // Предпочтения для сохранения достижений
    private Preferences prefs;
    private static final String PREFS_NAME = "spacecourier_achievements";
    private static final String UNLOCKED_SUFFIX = "_unlocked";
    private static final String PROGRESS_SUFFIX = "_progress";
    
    /**
     * Класс для хранения информации о достижении
     */
    public class Achievement {
        public String id;
        public String title;
        public String description;
        public boolean unlocked;
        public int progress;
        public int maxProgress;
        
        public Achievement(String id, String title, String description, int maxProgress) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.unlocked = false;
            this.progress = 0;
            this.maxProgress = maxProgress;
        }
        
        /**
         * Возвращает процент выполнения достижения от 0 до 1
         */
        public float getProgressPercent() {
            return (float) progress / maxProgress;
        }
    }
    
    /**
     * Конструктор системы достижений
     */
    public AchievementSystem() {
        achievements = new ObjectMap<String, Achievement>();
        unlockedThisSession = new Array<String>();
        
        // Загружаем настройки
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Инициализируем достижения
        initializeAchievements();
        
        // Загружаем прогресс
        loadProgress();
    }
    
    /**
     * Создает все достижения
     */
    private void initializeAchievements() {
        // Достижение за первый полет
        Achievement firstFlight = new Achievement(
            ACHIEVEMENT_FIRST_FLIGHT,
            "Первый полет",
            "Начать свое первое космическое путешествие",
            1
        );
        achievements.put(ACHIEVEMENT_FIRST_FLIGHT, firstFlight);
        
        // Достижение за достижение 5 уровня
        Achievement level5 = new Achievement(
            ACHIEVEMENT_LEVEL_5,
            "Опытный курьер",
            "Достичь 5 уровня",
            5
        );
        achievements.put(ACHIEVEMENT_LEVEL_5, level5);
        
        // Достижение за достижение 10 уровня
        Achievement level10 = new Achievement(
            ACHIEVEMENT_LEVEL_10,
            "Мастер доставки",
            "Достичь 10 уровня",
            10
        );
        achievements.put(ACHIEVEMENT_LEVEL_10, level10);
        
        // Достижение за набор 10000 очков
        Achievement score10000 = new Achievement(
            ACHIEVEMENT_SCORE_10000,
            "Звездный рейтинг",
            "Набрать 10000 очков за одну игру",
            10000
        );
        achievements.put(ACHIEVEMENT_SCORE_10000, score10000);
        
        // Достижение за игру без потери жизней
        Achievement noDamage = new Achievement(
            ACHIEVEMENT_NO_DAMAGE,
            "Неуязвимый",
            "Набрать 3000 очков без потери жизней",
            1
        );
        achievements.put(ACHIEVEMENT_NO_DAMAGE, noDamage);
        
        // Достижение за сбор 50 канистр с топливом
        Achievement collect50Fuel = new Achievement(
            ACHIEVEMENT_COLLECT_50_FUEL,
            "Заправщик",
            "Собрать 50 канистр с топливом",
            50
        );
        achievements.put(ACHIEVEMENT_COLLECT_50_FUEL, collect50Fuel);
        
        // Достижение за выживание в течение 5 минут
        Achievement survive5Min = new Achievement(
            ACHIEVEMENT_SURVIVE_5_MIN,
            "Долгий путь",
            "Выжить в космосе в течение 5 минут",
            300 // 300 секунд = 5 минут
        );
        achievements.put(ACHIEVEMENT_SURVIVE_5_MIN, survive5Min);
        
        // НОВЫЕ ДОСТИЖЕНИЯ
        
        // Достижение за достижение 15 уровня (продолжение серии уровней)
        Achievement level15 = new Achievement(
            ACHIEVEMENT_LEVEL_15,
            "Элитный курьер",
            "Достичь 15 уровня",
            15
        );
        achievements.put(ACHIEVEMENT_LEVEL_15, level15);
        
        // Достижение за достижение 20 уровня (продолжение серии уровней)
        Achievement level20 = new Achievement(
            ACHIEVEMENT_LEVEL_20,
            "Легендарный курьер",
            "Достичь 20 уровня",
            20
        );
        achievements.put(ACHIEVEMENT_LEVEL_20, level20);
        
        // Достижение за набор 25000 очков (продолжение серии очков)
        Achievement score25000 = new Achievement(
            ACHIEVEMENT_SCORE_25000,
            "Галактический рейтинг",
            "Набрать 25000 очков за одну игру",
            25000
        );
        achievements.put(ACHIEVEMENT_SCORE_25000, score25000);
        
        // Достижение за сбор 100 канистр с топливом (продолжение серии заправщика)
        Achievement collect100Fuel = new Achievement(
            ACHIEVEMENT_COLLECT_100_FUEL,
            "Коллекционер топлива",
            "Собрать 100 канистр с топливом",
            100
        );
        achievements.put(ACHIEVEMENT_COLLECT_100_FUEL, collect100Fuel);
        
        // Достижение за выживание в течение 10 минут (продолжение серии выживания)
        Achievement survive10Min = new Achievement(
            ACHIEVEMENT_SURVIVE_10_MIN,
            "Космический марафонец",
            "Выжить в космосе в течение 10 минут",
            600 // 600 секунд = 10 минут
        );
        achievements.put(ACHIEVEMENT_SURVIVE_10_MIN, survive10Min);
        
        // Достижение за совершение 10 полетов (вариация первого полета)
        Achievement totalFlights10 = new Achievement(
            ACHIEVEMENT_TOTAL_FLIGHTS_10,
            "Опытный пилот",
            "Совершить 10 космических полетов",
            10
        );
        achievements.put(ACHIEVEMENT_TOTAL_FLIGHTS_10, totalFlights10);
        
        // Достижение за набор 50000 очков в общем счете (вариация звездного рейтинга)
        Achievement totalScore50000 = new Achievement(
            ACHIEVEMENT_TOTAL_SCORE_50000,
            "Корпоративная звезда",
            "Набрать 50000 очков за все время игры",
            50000
        );
        achievements.put(ACHIEVEMENT_TOTAL_SCORE_50000, totalScore50000);
    }
    
    /**
     * Загружает прогресс достижений из настроек
     */
    private void loadProgress() {
        for (String id : achievements.keys()) {
            Achievement achievement = achievements.get(id);
            achievement.unlocked = prefs.getBoolean(id + UNLOCKED_SUFFIX, false);
            achievement.progress = prefs.getInteger(id + PROGRESS_SUFFIX, 0);
        }
    }
    
    /**
     * Сохраняет прогресс достижений в настройки
     */
    private void saveProgress() {
        for (String id : achievements.keys()) {
            Achievement achievement = achievements.get(id);
            prefs.putBoolean(id + UNLOCKED_SUFFIX, achievement.unlocked);
            prefs.putInteger(id + PROGRESS_SUFFIX, achievement.progress);
        }
        prefs.flush();
    }
    
    /**
     * Обновляет прогресс достижения
     * @param id идентификатор достижения
     * @param progress новый прогресс (если больше текущего)
     * @return true, если достижение разблокировано в этот момент
     */
    public boolean updateProgress(String id, int progress) {
        if (!achievements.containsKey(id)) {
            return false;
        }
        
        Achievement achievement = achievements.get(id);
        
        // Если достижение уже разблокировано, ничего не делаем
        if (achievement.unlocked) {
            return false;
        }
        
        // Обновляем прогресс, только если новое значение больше текущего
        if (progress > achievement.progress) {
            achievement.progress = progress;
            
            // Проверяем, достигли ли мы максимального прогресса
            if (achievement.progress >= achievement.maxProgress) {
                achievement.unlocked = true;
                unlockedThisSession.add(id);
                saveProgress();
                return true;
            }
            
            // Сохраняем прогресс
            saveProgress();
        }
        
        return false;
    }
    
    /**
     * Увеличивает прогресс достижения на 1
     * @param id идентификатор достижения
     * @return true, если достижение разблокировано в этот момент
     */
    public boolean incrementProgress(String id) {
        if (!achievements.containsKey(id)) {
            return false;
        }
        
        Achievement achievement = achievements.get(id);
        
        // Если достижение уже разблокировано, ничего не делаем
        if (achievement.unlocked) {
            return false;
        }
        
        // Увеличиваем прогресс
        achievement.progress++;
        
        // Проверяем, достигли ли мы максимального прогресса
        if (achievement.progress >= achievement.maxProgress) {
            achievement.unlocked = true;
            unlockedThisSession.add(id);
            saveProgress();
            return true;
        }
        
        // Сохраняем прогресс
        saveProgress();
        return false;
    }
    
    /**
     * Разблокирует достижение немедленно
     * @param id идентификатор достижения
     * @return true, если достижение было разблокировано (false, если уже было разблокировано)
     */
    public boolean unlockAchievement(String id) {
        if (!achievements.containsKey(id)) {
            return false;
        }
        
        Achievement achievement = achievements.get(id);
        
        // Если достижение уже разблокировано, ничего не делаем
        if (achievement.unlocked) {
            return false;
        }
        
        // Разблокируем достижение
        achievement.progress = achievement.maxProgress;
        achievement.unlocked = true;
        unlockedThisSession.add(id);
        saveProgress();
        return true;
    }
    
    /**
     * Проверяет, разблокировано ли достижение
     * @param id идентификатор достижения
     * @return true, если достижение разблокировано
     */
    public boolean isUnlocked(String id) {
        if (!achievements.containsKey(id)) {
            return false;
        }
        
        return achievements.get(id).unlocked;
    }
    
    /**
     * Возвращает прогресс достижения
     * @param id идентификатор достижения
     * @return прогресс достижения (0-100%)
     */
    public float getProgress(String id) {
        if (!achievements.containsKey(id)) {
            return 0;
        }
        
        Achievement achievement = achievements.get(id);
        return (float) achievement.progress / achievement.maxProgress;
    }
    
    /**
     * Возвращает массив идентификаторов достижений, разблокированных в текущей сессии
     * @return массив идентификаторов достижений
     */
    public Array<String> getUnlockedThisSession() {
        return unlockedThisSession;
    }
    
    /**
     * Очищает список достижений, разблокированных в текущей сессии
     */
    public void clearUnlockedThisSession() {
        unlockedThisSession.clear();
    }
    
    /**
     * Возвращает информацию о достижении
     * @param id идентификатор достижения
     * @return объект достижения или null, если достижение не найдено
     */
    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }
    
    /**
     * Возвращает все достижения
     * @return карта достижений
     */
    public ObjectMap<String, Achievement> getAllAchievements() {
        return achievements;
    }
    
    /**
     * Освобождает ресурсы, используемые системой достижений.
     * Сохраняет текущий прогресс перед завершением работы.
     */
    @Override
    public void dispose() {
        saveProgress();
        achievements.clear();
        unlockedThisSession.clear();
    }
} 