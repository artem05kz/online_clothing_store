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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CatalogActivity extends AppCompatActivity {
    private int currentUserId = -1;
    private RecyclerView productsGrid;
    private boolean isGuestMode = false;
    private ProductAdapter adapter;
    private ExecutorService executor;

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

                // Создание категорий, если их нет
                createCategories(db);

                // Проверка, есть ли продукты в базе
                List<Product> productList = productDao.getAllProducts();
                if (productList.isEmpty()) {
                    // Создание и сохранение продуктов
                    Product product1 = new Product(
                            "Футболка NIKE",
                            1999,
                            "Классическая футболка из мягкого хлопка",
                            "M",
                            "Nike",
                            4.7,
                            "https://i.pinimg.com/736x/e3/d0/95/e3d0952fd4a14fb6b6e257874ec8f873.jpg",
                            "100% хлопок",
                            1
                    );
                    long productId1 = productDao.insertProduct(product1);
                    product1.setId((int) productId1);

                    Product product2 = new Product(
                            "Джинсы Levi's",
                            2999,
                            "Классические прямые джинсы",
                            "32",
                            "Levi's",
                            4.9,
                            "https://i.pinimg.com/736x/95/7e/38/957e384285f86c792ab4d84c6ad5b1bd.jpg",
                            "98% хлопок, 2% эластан",
                            2
                    );
                    long productId2 = productDao.insertProduct(product2);
                    product2.setId((int) productId2);

                    // Сохранение дополнительных изображений
                    saveAdditionalImages((int) productId1, Arrays.asList(
                            "https://i.pinimg.com/736x/70/6c/82/706c82553371adde376d2a160a23da91.jpg",
                            "https://i.pinimg.com/736x/0a/69/a8/0a69a83720f021e4c28c9eecb846e8b9.jpg"
                    ));

                    // Обновляем список продуктов
                    productList = productDao.getAllProducts();
                }

                // Обновляем UI на главном потоке
                List<Product> finalProductList = productList;
                runOnUiThread(() -> {
                    adapter = new ProductAdapter(this, finalProductList, isGuestMode, currentUserId);
                    productsGrid.setAdapter(adapter);
                });
            } catch (Exception e) {
                Log.e("CatalogActivity", "Ошибка загрузки продуктов", e);
                runOnUiThread(() -> Toast.makeText(this, "Ошибка загрузки каталога", Toast.LENGTH_SHORT).show());
            }
        });
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
            // Проверяем, есть ли уже категории
            if (db.categoryDao().getCategoryCount() == 0) {
                Category menCategory = new Category("Мужская одежда");
                Category womenCategory = new Category("Женская одежда");
                db.categoryDao().insertAll(menCategory, womenCategory);
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