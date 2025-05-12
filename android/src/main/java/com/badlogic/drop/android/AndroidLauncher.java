package com.badlogic.drop.android;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.os.Build;

import com.badlogic.drop.SpaceCourierGame;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/** 
 * Лаунчер для Android-версии игры, оптимизированный для Samsung Galaxy S24 Ultra 
 */
public class AndroidLauncher extends AndroidApplication {
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
        
        // Инициализируем игру с конфигурацией
        initialize(new SpaceCourierGame(), config);
    }
}