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
import com.example.online_clothing_store.sync.SyncHelper;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            new SyncHelper(this).syncUsers();
            User user = AuthHelper.tryAutoLogin(this);

            runOnUiThread(() -> {
                if (user != null) {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().putInt("user_id", user.getId()).apply();
                    AuthHelper.saveCredentials(this, user.getEmail(), user.getPasswordHash());

                    Executors.newSingleThreadExecutor().execute(() -> {
                        SyncHelper syncHelper = new SyncHelper(this);
                        syncHelper.syncFavorites(user.getId());
                        syncHelper.syncCart(user.getId());
                        syncHelper.syncOrders(user.getId());
                        syncHelper.syncPromos();
                        syncHelper.syncProducts();
                    });

                    startActivity(new Intent(this, RecommendationsActivity.class));
                    finish();
                } else {
                    setContentView(R.layout.activity_login);
                    db = AppDatabase.getInstance(this);
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
                User user = db.userDao().getUserByEmail(email);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (user != null) {
                        try {
                            if (PasswordHasher.check(password, user.getPasswordHash())) {
                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("user_id", user.getId());
                                editor.apply();
                                AuthHelper.saveCredentials(this, email, password);

                                // Выполняем полную синхронизацию только при входе
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    try {
                                        SyncHelper syncHelper = new SyncHelper(this);
                                        syncHelper.syncFavorites(user.getId());
                                        syncHelper.syncCart(user.getId());
                                        syncHelper.syncOrders(user.getId());
                                        syncHelper.syncPromos();
                                        syncHelper.syncProducts();
                                        
                                        runOnUiThread(() -> {
                                            startActivity(new Intent(this, RecommendationsActivity.class));
                                            finish();
                                        });
                                    } catch (Exception e) {
                                        Log.e("LoginActivity", "Ошибка синхронизации", e);
                                        runOnUiThread(() -> {
                                            Toast.makeText(this, "Ошибка синхронизации данных", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                            } else {
                                showError(etPassword, "Неверный пароль");
                            }
                        } catch (Exception e) {
                            showError(etPassword, "Ошибка проверки пароля");
                            Log.e("Login", "Password check error", e);
                        }
                    } else {
                        showError(etEmail, "Пользователь не найден");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Ошибка базы данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LoginActivity", "DB error", e);
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
