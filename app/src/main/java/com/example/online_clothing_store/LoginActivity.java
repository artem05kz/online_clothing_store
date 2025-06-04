package com.example.online_clothing_store;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.online_clothing_store.database.entities.User;
import com.example.online_clothing_store.sync.SyncManager;
import com.example.online_clothing_store.utils.AuthHelper;
import com.example.online_clothing_store.utils.PasswordHasher;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.R;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private AppDatabase db;
    private TextInputEditText etEmail, etPassword;
    private SyncManager syncManager;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        syncManager = SyncManager.getInstance(this);
        db = AppDatabase.getInstance(this);

        // Выполняем синхронизацию при первой загрузке
        if (isFirstLoad) {
            isFirstLoad = false;
            Executors.newSingleThreadExecutor().execute(() -> {
                // Синхронизируем общие данные
                syncManager.performFullSync(-1);
            });
        }

        new Thread(() -> {
            User user = AuthHelper.tryAutoLogin(this);

            runOnUiThread(() -> {
                if (user != null) {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().putInt("user_id", user.getId()).apply();
                    AuthHelper.saveCredentials(this, user.getEmail(), user.getPasswordHash());

                    // Синхронизируем пользовательские данные
                    syncManager.performFullSync(user.getId());

                    startActivity(new Intent(this, RecommendationsActivity.class));
                    finish();
                } else {
                    setContentView(R.layout.activity_login);
                    etEmail = findViewById(R.id.EmailAddress);
                    etPassword = findViewById(R.id.TextPassword);

                    Button loginButton = findViewById(R.id.loginButton);
                    loginButton.setOnClickListener(v -> attemptLogin());

                    TextView tvRegister = findViewById(R.id.tvRegisterLink);
                    tvRegister.setOnClickListener(v -> {
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    });

                    TextView tvGuestLink = findViewById(R.id.tvGuestLink);
                    tvGuestLink.setOnClickListener(v -> {
                        Intent intent = new Intent(LoginActivity.this, CatalogActivity.class);
                        intent.putExtra("is_guest", true);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        }).start();
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d("LoginActivity", "Попытка входа для email: " + email);

        if (!isEmailValid(email)) {
            showError(etEmail, "Неверный формат email");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Проверка данных...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Log.d("LoginActivity", "Поиск пользователя в базе данных");
                User user = db.userDao().getUserByEmail(email);
                Log.d("LoginActivity", "Результат поиска пользователя: " + (user != null ? "найден" : "не найден"));
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (user != null) {
                        try {
                            Log.d("LoginActivity", "Проверка пароля");
                            if (PasswordHasher.check(password, user.getPasswordHash())) {
                                Log.d("LoginActivity", "Пароль верный, сохранение данных пользователя");
                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("user_id", user.getId());
                                editor.apply();
                                AuthHelper.saveCredentials(this, email, password);

                                // Синхронизируем пользовательские данные
                                Log.d("LoginActivity", "Начало синхронизации данных пользователя");
                                syncManager.performFullSync(user.getId());
                                
                                startActivity(new Intent(this, RecommendationsActivity.class));
                                finish();
                            } else {
                                Log.d("LoginActivity", "Неверный пароль");
                                showError(etPassword, "Неверный пароль");
                            }
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Ошибка проверки пароля", e);
                            showError(etPassword, "Ошибка проверки пароля");
                        }
                    } else {
                        Log.d("LoginActivity", "Пользователь не найден в базе данных");
                        showError(etEmail, "Пользователь не найден");
                    }
                });
            } catch (Exception e) {
                Log.e("LoginActivity", "Ошибка базы данных", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Ошибка базы данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showError(TextInputEditText field, String message) {
        TextInputLayout parent = (TextInputLayout) field.getParent().getParent();
        parent.setError(message);
    }
}
