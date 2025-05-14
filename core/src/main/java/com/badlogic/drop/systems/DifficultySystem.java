package com.badlogic.drop.systems;

import com.badlogic.gdx.math.MathUtils;

/**
 * Усовершенствованная система динамически адаптируемой сложности игры
 * Регулирует уровень сложности на основе успехов, неудач игрока и различных игровых метрик
 * Включает систему зон комфорта и систему подстраивания под стиль игры
 */
public class DifficultySystem {
    // Базовая сложность
    private float baseDifficulty;
    
    // Сложность с учетом умения игрока
    private float currentDifficulty;
    
    // Оценка умения игрока (от 0.5 до 2.0)
    private float playerSkill;
    
    // Метрики игрока
    private int successCount;     // Успешные действия (уклонения, сбор топлива)
    private int failureCount;     // Неудачи (столкновения)
    private float survivalTime;   // Время выживания на текущем уровне
    
    // Параметры игрового стиля
    private float aggressivePlayStyle;    // 0.0 = осторожный, 1.0 = агрессивный
    private float collectorPlayStyle;     // 0.0 = игнорирует сбор, 1.0 = активно собирает
    
    // Прогресс игрока (счет)
    private int lastScore;
    
    // Система уровней с увеличенным максимумом
    private int currentLevel;
    private int scoreForNextLevel;
    private int[] levelThresholds = {
        0, 500, 1200, 2000, 3000, 5000, 8000, 12000, 17000, 25000, 
        35000, 50000, 70000, 100000, 150000, 200000, 250000, 300000
    };
    
    // Постоянные значения
    private static final float MIN_DIFFICULTY = 1.0f;
    private static final float MAX_DIFFICULTY = 15.0f;      // Увеличен максимум сложности с 10.0f до 15.0f
    private static final float MIN_PLAYER_SKILL = 0.5f;
    private static final float MAX_PLAYER_SKILL = 2.0f;
    
    // Коэффициенты для расчетов
    private static final float DIFFICULTY_PROGRESSION_RATE = 0.0006f;    // Увеличена скорость роста базовой сложности (0.0003f -> 0.0006f)
    private static final float PLAYER_SKILL_SUCCESS_DELTA = 0.03f;       // Увеличено влияние успехов (0.02f -> 0.03f)
    private static final float PLAYER_SKILL_FAILURE_DELTA = 0.04f;       // Немного уменьшено влияние неудач (0.05f -> 0.04f)
    private static final float PLAYER_SKILL_WEIGHT = 0.7f;               // Увеличен вес навыка (0.6f -> 0.7f)
    private static final float DIFFICULTY_SMOOTHING = 0.08f;             // Ускорено изменение сложности (0.05f -> 0.08f)
    private static final float PLAY_STYLE_ADAPTATION_RATE = 0.02f;       // Увеличена скорость адаптации (0.01f -> 0.02f)
    
    // Зона комфорта - контролирует вызовы игрока
    private float comfortZoneMin = 0.8f;   // Минимальный уровень относительно базовой сложности
    private float comfortZoneMax = 1.2f;   // Максимальный уровень относительно базовой сложности
    private float comfortZoneTimer = 0;    // Время пребывания в зоне комфорта
    private static final float COMFORT_ZONE_EXPAND_TIME = 60.0f;  // Через сколько секунд расширять зону
    
    // Событие смены уровня
    private boolean levelChanged;
    private int lastLevelChangeScore;  // Счет, при котором последний раз менялся уровень
    
    // Флаги для игровых событий
    private boolean recentFailure;     // Произошла недавняя неудача
    private boolean recentSuccess;     // Произошел недавний успех
    private float eventCooldown;       // Время до сброса флагов событий
    
    /**
     * Конструктор системы сложности
     */
    public DifficultySystem() {
        reset();
    }
    
    /**
     * Сброс системы к начальным значениям
     */
    public void reset() {
        baseDifficulty = MIN_DIFFICULTY;
        currentDifficulty = MIN_DIFFICULTY;
        playerSkill = 1.0f;
        successCount = 0;
        failureCount = 0;
        lastScore = 0;
        currentLevel = 1;
        scoreForNextLevel = levelThresholds[1];
        levelChanged = false;
        lastLevelChangeScore = 0;
        
        // Сброс новых параметров
        survivalTime = 0;
        aggressivePlayStyle = 0.5f;
        collectorPlayStyle = 0.5f;
        comfortZoneMin = 0.8f;
        comfortZoneMax = 1.2f;
        comfortZoneTimer = 0;
        recentFailure = false;
        recentSuccess = false;
        eventCooldown = 0;
    }
    
    /**
     * Основной метод обновления, вызывается каждый кадр
     * @param score текущий счет игрока
     * @param delta время, прошедшее с предыдущего кадра в секундах
     */
    public void update(int score, float delta) {
        levelChanged = false;
        
        // Увеличиваем время выживания
        survivalTime += delta;
        
        // Обновляем таймер зоны комфорта
        comfortZoneTimer += delta;
        if (comfortZoneTimer >= COMFORT_ZONE_EXPAND_TIME) {
            // Постепенно расширяем зону комфорта
            comfortZoneMin = Math.max(0.6f, comfortZoneMin - 0.01f);
            comfortZoneMax = Math.min(1.4f, comfortZoneMax + 0.01f);
            comfortZoneTimer = 0;
        }
        
        // Обновляем кулдаун событий
        if (eventCooldown > 0) {
            eventCooldown -= delta;
            if (eventCooldown <= 0) {
                recentFailure = false;
                recentSuccess = false;
            }
        }
        
        // Обновляем базовую сложность на основе прогресса игрока
        if (score > lastScore) {
            float scoreDelta = score - lastScore;
            
            // Учитываем стиль игры при увеличении сложности
            float difficultyDelta = scoreDelta * DIFFICULTY_PROGRESSION_RATE;
            
            // Агрессивные игроки получают больший прирост сложности
            if (aggressivePlayStyle > 0.5f) {
                difficultyDelta *= 1.0f + (aggressivePlayStyle - 0.5f);
            }
            
            baseDifficulty += difficultyDelta;
            lastScore = score;
            
            // Проверяем достижение нового уровня
            checkLevelProgress(score);
        }
        
        // Рассчитываем целевую сложность на основе навыка игрока и зоны комфорта
        float targetDifficulty = calculateTargetDifficulty();
        
        // Учитываем недавние события для более динамичных изменений
        if (recentFailure) {
            targetDifficulty *= 0.9f; // Временное снижение после неудачи
        } else if (recentSuccess) {
            targetDifficulty *= 1.1f; // Временное повышение после успеха
        }
        
        // Плавно изменяем текущую сложность к целевой
        currentDifficulty = MathUtils.lerp(currentDifficulty, targetDifficulty, DIFFICULTY_SMOOTHING * delta);
        
        // Ограничиваем сложность в допустимых пределах
        currentDifficulty = MathUtils.clamp(currentDifficulty, MIN_DIFFICULTY, MAX_DIFFICULTY);
    }
    
    /**
     * Регистрирует успешное действие игрока
     */
    public void registerSuccess() {
        successCount++;
        
        // Увеличиваем оценку навыка игрока
        float skillDelta = PLAYER_SKILL_SUCCESS_DELTA;
        
        playerSkill += skillDelta;
        playerSkill = MathUtils.clamp(playerSkill, MIN_PLAYER_SKILL, MAX_PLAYER_SKILL);
        
        // Устанавливаем флаг недавнего успеха
        recentSuccess = true;
        recentFailure = false;
        eventCooldown = 2.0f;
        
        // Каждые 10 успехов сбрасываем счетчики, сохраняя отношение
        if (successCount + failureCount >= 15) {
            recalculateSkillRatio();
        }
    }
    
    /**
     * Регистрирует сбор топлива (отдельный тип успеха)
     */
    public void registerFuelCollection() {
        successCount++;
        
        // Сбор топлива увеличивает параметр "коллекционер"
        collectorPlayStyle = Math.min(1.0f, collectorPlayStyle + PLAY_STYLE_ADAPTATION_RATE);
        
        // Увеличиваем оценку навыка игрока (меньше, чем за другие успехи)
        playerSkill += PLAYER_SKILL_SUCCESS_DELTA * 0.7f;
        playerSkill = MathUtils.clamp(playerSkill, MIN_PLAYER_SKILL, MAX_PLAYER_SKILL);
        
        // Проверяем необходимость пересчета отношения успехов/неудач
        if (successCount + failureCount >= 15) {
            recalculateSkillRatio();
        }
    }
    
    /**
     * Регистрирует уклонение от препятствия (отдельный тип успеха)
     */
    public void registerDodge() {
        successCount++;
        
        // Успешное уклонение увеличивает параметр "агрессивность"
        aggressivePlayStyle = Math.min(1.0f, aggressivePlayStyle + PLAY_STYLE_ADAPTATION_RATE * 0.5f);
        
        // Увеличиваем оценку навыка игрока
        playerSkill += PLAYER_SKILL_SUCCESS_DELTA * 0.8f;
        playerSkill = MathUtils.clamp(playerSkill, MIN_PLAYER_SKILL, MAX_PLAYER_SKILL);
        
        // Проверяем необходимость пересчета отношения успехов/неудач
        if (successCount + failureCount >= 15) {
            recalculateSkillRatio();
        }
    }
    
    /**
     * Регистрирует неудачу игрока (столкновение)
     */
    public void registerFailure() {
        failureCount++;
        
        // Уменьшаем оценку навыка игрока
        playerSkill -= PLAYER_SKILL_FAILURE_DELTA;
        playerSkill = MathUtils.clamp(playerSkill, MIN_PLAYER_SKILL, MAX_PLAYER_SKILL);
        
        // Устанавливаем флаг недавней неудачи
        recentFailure = true;
        recentSuccess = false;
        eventCooldown = 3.0f;
        
        // После неудачи немного снижаем агрессивность стиля игры
        aggressivePlayStyle = Math.max(0.0f, aggressivePlayStyle - PLAY_STYLE_ADAPTATION_RATE);
        
        // Проверяем необходимость пересчета отношения успехов/неудач
        if (successCount + failureCount >= 15) {
            recalculateSkillRatio();
        }
    }
    
    /**
     * Пересчитывает соотношение успехов к неудачам, уменьшая абсолютные значения
     * Сохраняет пропорцию, но предотвращает переполнение счетчиков
     */
    private void recalculateSkillRatio() {
        if (successCount + failureCount > 0) {
            float ratio = (float)successCount / (successCount + failureCount);
            successCount = Math.max(3, (int)(ratio * 10)); // Минимум 3 успеха
            failureCount = 10 - successCount;
        }
    }
    
    /**
     * Рассчитывает целевую сложность на основе базовой сложности и навыка игрока
     * с учетом зоны комфорта
     */
    private float calculateTargetDifficulty() {
        // Базовая формула: целевая сложность = базовая сложность * (навык^коэффициент)
        float rawDifficulty = baseDifficulty * (float)Math.pow(playerSkill, PLAYER_SKILL_WEIGHT);
        
        // Проверяем, находится ли результат в зоне комфорта
        float minComfortDifficulty = baseDifficulty * comfortZoneMin;
        float maxComfortDifficulty = baseDifficulty * comfortZoneMax;
        
        // Возвращаем ограниченную сложность
        return MathUtils.clamp(rawDifficulty, minComfortDifficulty, maxComfortDifficulty);
        }
        
    /**
     * Проверяет прогресс игрока к следующему уровню
     */
    private void checkLevelProgress(int score) {
        // Если достигнут порог следующего уровня
        if (currentLevel < levelThresholds.length - 1 && score >= scoreForNextLevel) {
            currentLevel++;
            levelChanged = true;
            lastLevelChangeScore = score;
        
            // Обновляем порог для следующего уровня
            if (currentLevel < levelThresholds.length - 1) {
                scoreForNextLevel = levelThresholds[currentLevel + 1];
            } else {
                scoreForNextLevel = Integer.MAX_VALUE; // Финальный уровень
            }
            
            // Сужаем зону комфорта при переходе на новый уровень
            comfortZoneMin = Math.min(0.9f, comfortZoneMin + 0.05f);
            comfortZoneMax = Math.max(1.1f, comfortZoneMax - 0.05f);
            comfortZoneTimer = 0;
        
            // Ограничиваем базовую сложность
            baseDifficulty = MathUtils.clamp(baseDifficulty, MIN_DIFFICULTY, MAX_DIFFICULTY);
        }
    }
    
    /**
     * Возвращает текущий уровень сложности
     * @return текущая сложность
     */
    public float getDifficulty() {
        return currentDifficulty;
    }
    
    /**
     * Возвращает оценку навыка игрока
     * @return оценка навыка
     */
    public float getPlayerSkill() {
        return playerSkill;
    }
    
    /**
     * Возвращает текущий уровень игрока
     * @return текущий уровень
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Возвращает количество очков, необходимое для достижения следующего уровня
     * @return очки для следующего уровня
     */
    public int getScoreForNextLevel() {
        return scoreForNextLevel;
    }
    
    /**
     * Проверяет, изменился ли уровень на этом кадре
     * @return true, если уровень изменился
     */
    public boolean hasLevelChanged() {
        return levelChanged;
    }
    
    /**
     * Возвращает порог очков для указанного уровня
     * @param level уровень, для которого нужно получить порог очков
     * @return порог очков или 0, если уровень недопустим
     */
    public int getLevelThreshold(int level) {
        if (level >= 0 && level < levelThresholds.length) {
            return levelThresholds[level];
        }
        return 0;
    }
    
    /**
     * Возвращает значение агрессивности стиля игры
     * @return величина от 0.0 до 1.0
     */
    public float getAggressivePlayStyle() {
        return aggressivePlayStyle;
    }
    
    /**
     * Возвращает значение стиля коллекционирования
     * @return величина от 0.0 до 1.0
     */
    public float getCollectorPlayStyle() {
        return collectorPlayStyle;
    }
    
    /**
     * Возвращает прогресс в расширении зоны комфорта
     * @return величина от 0.0 до 1.0
     */
    public float getComfortZoneProgress() {
        return comfortZoneTimer / COMFORT_ZONE_EXPAND_TIME;
    }
} 