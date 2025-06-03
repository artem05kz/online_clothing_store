package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.online_clothing_store.api.ApiClient;
import com.example.online_clothing_store.api.ApiService;
import com.example.online_clothing_store.database.entities.User;
import com.example.online_clothing_store.sync.SyncHelper;
import com.example.online_clothing_store.utils.AuthHelper;
import com.example.online_clothing_store.utils.PasswordHasher;
import com.example.online_clothing_store.database.AppDatabase;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;

public class RegisterActivity extends AppCompatActivity {
    private AppDatabase db;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button registerButton = findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(v -> attemptRegistration());

        TextView tvRegister = findViewById(R.id.tvLoginLink);
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    private void attemptRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!validateInputs(name, email, password, confirmPassword)) return;

        // Создаём пользователя без id
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPasswordHash(PasswordHasher.hash(password));

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.registerUser(newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User registeredUser = response.body();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        db.userDao().insert(registeredUser);
                    });
                    runOnUiThread(() -> {
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        prefs.edit().putInt("user_id", registeredUser.getId()).apply();
                        AuthHelper.saveCredentials(RegisterActivity.this, registeredUser.getEmail(), password);
                        Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Ошибка регистрации на сервере", Toast.LENGTH_SHORT).show());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (name.isEmpty()) {
            showError(etName, "Введите имя");
            isValid = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, "Неверный формат email");
            isValid = false;
        }

        if (password.length() < 6) {
            showError(etPassword, "Пароль должен быть не менее 6 символов");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, "Пароли не совпадают");
            isValid = false;
        }

        return isValid;
    }

    private void showError(TextInputEditText field, String message) {
        TextInputLayout parent = (TextInputLayout) field.getParent().getParent();
        parent.setError(message);
    }
}