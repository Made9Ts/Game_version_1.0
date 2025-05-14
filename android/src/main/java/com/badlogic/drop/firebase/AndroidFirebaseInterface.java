package com.badlogic.drop.firebase;

import android.app.Activity;
import android.util.Log;

import com.badlogic.gdx.utils.ObjectMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Android-реализация интерфейса Firebase.
 * Использует Firebase Realtime Database для хранения
 * достижений и статистики пользователя.
 */
public class AndroidFirebaseInterface implements FirebaseInterface {
    private static final String TAG = "AndroidFirebaseInterface";
    
    private final Activity activity;
    private final FirebaseAuth auth;
    private final FirebaseDatabase database;
    
    // Константы для путей в базе данных
    private static final String USERS_PATH = "users";
    private static final String ACHIEVEMENTS_PATH = "achievements";
    private static final String STATISTICS_PATH = "statistics";
    
    /**
     * Конструктор
     * @param activity Активность Android
     */
    public AndroidFirebaseInterface(Activity activity) {
        this.activity = activity;
        this.auth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance();
        
        try {
            // Включаем офлайн-функциональность (только один раз при старте приложения)
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Если setPersistenceEnabled уже был вызван, игнорируем ошибку
            Log.d(TAG, "Persistence already enabled: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isUserSignedIn() {
        return auth.getCurrentUser() != null;
    }
    
    @Override
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    @Override
    public String getCurrentUserName() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getDisplayName() : null;
    }
    
    @Override
    public String getCurrentUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
    
    @Override
    public void getAchievements(final String userId, final AchievementsCallback callback) {
        if (userId == null) {
            callback.onError("User ID cannot be null");
            return;
        }
        
        DatabaseReference userAchievementsRef = database.getReference()
                .child(USERS_PATH)
                .child(userId)
                .child(ACHIEVEMENTS_PATH);
        
        userAchievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ObjectMap<String, Object> achievements = new ObjectMap<String, Object>();
                
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    Object value = childSnapshot.getValue();
                    achievements.put(key, value);
                }
                
                callback.onAchievementsLoaded(achievements);
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getAchievements:onCancelled", databaseError.toException());
                callback.onError(databaseError.getMessage());
            }
        });
    }
    
    @Override
    public void saveAchievements(final String userId, final ObjectMap<String, Object> achievements, 
                                 final CompletionCallback callback) {
        if (userId == null) {
            callback.onError("User ID cannot be null");
            return;
        }
        
        if (achievements == null || achievements.size == 0) {
            callback.onError("Achievements data cannot be null or empty");
            return;
        }
        
        DatabaseReference userAchievementsRef = database.getReference()
                .child(USERS_PATH)
                .child(userId)
                .child(ACHIEVEMENTS_PATH);
        
        // Конвертируем ObjectMap в Map для Firebase
        Map<String, Object> achievementsMap = convertToJavaMap(achievements);
        
        userAchievementsRef.updateChildren(achievementsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "saveAchievements:onComplete:error", databaseError.toException());
                    callback.onError(databaseError.getMessage());
                } else {
                    callback.onSuccess();
                }
            }
        });
    }
    
    @Override
    public void saveGameStatistics(final String userId, final ObjectMap<String, Object> statistics, 
                                   final CompletionCallback callback) {
        if (userId == null) {
            callback.onError("User ID cannot be null");
            return;
        }
        
        if (statistics == null || statistics.size == 0) {
            callback.onError("Statistics data cannot be null or empty");
            return;
        }
        
        DatabaseReference userStatisticsRef = database.getReference()
                .child(USERS_PATH)
                .child(userId)
                .child(STATISTICS_PATH);
        
        // Конвертируем ObjectMap в Map для Firebase
        Map<String, Object> statisticsMap = convertToJavaMap(statistics);
        
        userStatisticsRef.updateChildren(statisticsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "saveGameStatistics:onComplete:error", databaseError.toException());
                    callback.onError(databaseError.getMessage());
                } else {
                    callback.onSuccess();
                }
            }
        });
    }
    
    @Override
    public void getGameStatistics(final String userId, final AchievementsCallback callback) {
        if (userId == null) {
            callback.onError("User ID cannot be null");
            return;
        }
        
        DatabaseReference userStatisticsRef = database.getReference()
                .child(USERS_PATH)
                .child(userId)
                .child(STATISTICS_PATH);
        
        userStatisticsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ObjectMap<String, Object> statistics = new ObjectMap<String, Object>();
                
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    Object value = childSnapshot.getValue();
                    statistics.put(key, value);
                }
                
                callback.onAchievementsLoaded(statistics);
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getGameStatistics:onCancelled", databaseError.toException());
                callback.onError(databaseError.getMessage());
            }
        });
    }
    
    /**
     * Конвертирует ObjectMap<String, Object> в Map<String, Object>
     * @param objectMap Исходная карта
     * @return Java Map для Firebase
     */
    private Map<String, Object> convertToJavaMap(ObjectMap<String, Object> objectMap) {
        Map<String, Object> javaMap = new HashMap<>();
        
        for (ObjectMap.Entry<String, Object> entry : objectMap.entries()) {
            javaMap.put(entry.key, entry.value);
        }
        
        return javaMap;
    }
} 