package com.badlogic.drop.systems;

import com.badlogic.gdx.math.MathUtils;

/**
 * Система динамически адаптируемой сложности игры
 * Регулирует уровень сложности на основе успехов и неудач игрока
 */
public class DifficultySystem {
    // Базовая сложность
    private float baseDifficulty;
    
    // Сложность с учетом умения игрока
    private float currentDifficulty;
    
    // Оценка умения игрока (от 0.5 до 2.0)
    private float playerSkill;
    
    // Счетчики успехов и неудач
    private int successCount;
    private int failureCount;
    
    // Прогресс игрока (счет)
    private int lastScore;
    
    // Система уровней
    private int currentLevel;
    private int scoreForNextLevel;
    private int[] levelThresholds = {0, 500, 1200, 2000, 3000, 5000, 8000, 12000, 17000, 25000};
    
    // Постоянные значения
    private static final float MIN_DIFFICULTY = 1.0f;
    private static final float MAX_DIFFICULTY = 5.0f;
    private static final float MIN_PLAYER_SKILL = 0.5f;
    private static final float MAX_PLAYER_SKILL = 2.0f;
    
    // Коэффициенты для расчетов
    private static final float DIFFICULTY_PROGRESSION_RATE = 0.0005f; // Насколько быстро растет базовая сложность
    private static final float PLAYER_SKILL_SUCCESS_DELTA = 0.05f;    // Увеличение навыка при успехе
    private static final float PLAYER_SKILL_FAILURE_DELTA = 0.1f;     // Уменьшение навыка при неудаче
    private static final float PLAYER_SKILL_WEIGHT = 0.7f;            // Вес навыка в формуле сложности
    private static final float DIFFICULTY_SMOOTHING = 0.1f;           // Скорость изменения сложности
    
    // Событие смены уровня
    private boolean levelChanged;
    
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
    }
    
    /**
     * Основной метод обновления, вызывается каждый кадр
     * @param score текущий счет игрока
     * @param delta время, прошедшее с предыдущего кадра в секундах
     */
    public void update(int score, float delta) {
        levelChanged = false;
        
        // Обновляем базовую сложность на основе прогресса игрока
        if (score > lastScore) {
            baseDifficulty += (score - lastScore) * DIFFICULTY_PROGRESSION_RATE;
            lastScore = score;
            
            // Проверяем достижение нового уровня
            checkLevelProgress(score);
        }
        
        // Рассчитываем целевую сложность на основе навыка игрока
        float targetDifficulty = calculateTargetDifficulty();
        
        // Плавно изменяем текущую сложность к целевой
        currentDifficulty = MathUtils.lerp(currentDifficulty, targetDifficulty, DIFFICULTY_SMOOTHING * delta);
        
        // Ограничиваем сложность в допустимых пределах
        currentDifficulty = MathUtils.clamp(currentDifficulty, MIN_DIFFICULTY, MAX_DIFFICULTY);
    }
    
    /**
     * Проверяет и обновляет уровень игрока на основе текущего счета
     * @param score текущий счет игрока
     */
    private void checkLevelProgress(int score) {
        // Проверяем, достигли ли мы порога следующего уровня
        if (score >= scoreForNextLevel && currentLevel < levelThresholds.length - 1) {
            currentLevel++;
            scoreForNextLevel = levelThresholds[currentLevel];
            levelChanged = true;
            
            // При переходе на новый уровень увеличиваем базовую сложность
            baseDifficulty += 0.3f;
            baseDifficulty = MathUtils.clamp(baseDifficulty, MIN_DIFFICULTY, MAX_DIFFICULTY);
        }
    }
    
    /**
     * Регистрирует успешное действие игрока
     */
    public void registerSuccess() {
        successCount++;
        
        // Увеличиваем оценку навыка игрока
        playerSkill += PLAYER_SKILL_SUCCESS_DELTA;
        playerSkill = MathUtils.clamp(playerSkill, MIN_PLAYER_SKILL, MAX_PLAYER_SKILL);
        
        // Каждые 10 успехов сбрасываем счетчики, сохраняя отношение
        if (successCount + failureCount >= 10) {
            recalculateSkillRatio();
        }
    }
    
    /**
     * Регистрирует неудачное действие игрока
     */
    public void registerFailure() {
        failureCount++;
        
        // Уменьшаем оценку навыка игрока
        playerSkill -= PLAYER_SKILL_FAILURE_DELTA;
        playerSkill = MathUtils.clamp(playerSkill, MIN_PLAYER_SKILL, MAX_PLAYER_SKILL);
        
        // Каждые 10 действий сбрасываем счетчики, сохраняя отношение
        if (successCount + failureCount >= 10) {
            recalculateSkillRatio();
        }
    }
    
    /**
     * Пересчитывает соотношение успехов/неудач для долгосрочной статистики
     */
    private void recalculateSkillRatio() {
        if (successCount + failureCount > 0) {
            // Сохраняем соотношение успехов и неудач
            float ratio = (float) successCount / (successCount + failureCount);
            
            // Сбрасываем счетчики
            successCount = 0;
            failureCount = 0;
            
            // Дополнительная корректировка навыка на основе соотношения
            if (ratio > 0.7f) {
                // Если игрок успешен более чем в 70% случаев, немного увеличиваем сложность
                playerSkill += PLAYER_SKILL_SUCCESS_DELTA;
            } else if (ratio < 0.3f) {
                // Если игрок успешен менее чем в 30% случаев, немного уменьшаем сложность
                playerSkill -= PLAYER_SKILL_FAILURE_DELTA;
            }
            
            playerSkill = MathUtils.clamp(playerSkill, MIN_PLAYER_SKILL, MAX_PLAYER_SKILL);
        }
    }
    
    /**
     * Рассчитывает целевую сложность на основе базовой сложности и навыка игрока
     * @return целевая сложность
     */
    private float calculateTargetDifficulty() {
        // Базовая формула: базовая сложность + модификатор на основе навыка
        float skillModifier = (playerSkill - 1.0f) * PLAYER_SKILL_WEIGHT;
        return baseDifficulty * (1.0f + skillModifier);
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
} 