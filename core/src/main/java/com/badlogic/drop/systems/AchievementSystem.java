package com.badlogic.drop.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.drop.firebase.FirebaseInterface;

/**
 * Система достижений для игры Space Courier.
 * Отслеживает прогресс игрока и выдает награды за выполнение определенных условий.
 * Хранит состояние разблокированных достижений между сессиями игры.
 * Поддерживает сохранение достижений в Firebase для авторизованных пользователей.
 */
public class AchievementSystem implements Disposable {
    
    // Состояние достижений
    private ObjectMap<String, Achievement> achievements;
    private Array<String> unlockedThisSession;
    private boolean isLoading = false; // Флаг, указывающий что идет загрузка достижений
    
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
    
    // Firebase для онлайн-сохранений
    private FirebaseInterface firebase;
    private boolean isUsingFirebase = false;
    private String userId = null;
    private SpaceCourierGame game;
    
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
        
        // Проверяем, что Gdx.app инициализирован
        if (Gdx.app == null) {
            System.err.println("AchievementSystem: Gdx.app не инициализирован!");
            // Инициализируем только достижения без загрузки прогресса
            initializeAchievements();
            return;
        }
        
        // Загружаем настройки
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Инициализируем достижения
        initializeAchievements();
        
        // Загружаем прогресс из локального хранилища
        loadProgress();
    }
    
    /**
     * Конструктор системы достижений с Firebase
     * @param game Основной класс игры
     * @param firebase Интерфейс Firebase
     */
    public AchievementSystem(SpaceCourierGame game, FirebaseInterface firebase) {
        this();
        this.game = game;
        this.firebase = firebase;
        this.isUsingFirebase = (firebase != null);
    }
    
    /**
     * Устанавливает текущего пользователя Firebase
     * @param userId ID пользователя
     */
    public void setUser(String userId) {
        // Если ID не изменился, ничего не делаем
        if ((this.userId == null && userId == null) || 
            (this.userId != null && this.userId.equals(userId))) {
            return;
        }
        
        // Если у нас был предыдущий пользователь, сохраняем его достижения
        if (isUsingFirebase && this.userId != null) {
            // Сохраняем текущие достижения перед переключением
            saveProgress();
        }
        
        // Сохраняем новый ID пользователя
        String oldUserId = this.userId;
        this.userId = userId;
        
        // Устанавливаем флаг загрузки, если новый пользователь (для немедленного UI-отклика)
        if (userId != null) {
            isLoading = true;
        }
        
        // Если установлен новый пользователь и Firebase доступен
        if (isUsingFirebase && userId != null) {
            // Сначала сбрасываем все прогрессы на исходные 
            resetAllAchievements();
            
            // Затем загружаем достижения пользователя из Firebase
            loadFromFirebase();
            
            // Добавляем логирование для отладки
            Gdx.app.log("AchievementSystem", "Переключение пользователя с " + 
                         (oldUserId != null ? oldUserId : "null") + " на " + userId);
        } 
        // Если пользователь вышел (userId == null) или Firebase недоступен
        else {
            // Сбрасываем все достижения
            resetAllAchievements();
            
            // Сбрасываем состояние загрузки
            isLoading = false;
            
            Gdx.app.log("AchievementSystem", "Пользователь не авторизован или Firebase недоступен, достижения сброшены");
        }
    }
    
    /**
     * Сбрасывает все достижения в начальное состояние
     */
    private void resetAllAchievements() {
        for (String id : achievements.keys()) {
            Achievement achievement = achievements.get(id);
            achievement.progress = 0;
            achievement.unlocked = false;
        }
        
        unlockedThisSession.clear();
        Gdx.app.log("AchievementSystem", "Все достижения сброшены");
    }
    
    /**
     * Загружает достижения из Firebase
     */
    private void loadFromFirebase() {
        if (firebase == null || userId == null) {
            Gdx.app.error("AchievementSystem", "Невозможно загрузить из Firebase: " + 
                         (firebase == null ? "firebase == null" : "userId == null"));
            return;
        }
        
        Gdx.app.log("AchievementSystem", "Начинаем загрузку достижений из Firebase для пользователя: " + userId);
        
        // Устанавливаем флаг загрузки
        isLoading = true;
        
        final long startTime = System.currentTimeMillis();
        final boolean[] loadingCompleted = {false};
        
        // Запускаем асинхронную загрузку
        firebase.getAchievements(userId, new FirebaseInterface.AchievementsCallback() {
            @Override
            public void onAchievementsLoaded(ObjectMap<String, Object> achievementsData) {
                // Отмечаем, что загрузка завершена
                loadingCompleted[0] = true;
                isLoading = false;
                
                if (achievementsData == null || achievementsData.size == 0) {
                    Gdx.app.log("AchievementSystem", "Нет данных достижений в Firebase для пользователя: " + userId);
                    return;
                }
                
                Gdx.app.log("AchievementSystem", "Получены данные из Firebase: " + achievementsData.size + " записей");
                
                // Обновляем достижения из Firebase
                int updatedCount = 0;
                
                for (String id : achievements.keys()) {
                    Achievement achievement = achievements.get(id);
                    
                    // Проверяем, есть ли данные для этого достижения
                    if (achievementsData.containsKey(id + UNLOCKED_SUFFIX)) {
                        Object unlockedValue = achievementsData.get(id + UNLOCKED_SUFFIX);
                        if (unlockedValue instanceof Boolean) {
                            boolean unlocked = (Boolean) unlockedValue;
                            achievement.unlocked = unlocked;
                            updatedCount++;
                        }
                    }
                    
                    if (achievementsData.containsKey(id + PROGRESS_SUFFIX)) {
                        Object progressValue = achievementsData.get(id + PROGRESS_SUFFIX);
                        if (progressValue instanceof Number) {
                            int progress = ((Number) progressValue).intValue();
                            achievement.progress = progress;
                            updatedCount++;
                        }
                    }
                }
                
                // Сохраняем данные в локальное хранилище для резервного доступа
                saveProgressToLocal();
                
                // Журналируем статистику загрузки
                long loadTime = System.currentTimeMillis() - startTime;
                Gdx.app.log("AchievementSystem", "Достижения успешно загружены из Firebase для пользователя: " + 
                          userId + ". Обновлено " + updatedCount + " полей за " + loadTime + " мс");
            }
            
            @Override
            public void onError(String error) {
                // Отмечаем, что загрузка завершена с ошибкой
                loadingCompleted[0] = true;
                isLoading = false;
                
                Gdx.app.error("AchievementSystem", "Ошибка загрузки достижений из Firebase: " + error);
                
                // Если возникла ошибка, используем локальные данные
                loadProgressFromLocal();
            }
        });
        
        // Добавим таймаут для загрузки, чтобы не зависать если Firebase не отвечает
        // Это будет работать, только если метод вызывается из UI потока
        if (Gdx.app != null) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    // Проверяем через 5 секунд
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (!loadingCompleted[0] && elapsedTime > 5000) {
                        Gdx.app.error("AchievementSystem", "Тайм-аут загрузки достижений из Firebase для пользователя " + 
                                  userId + " после " + elapsedTime + " мс");
                        
                        // Если таймаут, загружаем локальные данные
                        loadProgressFromLocal();
                    }
                }
            });
        }
    }
    
    /**
     * Сохраняет достижения в Firebase
     */
    private void saveToFirebase() {
        if (!isUsingFirebase || userId == null) {
            Gdx.app.log("AchievementSystem", "Невозможно сохранить данные в Firebase: " +
                        (userId == null ? "userId == null" : "Firebase не активирован"));
            return;
        }
        
        Gdx.app.log("AchievementSystem", "Сохранение достижений в Firebase для пользователя: " + userId);
        final long startTime = System.currentTimeMillis();
        
        ObjectMap<String, Object> achievementsData = new ObjectMap<String, Object>();
        
        // Преобразуем достижения в формат для Firebase
        for (String id : achievements.keys()) {
            Achievement achievement = achievements.get(id);
            achievementsData.put(id + UNLOCKED_SUFFIX, achievement.unlocked);
            achievementsData.put(id + PROGRESS_SUFFIX, achievement.progress);
        }
        
        // Сохраняем в Firebase
        firebase.saveAchievements(userId, achievementsData, new FirebaseInterface.CompletionCallback() {
            @Override
            public void onSuccess() {
                long saveTime = System.currentTimeMillis() - startTime;
                Gdx.app.log("AchievementSystem", "Достижения успешно сохранены в Firebase для пользователя: " + 
                          userId + " за " + saveTime + " мс");
            }
            
            @Override
            public void onError(String error) {
                Gdx.app.error("AchievementSystem", "Ошибка сохранения достижений в Firebase для пользователя " + 
                           userId + ": " + error);
            }
        });
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
        if (isUsingFirebase && userId != null) {
            loadFromFirebase();
        } else {
            loadProgressFromLocal();
        }
    }
    
    /**
     * Загружает прогресс достижений из локального хранилища
     */
    private void loadProgressFromLocal() {
        // ВРЕМЕННО: Отключаем локальное хранилище, используем только Firebase
        Gdx.app.log("AchievementSystem", "Локальное хранилище временно отключено. Используем только Firebase.");
        
        // Если Firebase недоступен, инициализируем достижения пустыми значениями
        if (!isUsingFirebase || userId == null) {
            for (String id : achievements.keys()) {
                Achievement achievement = achievements.get(id);
                achievement.unlocked = false;
                achievement.progress = 0;
            }
        }
    }
    
    /**
     * Сохраняет прогресс достижений в локальное хранилище
     */
    private void saveProgressToLocal() {
        // ВРЕМЕННО: Отключаем локальное хранилище, используем только Firebase
        Gdx.app.log("AchievementSystem", "Локальное хранилище временно отключено. Используем только Firebase.");
        
        // Не сохраняем в локальное хранилище
    }
    
    /**
     * Сохраняет прогресс достижений
     */
    private void saveProgress() {
        // Временно: пропускаем локальное сохранение
        
        // Если включен Firebase и пользователь авторизован, сохраняем туда
        if (isUsingFirebase && userId != null) {
            saveToFirebase();
        } else {
            Gdx.app.log("AchievementSystem", "Данные не сохранены: Firebase " + 
                    (isUsingFirebase ? "активирован" : "не активирован") + 
                    ", userId " + (userId != null ? userId : "null"));
        }
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
    
    /**
     * Проверяет, выполняется ли в данный момент загрузка достижений
     * @return true, если идет асинхронная загрузка достижений
     */
    public boolean isLoading() {
        return isLoading;
    }
} 