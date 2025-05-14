package com.badlogic.drop.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.os.Build;
import android.widget.Toast;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.drop.android.GoogleAuthManager.AuthCallback;
import com.badlogic.drop.util.GoogleAuthInterface;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.firebase.auth.FirebaseUser;

/** 
 * Лаунчер для Android-версии игры, оптимизированный для Samsung Galaxy S24 Ultra 
 */
public class AndroidLauncher extends AndroidApplication implements AuthCallback, GoogleAuthInterface {
    private static final String TAG = "AndroidLauncher";
    private GoogleAuthManager authManager;
    private SpaceCourierGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Устанавливаем полноэкранный режим
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Блокируем выключение экрана во время игры
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Для новых устройств используем иммерсивный режим
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            
            // Иммерсивный полноэкранный режим с поддержкой вырезов (для S24 Ultra)
            int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                      | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                      | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_FULLSCREEN;
            
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        
        // Инициализируем менеджер авторизации Google
        authManager = new GoogleAuthManager(this);
        
        // Создаем и настраиваем конфигурацию для Android
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        
        // Оптимизируем для производительности
        config.useAccelerometer = false; // Отключаем ускорометр, если не используем
        config.useCompass = false;      // Отключаем компас, если не используем
        config.useGyroscope = false;    // Отключаем гироскоп, если не используем
        
        // Включаем аппаратное ускорение
        config.useGL30 = true;          // Используем OpenGL ES 3.0 для улучшения производительности
        config.r = 8;                   // Глубина цвета R
        config.g = 8;                   // Глубина цвета G
        config.b = 8;                   // Глубина цвета B
        config.a = 8;                   // Альфа-канал
        
        // Включаем мультисэмплинг для лучшего качества отрисовки
        config.numSamples = 2;          // 2x MSAA (хороший баланс качества и производительности)
        
        // Устанавливаем буфер глубины
        config.depth = 16;              // 16-битный буфер глубины
        
        // Инициализируем игру с конфигурацией и передаем ей интерфейс для авторизации
        game = new SpaceCourierGame();
        // Устанавливаем интерфейс Google Auth
        game.setGoogleAuthInterface(this);
        initialize(game, config);
    }
    
    // --- Реализация интерфейса GoogleAuthInterface ---
    
    @Override
    public void signIn() {
        runOnUiThread(() -> {
            authManager.signIn(this);
        });
    }
    
    @Override
    public void signOut() {
        runOnUiThread(() -> {
            authManager.signOut();
            Toast.makeText(this, "Выход выполнен", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public boolean isSignedIn() {
        return authManager.isSignedIn();
    }
    
    @Override
    public String getUserName() {
        FirebaseUser user = authManager.getCurrentUser();
        return user != null ? user.getDisplayName() : null;
    }
    
    @Override
    public String getUserEmail() {
        FirebaseUser user = authManager.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
    
    @Override
    public String getUserId() {
        FirebaseUser user = authManager.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    @Override
    public String getUserPhotoUrl() {
        FirebaseUser user = authManager.getCurrentUser();
        return user != null && user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Передаем результат активности в менеджер аутентификации
        authManager.handleActivityResult(requestCode, resultCode, data);
    }
    
    // --- Реализация интерфейса AuthCallback ---
    
    @Override
    public void onAuthSuccess(FirebaseUser user) {
        Log.d(TAG, "Вход выполнен успешно: " + user.getDisplayName());
        runOnUiThread(() -> {
            Toast.makeText(this, "Добро пожаловать, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            // Уведомляем игру о успешном входе
            if (game != null) {
                game.onGoogleSignInSuccess(user.getDisplayName(), user.getEmail(), user.getUid());
            }
        });
    }
    
    @Override
    public void onAuthFailure(Exception exception) {
        Log.e(TAG, "Ошибка входа", exception);
        runOnUiThread(() -> {
            Toast.makeText(this, "Ошибка входа: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            // Уведомляем игру об ошибке входа
            if (game != null) {
                game.onGoogleSignInFailure(exception.getMessage());
            }
        });
    }
}