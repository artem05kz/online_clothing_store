package com.example.online_clothing_store;

import android.os.Bundle;
import android.widget.TextView;
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
import java.util.concurrent.Executors;

public class RecommendationsActivity extends AppCompatActivity {

    private RecyclerView newArrivalsList;
    private RecyclerView recommendationsList;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

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

        // Загрузка данных
        loadProducts();
        setupMenuButtons();
    }

    private void loadProducts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            ProductDao productDao = db.productDao();
            List<Product> newProducts = productDao.getNewArrivals();
            List<Product> recommendedProducts = productDao.getRecommendedProducts();

            runOnUiThread(() -> {
                // Новые поступления
                ProductAdapter newArrivalsAdapter = new ProductAdapter(newProducts, false);
                newArrivalsList.setAdapter(newArrivalsAdapter);

                // Рекомендации
                RecommendationsAdapter recommendedAdapter = new RecommendationsAdapter(recommendedProducts, this);
                recommendationsList.setAdapter(recommendedAdapter);
            });
        });
    }

    private void setupMenuButtons() {
        // Обработчики для нижнего меню
        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            // Уже на главной
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            // Переход к подбору одежды
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            // Переход к гардеробу
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            // Переход в корзину
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            // Переход в профиль
        });
    }
}