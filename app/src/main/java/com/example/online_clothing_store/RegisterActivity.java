package com.example.online_clothing_store;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.online_clothing_store.database.entities.User;
import com.example.online_clothing_store.utils.PasswordHasher;
import com.example.online_clothing_store.database.AppDatabase;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;

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

        Executors.newSingleThreadExecutor().execute(() -> {
            if (db.userDao().getUserByEmail(email) != null) {
                runOnUiThread(() ->
                        showError(etEmail, "Этот email уже зарегистрирован"));
                return;
            }

            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPasswordHash(password);

            db.userDao().insert(newUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
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