package com.example.online_clothing_store;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.online_clothing_store.api.ApiClient;
import com.example.online_clothing_store.api.ApiService;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.User;

import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private EditText nameInput, addressInput;
    private Button saveButton, changeAccountButton;
    private int currentUserId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        saveButton = findViewById(R.id.saveButton);
        changeAccountButton = findViewById(R.id.changeAccountButton);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);

        // Загрузка текущих данных пользователя
        new Thread(() -> {
            User user = db.userDao().getUserById(currentUserId);
            runOnUiThread(() -> {
                if (user != null) {
                    nameInput.setText(user.getName());
                    addressInput.setText(user.getAddress());
                }
            });
        }).start();

        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newAddress = addressInput.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите имя", Toast.LENGTH_SHORT).show();
                return;
            }

            // Обновляем локально
            Executors.newSingleThreadExecutor().execute(() -> {
                User user = db.userDao().getUserById(currentUserId);
                user.setName(newName);
                user.setAddress(newAddress);
                db.userDao().update(user);

                // Синхронизация с сервером
                runOnUiThread(() -> {
                    ApiService apiService = ApiClient.getClient().create(ApiService.class);
                    apiService.updateUser(user.getId(), user).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful()) {
                                Executors.newSingleThreadExecutor().execute(() -> db.userDao().insert(response.body()));
                                Toast.makeText(EditProfileActivity.this, "Профиль обновлён", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(EditProfileActivity.this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        });

        changeAccountButton.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finishAffinity();
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}