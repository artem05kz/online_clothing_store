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
                createCategories(db);
                List<Product> productList = productDao.getAllProducts();
                if (productList.isEmpty()) {
                    Product product1 = new Product(
                            "Футболка NIKE",
                            1999,
                            "Классическая футболка из мягкого хлопка",
                            "M",
                            "Nike",
                            4.7,
                            "https://i.pinimg.com/736x/d5/66/a8/d566a8cda6e5bfb7ee9261885f5b9ea2.jpg",
                            "100% хлопок",
                            2
                    );
                    long productId1 = productDao.insertProduct(product1);
                    product1.setId((int) productId1);

                    Product product2 = new Product(
                            "Джинсы Levi's",
                            2999,
                            "Джинсы",
                            "32",
                            "Levi's",
                            4.9,
                            "https://i.pinimg.com/736x/fb/5d/ff/fb5dff754e24426ab01d6c67b867d8d8.jpg",
                            "98% хлопок, 2% эластан",
                            3
                    );
                    long productId2 = productDao.insertProduct(product2);
                    product2.setId((int) productId2);

                    // Новые товары:
                    Product product3 = new Product(
                            "Кепка",
                            1200,
                            "Стильная кепка для города и спорта",
                            "L",
                            "Adidas",
                            4.5,
                            "https://i.pinimg.com/736x/e5/a8/b5/e5a8b5c3c07ed30cf53fbb2b1e91db80.jpg",
                            "100% хлопок",
                            1
                    );
                    long productId3 = productDao.insertProduct(product3);
                    product3.setId((int) productId3);

                    Product product4 = new Product(
                            "Кеды Converse",
                            3500,
                            "Классические кеды для повседневной носки",
                            "42",
                            "Converse",
                            4.8,
                            "https://i.pinimg.com/736x/71/f0/db/71f0db5e25de58caabbaae9c72809bda.jpg",
                            "Текстиль, резина",
                            4
                    );
                    long productId4 = productDao.insertProduct(product4);
                    product4.setId((int) productId4);

                    // Сохранение дополнительных изображений для футболки
                    saveAdditionalImages((int) productId1, Arrays.asList(
                            "https://i.pinimg.com/736x/4c/f6/f0/4cf6f0f06ffca64af51fe14f461e76a9.jpg"
                    ));

                    productList = productDao.getAllProducts();
                }
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