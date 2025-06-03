package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.online_clothing_store.Adapter.ProductAdapter;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Category;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.ProductImage;
import com.example.online_clothing_store.database.dao.ProductDao;
import com.example.online_clothing_store.sync.SyncHelper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CatalogActivity extends AppCompatActivity {
    private int currentUserId = -1;
    private RecyclerView productsGrid;
    private boolean isGuestMode = false;
    private ProductAdapter adapter;
    private ExecutorService executor;
    private Spinner spinnerCategory;
    private Spinner spinnerSort;
    private List<Product> allProducts = new ArrayList<>();
    private int selectedCategory = 0; // 0 - все
    private int selectedSort = 0; // 0 - без сортировки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Получение ID пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        isGuestMode = getIntent().getBooleanExtra("is_guest", false);

        // Инициализация RecyclerView
        productsGrid = findViewById(R.id.productsGrid);
        productsGrid.setLayoutManager(new GridLayoutManager(this, 2));

        // Инициализация пула потоков
        executor = Executors.newFixedThreadPool(2);

        // Инициализация Spinner'ов
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSort = findViewById(R.id.spinnerSort);
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this, R.array.category_names, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this, R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = position;
                updateProductList();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedSort = position;
                updateProductList();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Загрузка продуктов
        loadProducts();

        // Настройка кнопок навигации
        setupMenuButtons();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                ProductDao productDao = db.productDao();
                createCategories(db);
                List<Product> productList = productDao.getAllProducts();
                allProducts = new ArrayList<>(productList);
                runOnUiThread(this::updateProductList);
            } catch (Exception e) {
                Log.e("CatalogActivity", "Ошибка загрузки продуктов", e);
                runOnUiThread(() -> Toast.makeText(this, "Ошибка загрузки каталога", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateProductList() {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (selectedCategory == 0 || (p.getCategoryId() != null && p.getCategoryId() == selectedCategory)) {
                filtered.add(p);
            }
        }
        // Сортировка
        switch (selectedSort) {
            case 1: // По рейтингу
                Collections.sort(filtered, (a, b) -> Double.compare(b.getRating(), a.getRating()));
                break;
            case 2: // По цене (возрастание)
                Collections.sort(filtered, Comparator.comparingDouble(Product::getPrice));
                break;
            case 3: // По цене (убывание)
                Collections.sort(filtered, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
        }
        adapter = new ProductAdapter(this, filtered, isGuestMode, currentUserId);
        productsGrid.setAdapter(adapter);
    }

    private void saveAdditionalImages(int productId, List<String> imageUrls) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                ProductDao productDao = db.productDao();

                // Проверяем существование продукта
                if (productDao.getProductById(productId) != null) {
                    for (String url : imageUrls) {
                        ProductImage image = new ProductImage(productId, url);
                        productDao.insertProductImage(image);
                    }
                } else {
                    Log.e("CatalogActivity", "Product ID " + productId + " does not exist");
                }
            } catch (Exception e) {
                Log.e("CatalogActivity", "Ошибка сохранения изображений", e);
            }
        });
    }

    private void createCategories(AppDatabase db) {
        try {
            if (db.categoryDao().getCategoryCount() == 0) {
                Category hats = new Category("Головные уборы");
                Category tops = new Category("Верхняя одежда");
                Category bottoms = new Category("Нижняя одежда");
                Category shoes = new Category("Обувь");
                db.categoryDao().insertAll(hats, tops, bottoms, shoes);
            }
        } catch (Exception e) {
            Log.e("CatalogActivity", "Ошибка создания категорий", e);
        }
    }

    private void setupMenuButtons() {
        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            if (isGuestMode) {
                showGuestMessage("Для использования этой функции требуется регистрация");
            } else {
                startActivity(new Intent(this, RecommendationsActivity.class));
            }
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            if (isGuestMode) {
                showGuestMessage("Для использования этой функции требуется регистрация");
            } else {
                // Уже в CatalogActivity, обновляем продукты
                loadProducts();
            }
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            if (isGuestMode) {
                showGuestMessage("Для использования этой функции требуется регистрация");
            } else {
                startActivity(new Intent(this, WardrobeActivity.class));
            }
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            if (isGuestMode) {
                showGuestMessage("Для использования этой функции требуется регистрация");
            } else {
                startActivity(new Intent(this, CartActivity.class));
            }
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            if (isGuestMode) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });
    }

    private void showGuestMessage(String message) {
        Toast.makeText(this, "Гостевой режим: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.shutdownExecutor(); // Очистка пула потоков адаптера
        }
        if (executor != null) {
            executor.shutdown(); // Очистка пула потоков активности
        }
    }
}