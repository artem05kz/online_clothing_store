package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Favorite;
import com.example.online_clothing_store.database.entities.Order;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.User;
import com.example.online_clothing_store.sync.SyncHelper;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private TextView tvUserName, tvAddress;
    private LinearLayout favoritesContainer, ordersContainer;
    private int currentUserId;
    private ImageButton ibEdit;
    private SyncHelper syncHelper;
    private AppDatabase db;
    private TextView tvFavoritesCount, tvCartCount, tvOrdersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupClickListeners();
        loadUserData();
        loadFavorites();
        loadOrderHistory();

        syncHelper = new SyncHelper(this);
    }

    private void initializeViews() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        tvUserName = findViewById(R.id.tvUserName);
        tvAddress = findViewById(R.id.tvAddress);
        favoritesContainer = findViewById(R.id.favoritesContainer);
        ordersContainer = findViewById(R.id.ordersContainer);
        ibEdit = findViewById(R.id.ibEdit);
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount);
        tvCartCount = findViewById(R.id.tvCartCount);
        tvOrdersCount = findViewById(R.id.tvOrdersCount);
    }

    private void setupClickListeners() {
        if (ibEdit != null) {
            ibEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        setupMenuButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncUserData();
        loadUserData();
        loadOrderHistory();
        loadFavorites();
    }

    private void syncUserData() {
        Log.d(TAG, "Загрузка данных из локальной БД");
        loadUserData();
    }

    private void loadUserData() {
        Log.d(TAG, "Загрузка данных пользователя");
        int userId = getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            try {
                User user = db.userDao().getUserById(userId);
                List<Favorite> favorites = db.favoriteDao().getFavoritesByUserId(userId);
                List<Cart> cartItems = db.cartDao().getCartItemsByUserId(userId);
                List<Order> orders = db.orderDao().getOrdersByUserId(userId);

                runOnUiThread(() -> {
                    if (user != null) {
                        if (tvUserName != null) {
                            tvUserName.setText(user.getName());
                        }
                        if (tvAddress != null) {
                            tvAddress.setText(user.getAddress() != null ? user.getAddress() : "Адрес не указан");
                        }
                    }
                    updateFavoritesCount(favorites.size());
                    updateCartCount(cartItems.size());
                    updateOrdersCount(orders.size());
                    Log.d(TAG, "Данные пользователя загружены и отображены");
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки данных", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadFavorites() {
        if (favoritesContainer == null) return;

        new Thread(() -> {
            try {
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

                        if (tvProductName != null) {
                            tvProductName.setText(product.getName());
                        }
                        if (tvProductPrice != null) {
                            tvProductPrice.setText(String.format("%.2f ₽", product.getPrice()));
                        }

                        if (ivProductImage != null && product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
                            Picasso.get()
                                    .load(product.getMainImageUrl())
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.error)
                                    .into(ivProductImage);
                        } else if (ivProductImage != null) {
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
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки избранного", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadOrderHistory() {
        if (ordersContainer == null) return;

        Log.d(TAG, "Начало загрузки истории заказов для пользователя " + currentUserId);
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                List<Order> orders = db.orderDao().getOrdersByUserId(currentUserId);
                Log.d(TAG, "Получено заказов из БД: " + orders.size());
                
                runOnUiThread(() -> {
                    ordersContainer.removeAllViews();
                    if (orders.isEmpty()) {
                        TextView emptyView = new TextView(this);
                        emptyView.setText("У вас пока нет заказов");
                        emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        emptyView.setPadding(0, 20, 0, 20);
                        ordersContainer.addView(emptyView);
                        Log.d(TAG, "Отображено сообщение об отсутствии заказов");
                        return;
                    }

                    for (Order order : orders) {
                        Log.d(TAG, "Обработка заказа: id=" + order.getId() + 
                            ", status=" + order.getStatus() + 
                            ", address=" + order.getAddress());
                            
                        View orderCard = LayoutInflater.from(this)
                                .inflate(R.layout.item_order, ordersContainer, false);
                        TextView tvOrderId = orderCard.findViewById(R.id.tvOrderId);
                        TextView tvOrderDate = orderCard.findViewById(R.id.tvOrderDate);
                        TextView tvOrderStatus = orderCard.findViewById(R.id.tvOrderStatus);

                        if (tvOrderId != null) {
                            tvOrderId.setText("Заказ #" + order.getId());
                        }
                        if (tvOrderDate != null) {
                            tvOrderDate.setText(order.getAddress());
                        }
                        if (tvOrderStatus != null) {
                            tvOrderStatus.setText(order.getStatus());
                        }

                        ordersContainer.addView(orderCard);
                    }
                    Log.d(TAG, "Заказы успешно отображены");
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки истории заказов", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки истории заказов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void setupMenuButtons() {
        View profileButton = findViewById(R.id.imageButtonProfile);
        View hangerButton = findViewById(R.id.imageButtonHanger);
        View wardrobeButton = findViewById(R.id.imageButtonWardrobe);
        View cartButton = findViewById(R.id.imageButtonCart);
        View homeButton = findViewById(R.id.imageButtonHome);

        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                loadUserData();
                loadFavorites();
                loadOrderHistory();
            });
        }

        if (hangerButton != null) {
            hangerButton.setOnClickListener(v -> {
                startActivity(new Intent(this, CatalogActivity.class));
            });
        }

        if (wardrobeButton != null) {
            wardrobeButton.setOnClickListener(v -> {
                startActivity(new Intent(this, WardrobeActivity.class));
            });
        }

        if (cartButton != null) {
            cartButton.setOnClickListener(v -> {
                startActivity(new Intent(this, CartActivity.class));
            });
        }

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                startActivity(new Intent(this, RecommendationsActivity.class));
            });
        }
    }

    private int getUserId() {
        return currentUserId;
    }

    private void updateFavoritesCount(int count) {
        if (tvFavoritesCount != null) {
            tvFavoritesCount.setText(String.valueOf(count));
        }
    }

    private void updateCartCount(int count) {
        if (tvCartCount != null) {
            tvCartCount.setText(String.valueOf(count));
        }
    }

    private void updateOrdersCount(int count) {
        if (tvOrdersCount != null) {
            tvOrdersCount.setText(String.valueOf(count));
        }
    }
}