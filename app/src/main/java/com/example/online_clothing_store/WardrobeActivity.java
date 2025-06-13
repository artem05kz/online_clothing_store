package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.Cart;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.HashMap;

public class WardrobeActivity extends AppCompatActivity {
    private int currentUserId;
    private List<Product> hats = new ArrayList<>();
    private List<Product> tops = new ArrayList<>();
    private List<Product> bottoms = new ArrayList<>();
    private List<Product> shoes = new ArrayList<>();
    private int hatIndex = 0, topIndex = 0, bottomIndex = 0, shoesIndex = 0;
    private Map<String, List<Product>> categoryProducts = new HashMap<>();

    private ImageView ivHat, ivShirt, ivPants, ivShoes;
    private ImageButton btnHatPrev, btnHatNext, btnShirtPrev, btnShirtNext, btnPantsPrev, btnPantsNext, btnShoesPrev, btnShoesNext;
    private TextView tvTotalPrice;
    private Button btnGenerate;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        // Инициализация базы данных
        db = AppDatabase.getInstance(this);

        // Получение ID пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация UI элементов
        ivHat = findViewById(R.id.ivHat);
        ivShirt = findViewById(R.id.ivShirt);
        ivPants = findViewById(R.id.ivPants);
        ivShoes = findViewById(R.id.ivShoes);

        btnHatPrev = findViewById(R.id.btnHatPrev);
        btnHatNext = findViewById(R.id.btnHatNext);
        btnShirtPrev = findViewById(R.id.btnShirtPrev);
        btnShirtNext = findViewById(R.id.btnShirtNext);
        btnPantsPrev = findViewById(R.id.btnPantsPrev);
        btnPantsNext = findViewById(R.id.btnPantsNext);
        btnShoesPrev = findViewById(R.id.btnShoesPrev);
        btnShoesNext = findViewById(R.id.btnShoesNext);

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnGenerate = findViewById(R.id.btnGenerate);

        // Установка обработчика нажатия на кнопку "Добавить все в корзину"
        btnGenerate.setOnClickListener(v -> addAllToCart());

        // Загрузка избранных товаров и настройка UI
        loadFavoritesAndSetup();
        setupWardrobeUI();
        setupMenuButtons();
        setupImageClicks();
    }

    private void loadFavoritesAndSetup() {
        new Thread(() -> {
            // Очищаем списки перед загрузкой
            hats.clear();
            tops.clear();
            bottoms.clear();
            shoes.clear();
            
            List<Product> favoriteProducts = db.favoriteDao().getFavoriteProductsForUser(currentUserId);
            Log.d("WardrobeActivity", "Загружено избранных товаров: " + favoriteProducts.size());
            for (Product product : favoriteProducts) {
                Log.d("WardrobeActivity", "Товар: id=" + product.getId() + 
                    ", name=" + product.getName() + 
                    ", categoryId=" + product.getCategoryId() + 
                    ", imageUrl=" + product.getMainImageUrl());
                if (product.getCategoryId() != null) {
                    switch (product.getCategoryId()) {
                        case 1: // Шляпы
                            hats.add(product);
                            break;
                        case 2: // Верхняя одежда
                            tops.add(product);
                            break;
                        case 3: // Нижняя одежда
                            bottoms.add(product);
                            break;
                        case 4: // Обувь
                            shoes.add(product);
                            break;
                }
            }
            }
            Log.d("WardrobeActivity", "Распределение по категориям: " +
                "шляпы=" + hats.size() + 
                ", верх=" + tops.size() + 
                ", низ=" + bottoms.size() + 
                ", обувь=" + shoes.size());
            runOnUiThread(this::updateUI);
        }).start();
    }

    private void setupWardrobeUI() {
        ivHat = findViewById(R.id.ivHat);
        ivShirt = findViewById(R.id.ivShirt);
        ivPants = findViewById(R.id.ivPants);
        ivShoes = findViewById(R.id.ivShoes);

        btnHatPrev = findViewById(R.id.btnHatPrev);
        btnHatNext = findViewById(R.id.btnHatNext);
        btnShirtPrev = findViewById(R.id.btnShirtPrev);
        btnShirtNext = findViewById(R.id.btnShirtNext);
        btnPantsPrev = findViewById(R.id.btnPantsPrev);
        btnPantsNext = findViewById(R.id.btnPantsNext);
        btnShoesPrev = findViewById(R.id.btnShoesPrev);
        btnShoesNext = findViewById(R.id.btnShoesNext);

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
    }

    private void updateUI() {
        Log.d("WardrobeActivity", "Обновление UI: " +
            "шляпы=" + hats.size() + " (индекс=" + hatIndex + "), " +
            "верх=" + tops.size() + " (индекс=" + topIndex + "), " +
            "низ=" + bottoms.size() + " (индекс=" + bottomIndex + "), " +
            "обувь=" + shoes.size() + " (индекс=" + shoesIndex + ")");

        showProductInCategory(ivHat, hats, hatIndex, R.drawable.placeholder);
        showProductInCategory(ivShirt, tops, topIndex, R.drawable.placeholder);
        showProductInCategory(ivPants, bottoms, bottomIndex, R.drawable.placeholder);
        showProductInCategory(ivShoes, shoes, shoesIndex, R.drawable.placeholder);
        updateTotalPrice();
    }

    private void showProductInCategory(ImageView iv, List<Product> list, int index, int placeholderRes) {
        if (list.isEmpty()) {
            Log.d("WardrobeActivity", "Список товаров пуст для категории " + iv.getId());
            iv.setVisibility(View.GONE);
            iv.setImageResource(placeholderRes);
            return;
        }

        // Нормализуем индекс
        if (index < 0) index = 0;
        if (index >= list.size()) index = list.size() - 1;

        Product product = list.get(index);
        Log.d("WardrobeActivity", "Показываем товар: id=" + product.getId() + 
            ", name=" + product.getName() + 
            ", imageUrl=" + product.getMainImageUrl());

        iv.setVisibility(View.VISIBLE);
        iv.setImageResource(0);

        // Загружаем изображение
        if (product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
            Picasso.get()
                .load(product.getMainImageUrl())
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .into(iv);
        } else {
            Log.w("WardrobeActivity", "URL изображения пустой для товара id=" + product.getId());
            iv.setVisibility(View.GONE);
                iv.setImageResource(placeholderRes);
        }
    }

    private void switchProduct(List<Product> list, int delta, String type) {
        if (list.isEmpty()) return;
        switch (type) {
            case "hat":
                hatIndex = (hatIndex + delta + list.size()) % list.size();
                showProductInCategory(ivHat, list, hatIndex, R.drawable.placeholder);
                break;
            case "shirt":
                topIndex = (topIndex + delta + list.size()) % list.size();
                showProductInCategory(ivShirt, list, topIndex, R.drawable.placeholder);
                break;
            case "pants":
                bottomIndex = (bottomIndex + delta + list.size()) % list.size();
                showProductInCategory(ivPants, list, bottomIndex, R.drawable.placeholder);
                break;
            case "shoes":
                shoesIndex = (shoesIndex + delta + list.size()) % list.size();
                showProductInCategory(ivShoes, list, shoesIndex, R.drawable.placeholder);
                break;
        }
        updateTotalPrice();
    }

    private void setupMenuButtons() {
        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            startActivity(new Intent(this, RecommendationsActivity.class));
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogActivity.class));
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            Toast.makeText(this, "Вы уже в гардеробе", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void setupImageClicks() {
        btnHatPrev.setOnClickListener(v -> switchProduct(hats, -1, "hat"));
        btnHatNext.setOnClickListener(v -> switchProduct(hats, 1, "hat"));
        btnShirtPrev.setOnClickListener(v -> switchProduct(tops, -1, "shirt"));
        btnShirtNext.setOnClickListener(v -> switchProduct(tops, 1, "shirt"));
        btnPantsPrev.setOnClickListener(v -> switchProduct(bottoms, -1, "pants"));
        btnPantsNext.setOnClickListener(v -> switchProduct(bottoms, 1, "pants"));
        btnShoesPrev.setOnClickListener(v -> switchProduct(shoes, -1, "shoes"));
        btnShoesNext.setOnClickListener(v -> switchProduct(shoes, 1, "shoes"));

        ivHat.setOnClickListener(v -> openProductDetail(hats, hatIndex));
        ivShirt.setOnClickListener(v -> openProductDetail(tops, topIndex));
        ivPants.setOnClickListener(v -> openProductDetail(bottoms, bottomIndex));
        ivShoes.setOnClickListener(v -> openProductDetail(shoes, shoesIndex));
    }

    private void openProductDetail(List<Product> list, int index) {
        if (list.isEmpty() || index < 0 || index >= list.size()) return;
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", list.get(index));
        startActivity(intent);
    }

    private void updateTotalPrice() {
        double total = 0;
        if (!hats.isEmpty()) total += hats.get(hatIndex).getPrice();
        if (!tops.isEmpty()) total += tops.get(topIndex).getPrice();
        if (!bottoms.isEmpty()) total += bottoms.get(bottomIndex).getPrice();
        if (!shoes.isEmpty()) total += shoes.get(shoesIndex).getPrice();
        tvTotalPrice.setText(String.format("%.2f ₽", total));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritesAndSetup();
    }

    private void addAllToCart() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Для добавления в корзину требуется авторизация", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("WardrobeActivity", "Начало добавления в корзину. Текущие индексы: " +
            "шляпы=" + hatIndex + " (размер=" + hats.size() + "), " +
            "верх=" + topIndex + " (размер=" + tops.size() + "), " +
            "низ=" + bottomIndex + " (размер=" + bottoms.size() + "), " +
            "обувь=" + shoesIndex + " (размер=" + shoes.size() + ")");

        // Проверяем валидность индексов
        if (!hats.isEmpty() && (hatIndex < 0 || hatIndex >= hats.size())) {
            Log.e("WardrobeActivity", "Некорректный индекс для шляп: " + hatIndex);
            hatIndex = 0;
        }
        if (!tops.isEmpty() && (topIndex < 0 || topIndex >= tops.size())) {
            Log.e("WardrobeActivity", "Некорректный индекс для верха: " + topIndex);
            topIndex = 0;
        }
        if (!bottoms.isEmpty() && (bottomIndex < 0 || bottomIndex >= bottoms.size())) {
            Log.e("WardrobeActivity", "Некорректный индекс для низа: " + bottomIndex);
            bottomIndex = 0;
        }
        if (!shoes.isEmpty() && (shoesIndex < 0 || shoesIndex >= shoes.size())) {
            Log.e("WardrobeActivity", "Некорректный индекс для обуви: " + shoesIndex);
            shoesIndex = 0;
        }

        new Thread(() -> {
            try {
                final boolean[] addedAny = {false};
                
                // Добавляем головной убор
                if (!hats.isEmpty()) {
                    Product hat = hats.get(hatIndex);
                    Cart cartItem = new Cart();
                    cartItem.setUserId(currentUserId);
                    cartItem.setProductId(hat.getId());
                    cartItem.setQuantity(1);
                    cartItem.setPrice(hat.getPrice());
                    db.cartDao().insert(cartItem);
                    addedAny[0] = true;
                    Log.d("WardrobeActivity", "Добавлена шапка в корзину: " + hat.getName() + " (id=" + hat.getId() + ")");
                }

                // Добавляем верхнюю одежду
                if (!tops.isEmpty()) {
                    Product top = tops.get(topIndex);
                    Cart cartItem = new Cart();
                    cartItem.setUserId(currentUserId);
                    cartItem.setProductId(top.getId());
                    cartItem.setQuantity(1);
                    cartItem.setPrice(top.getPrice());
                    db.cartDao().insert(cartItem);
                    addedAny[0] = true;
                    Log.d("WardrobeActivity", "Добавлена верхняя одежда в корзину: " + top.getName() + " (id=" + top.getId() + ")");
                }

                // Добавляем нижнюю одежду
                if (!bottoms.isEmpty()) {
                    Product bottom = bottoms.get(bottomIndex);
                    Cart cartItem = new Cart();
                    cartItem.setUserId(currentUserId);
                    cartItem.setProductId(bottom.getId());
                    cartItem.setQuantity(1);
                    cartItem.setPrice(bottom.getPrice());
                    db.cartDao().insert(cartItem);
                    addedAny[0] = true;
                    Log.d("WardrobeActivity", "Добавлена нижняя одежда в корзину: " + bottom.getName() + " (id=" + bottom.getId() + ")");
                }

                // Добавляем обувь
                if (!shoes.isEmpty()) {
                    Product shoe = shoes.get(shoesIndex);
                    Cart cartItem = new Cart();
                    cartItem.setUserId(currentUserId);
                    cartItem.setProductId(shoe.getId());
                    cartItem.setQuantity(1);
                    cartItem.setPrice(shoe.getPrice());
                    db.cartDao().insert(cartItem);
                    addedAny[0] = true;
                    Log.d("WardrobeActivity", "Добавлена обувь в корзину: " + shoe.getName() + " (id=" + shoe.getId() + ")");
                }

                runOnUiThread(() -> {
                    if (addedAny[0]) {
                        Toast.makeText(this, "Выбранные товары добавлены в корзину", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Нет выбранных товаров для добавления в корзину", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("WardrobeActivity", "Ошибка при добавлении в корзину", e);
                runOnUiThread(() -> Toast.makeText(this, "Ошибка при добавлении в корзину", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadProductsForCategory(String category) {
        Log.d("WardrobeActivity", "Загрузка товаров для категории: " + category);
        
        // Получаем список товаров из базы данных
        List<Product> products = db.favoriteDao().getFavoriteProductsForUser(currentUserId);
        Log.d("WardrobeActivity", "Получено товаров из БД: " + products.size());
        
        // Выводим информацию о каждом товаре
        for (Product product : products) {
            Log.d("WardrobeActivity", String.format(
                "Товар: id=%d, name=%s, categoryId=%d, imageUrl=%s",
                product.getId(),
                product.getName(),
                product.getCategoryId(),
                product.getMainImageUrl()
            ));
        }
        
        // Сохраняем список товаров
        categoryProducts.put(category, products);
        
        // Обновляем отображение
        updateUI();
    }
}
