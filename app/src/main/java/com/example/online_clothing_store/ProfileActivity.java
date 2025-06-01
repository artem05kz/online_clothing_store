package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.dao.FavoriteDao;
import com.example.online_clothing_store.database.dao.OrderDao;
import com.example.online_clothing_store.database.dao.UserDao;
import com.example.online_clothing_store.database.entities.Favorite;
import com.example.online_clothing_store.database.entities.Order;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.User;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvAddress;
    private LinearLayout favoritesContainer, ordersContainer;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Получаем ID пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvUserName = findViewById(R.id.tvUserName);
        tvAddress = findViewById(R.id.tvAddress);
        favoritesContainer = findViewById(R.id.favoritesContainer);
        ordersContainer = findViewById(R.id.ordersContainer);

        // Кнопка редактирования профиля
        ImageButton ibEdit = findViewById(R.id.ibEdit);
        ibEdit.setOnClickListener(v -> {
            // Здесь будет переход на экран редактирования профиля
            Toast.makeText(ProfileActivity.this,
                    "Редактирование профиля",
                    Toast.LENGTH_SHORT).show();
        });
        setupMenuButtons();
        loadUserData();
        loadFavorites();
        loadOrders();
    }

    private void loadUserData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            UserDao userDao = db.userDao();
            User user = userDao.getUserById(currentUserId);

            runOnUiThread(() -> {
                if (user != null) {
                    tvUserName.setText(user.getName());
                    tvAddress.setText(user.getAddress() != null ? user.getAddress() : "Адрес не указан");
                }
            });
        }).start();
    }

    private void loadFavorites() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            FavoriteDao favoriteDao = db.favoriteDao();
            List<Product> favoriteProducts = favoriteDao.getFavoriteProductsForUser(currentUserId);

            runOnUiThread(() -> {
                favoritesContainer.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);

                for (Product product : favoriteProducts) {
                    View favoriteView = inflater.inflate(R.layout.item_favorite, favoritesContainer, false);

                    TextView tvProductName = favoriteView.findViewById(R.id.tvProductName);
                    TextView tvProductPrice = favoriteView.findViewById(R.id.tvProductPrice);
                    ImageView ivProductImage = favoriteView.findViewById(R.id.ivProductImage);

                    tvProductName.setText(product.getName());
                    tvProductPrice.setText(String.format("%.2f ₽", product.getPrice()));

                    if (product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
                        Picasso.get()
                                .load(product.getMainImageUrl())
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.error)
                                .into(ivProductImage);
                    } else {
                        ivProductImage.setImageResource(R.drawable.error);
                    }

                    favoriteView.setOnClickListener(v -> {
                        Intent intent = new Intent(ProfileActivity.this, ProductDetailActivity.class);
                        intent.putExtra("product", product);
                        startActivity(intent);
                    });

                    favoritesContainer.addView(favoriteView);
                }
            });
        }).start();
    }

    private void loadOrders() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            OrderDao orderDao = db.orderDao();
            List<Order> orders = orderDao.getOrdersByUserId(currentUserId);

            runOnUiThread(() -> {
                ordersContainer.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);

                for (Order order : orders) {
                    View orderView = inflater.inflate(R.layout.item_order, ordersContainer, false);

                    TextView tvOrderId = orderView.findViewById(R.id.tvOrderId);
                    TextView tvOrderDate = orderView.findViewById(R.id.tvOrderDate);
                    TextView tvOrderStatus = orderView.findViewById(R.id.tvOrderStatus);

                    tvOrderId.setText(String.format("Заказ #%d", order.getId()));
                    tvOrderDate.setText(order.getOrderDate());
                    tvOrderStatus.setText(order.getStatus());

                    ordersContainer.addView(orderView);
                }
            });
        }).start();
    }
    private void setupMenuButtons() {
        // Обработчики для нижнего меню
        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            // Уже на главной - обновляем страницу
            loadUserData();
            loadFavorites();
            loadOrders();
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            // Переход в каталог для авторизованных
            startActivity(new Intent(this, CatalogActivity.class));
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            // Переход в гардероб
            startActivity(new Intent(this, WardrobeActivity.class));
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            // Переход в корзину
            startActivity(new Intent(this, CartActivity.class));
        });

        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            // Переход в профиль
            startActivity(new Intent(this, RecommendationsActivity.class));
        });
    }
}