package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.dao.FavoriteDao;
import com.example.online_clothing_store.database.entities.Cart;
import com.example.online_clothing_store.database.entities.Favorite;
import com.example.online_clothing_store.database.entities.Product;
import com.example.online_clothing_store.database.entities.ProductImage;
import me.relex.circleindicator.CircleIndicator3;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        Product product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Ошибка: продукт не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewPager2 viewPager = findViewById(R.id.productImagesPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);

        loadProductImages(product, adapter, viewPager);

        TextView title = findViewById(R.id.productTitle);
        TextView price = findViewById(R.id.productPrice);
        TextView rating = findViewById(R.id.productRating);
        TextView description = findViewById(R.id.productDescription);
        TextView composition = findViewById(R.id.productComposition);

        title.setText(product.getName());
        price.setText(String.format("%.2f ₽", product.getPrice()));
        rating.setText(String.valueOf(product.getRating()));
        description.setText(product.getDescription());
        composition.setText(product.getComposition());

        Button addToCart = findViewById(R.id.addToCartButton);
        addToCart.setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(ProductDetailActivity.this);
                List<Cart> existingItems = db.cartDao().getCartItemsByUserId(currentUserId);
                boolean productExists = false;
                for (Cart item : existingItems) {
                    if (item.getProductId() == product.getId()) {
                        item.setQuantity(item.getQuantity() + 1);
                        db.cartDao().update(item);
                        productExists = true;
                        break;
                    }
                }
                if (!productExists) {
                    Cart cartItem = new Cart();
                    cartItem.setUserId(currentUserId);
                    cartItem.setProductId(product.getId());
                    cartItem.setQuantity(1);
                    db.cartDao().insert(cartItem);
                }
                runOnUiThread(() -> {
                    Toast.makeText(ProductDetailActivity.this, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        ImageButton ibFavorite = findViewById(R.id.ibFavorite);
        updateFavoriteIcon(ibFavorite, currentUserId, product.getId());
        ibFavorite.setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(ProductDetailActivity.this);
                FavoriteDao favoriteDao = db.favoriteDao();
                Favorite existing = favoriteDao.getFavorite(currentUserId, product.getId());
                if (existing == null) {
                    Favorite fav = new Favorite();
                    fav.setUserId(currentUserId);
                    fav.setProductId(product.getId());
                    favoriteDao.insert(fav);
                    runOnUiThread(() -> {
                        ibFavorite.setImageResource(R.drawable.ic_heart_filled);
                        Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    favoriteDao.delete(existing);
                    runOnUiThread(() -> {
                        ibFavorite.setImageResource(R.drawable.ic_heart);
                        Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        setupMenuButtons();
    }

    private void loadProductImages(Product product, ImagePagerAdapter adapter, ViewPager2 viewPager) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            if (product.getMainImageUrl() != null && !product.getMainImageUrl().isEmpty()) {
                adapter.addImage(product.getMainImageUrl());
            }

            List<ProductImage> additionalImages = db.productImageDao().getImagesForProduct(product.getId());
            for (ProductImage image : additionalImages) {
                adapter.addImage(image.getImageUrl());
            }

            runOnUiThread(() -> {
                if (adapter.getItemCount() == 0) {
                    adapter.addImage(R.drawable.error);
                }
                setupViewPagerIndicator(viewPager);
            });
        }).start();
    }

    private void setupViewPagerIndicator(ViewPager2 viewPager) {
        CircleIndicator3 indicator = new CircleIndicator3(this);
        ViewGroup indicatorContainer = findViewById(R.id.indicatorContainer);

        if (indicatorContainer.getChildCount() > 0) {
            indicatorContainer.removeAllViews();
        }

        indicatorContainer.addView(indicator);
        indicator.setViewPager(viewPager);
    }

    private void setupMenuButtons() {
        findViewById(R.id.imageButtonHome).setOnClickListener(v -> {
            startActivity(new Intent(this, RecommendationsActivity.class));
        });

        findViewById(R.id.imageButtonHanger).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogActivity.class));
        });

        findViewById(R.id.imageButtonWardrobe).setOnClickListener(v -> {
            startActivity(new Intent(this, WardrobeActivity.class));
        });

        findViewById(R.id.imageButtonCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        findViewById(R.id.imageButtonProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void updateFavoriteIcon(ImageButton ibFavorite, int userId, int productId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            FavoriteDao favoriteDao = db.favoriteDao();
            Favorite existing = favoriteDao.getFavorite(userId, productId);
            runOnUiThread(() -> {
                if (existing != null) {
                    ibFavorite.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    ibFavorite.setImageResource(R.drawable.ic_heart);
                }
            });
        }).start();
    }
}