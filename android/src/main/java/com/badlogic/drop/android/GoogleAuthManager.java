package com.badlogic.drop.android;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Менеджер авторизации Google для игры.
 * Обеспечивает вход и выход из Google аккаунта.
 */
public class GoogleAuthManager {
    private static final String TAG = "GoogleAuthManager";
    private static final int RC_SIGN_IN = 9001;

    private final Activity activity;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private AuthCallback authCallback;

    /**
     * Интерфейс для обратного вызова при аутентификации
     */
    public interface AuthCallback {
        void onAuthSuccess(FirebaseUser user);
        void onAuthFailure(Exception exception);
    }

    /**
     * Конструктор менеджера аутентификации Google
     * @param activity Активность, с которой будет работать менеджер
     */
    public GoogleAuthManager(Activity activity) {
        this.activity = activity;
        initializeAuth();
    }

    /**
     * Инициализирует необходимые компоненты для аутентификации
     */
    private void initializeAuth() {
        // Инициализация Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Настройка входа в систему Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1051336094878-kq3593rbem64qv7r9eu11fsoucrccpf6.apps.googleusercontent.com")  // Нужно заменить на ваш Web Client ID из Firebase Console
                .requestEmail()
                .build();

        // Создание клиента для входа через Google
        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    /**
     * Начинает процесс входа в систему Google
     * @param callback Обратный вызов для результата аутентификации
     */
    public void signIn(AuthCallback callback) {
        this.authCallback = callback;
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Выход из текущей учетной записи
     */
    public void signOut() {
        // Выход из Firebase
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }

        // Выход из Google
        if (googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener(activity,
                    task -> Log.d(TAG, "Google sign out completed"));
        }
    }

    /**
     * Обрабатывает результат активности входа в систему Google
     * @param requestCode Код запроса
     * @param resultCode Код результата
     * @param data Данные интента
     */
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Успешная аутентификация Google, теперь аутентифицируемся в Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                if (authCallback != null) {
                    authCallback.onAuthFailure(e);
                }
            }
        }
    }

    /**
     * Аутентифицирует пользователя в Firebase с помощью Google аккаунта
     * @param account Аккаунт Google
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Вход выполнен успешно
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (authCallback != null) {
                                authCallback.onAuthSuccess(user);
                            }
                        } else {
                            // Вход не удался
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (authCallback != null) {
                                authCallback.onAuthFailure(task.getException());
                            }
                        }
                    }
                });
    }

    /**
     * Проверяет, авторизован ли пользователь
     * @return true, если пользователь авторизован
     */
    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Возвращает текущего авторизованного пользователя
     * @return Объект FirebaseUser или null, если пользователь не авторизован
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
} 