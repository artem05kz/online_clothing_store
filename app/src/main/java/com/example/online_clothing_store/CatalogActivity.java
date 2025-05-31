package com.example.online_clothing_store;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Category;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.ProductImage;
import com.example.online_clothing_store.database.dao.ProductDao;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {

    private RecyclerView productsGrid;
    private boolean isGuestMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Проверяем, вошел ли пользователь как гость
        isGuestMode = getIntent().getBooleanExtra("is_guest", false);

        // Инициализация RecyclerView
        productsGrid = findViewById(R.id.productsGrid);
        productsGrid.setLayoutManager(new GridLayoutManager(this, 2));

        // Загрузка товаров
        loadProducts();

        // Настройка кнопок меню
        setupMenuButtons();
    }

    private void loadProducts() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            ProductDao productDao = db.productDao();
            createCategories(db);
            // Создаем и сохраняем продукты
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
            product1.setId((int) productId1); // Обновляем ID продукта

            Product product2 = new Product(
                    "Джинсы Levi's",
                    2999,
                    "Классические прямые джинсы",
                    "32",
                    "Levi's",
                    4.8,
                    "https://i.pinimg.com/736x/95/7e/38/957e384285f86c792ab4d84c6ad5b1bd.jpg",
                    "98% хлопок, 2% эластан",
                    2
            );;
            long productId2 = productDao.insertProduct(product2);
            product2.setId((int) productId2);

            // Получаем список продуктов из БД
            List<Product> productList = productDao.getAllProducts(); // Нужно добавить этот метод в DAO

            // Обновляем UI на главном потоке
            runOnUiThread(() -> {
                ProductAdapter adapter = new ProductAdapter(productList, isGuestMode);
                productsGrid.setAdapter(adapter);
            });

            // Сохраняем изображения
            saveAdditionalImages((int) productId1, Arrays.asList(
                    "https://i.pinimg.com/736x/70/6c/82/706c82553371adde376d2a160a23da91.jpg",
                    "https://i.pinimg.com/736x/0a/69/a8/0a69a83720f021e4c28c9eecb846e8b9.jpg"
            ));
        }).start();
    }
    private void saveAdditionalImages(int productId, List<String> imageUrls) {
        new Thread(() -> {
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
        }).start();
    }
    private void createCategories(AppDatabase db) {
        // Проверяем, есть ли уже категории
        if (db.categoryDao().getCategoryCount() == 0) {
            Category menCategory = new Category("Мужская одежда");
            Category womenCategory = new Category("Женская одежда");

            db.categoryDao().insertAll(menCategory, womenCategory);
        }
    }
    private void setupMenuButtons() {
        // Обработчики для нижнего меню
        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            // Уже на главной
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            showGuestMessage("Требуется регистрация");
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            showGuestMessage("Требуется регистрация");
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            showGuestMessage("Требуется регистрация");
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            if (isGuestMode) {
                // Переход на экран авторизации
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                // Переход в профиль
            }
        });
    }

    private void showGuestMessage(String message) {
        if (isGuestMode) {
            Toast.makeText(this, "Гостевой режим: " + message, Toast.LENGTH_SHORT).show();
        }
    }
}