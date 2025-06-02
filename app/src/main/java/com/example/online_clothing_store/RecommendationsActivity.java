package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.online_clothing_store.Adapter.ProductAdapter;
import com.example.online_clothing_store.Adapter.RecommendationsAdapter;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.dao.ProductDao;
import com.example.online_clothing_store.database.entities.Product;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecommendationsActivity extends AppCompatActivity {

    private RecyclerView newArrivalsList;
    private RecyclerView recommendationsList;
    private TextView tvWelcome;
    private int currentUserId = -1;
    private boolean isGuestMode = false;
    private ProductAdapter newArrivalsAdapter;
    private RecommendationsAdapter recommendedAdapter;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // Получаем ID пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        isGuestMode = getIntent().getBooleanExtra("is_guest", false);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация элементов
        tvWelcome = findViewById(R.id.tvWelcome);
        newArrivalsList = findViewById(R.id.newArrivalsList);
        recommendationsList = findViewById(R.id.recommendationsList);

        // Настройка RecyclerView
        newArrivalsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendationsList.setLayoutManager(new GridLayoutManager(this, 2));

        // Инициализация пула потоков
        executor = Executors.newFixedThreadPool(2);

        // Загрузка данных
        loadProducts();

        // Настройка кнопок навигации
        setupMenuButtons();
    }

    private void loadProducts() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                ProductDao productDao = db.productDao();
                List<Product> newProducts = productDao.getNewArrivals();
                List<Product> recommendedProducts = productDao.getRecommendedProducts();

                runOnUiThread(() -> {
                    // Новые поступления
                    newArrivalsAdapter = new ProductAdapter(this, newProducts, isGuestMode, currentUserId, true);
                    newArrivalsList.setAdapter(newArrivalsAdapter);

                    // Рекомендации
                    recommendedAdapter = new RecommendationsAdapter(this, recommendedProducts, isGuestMode, currentUserId);
                    recommendationsList.setAdapter(recommendedAdapter);
                });
            } catch (Exception e) {
                Log.e("RecommendationsActivity", "Ошибка загрузки продуктов", e);
                runOnUiThread(() -> Toast.makeText(this, "Ошибка загрузки рекомендаций", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupMenuButtons() {
        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            // Уже на главной - обновляем страницу
            loadProducts();
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogActivity.class));
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(this, "Для использования этой функции требуется регистрация", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, WardrobeActivity.class));
            }
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(this, "Для использования этой функции требуется регистрация", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, CartActivity.class));
            }
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            if (isGuestMode) {
                Toast.makeText(this, "Для использования этой функции требуется регистрация", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newArrivalsAdapter != null) {
            newArrivalsAdapter.shutdownExecutor();
        }
        if (recommendedAdapter != null) {
            recommendedAdapter.shutdownExecutor();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
}