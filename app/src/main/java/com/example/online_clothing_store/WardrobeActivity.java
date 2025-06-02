package com.example.online_clothing_store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.dao.FavoriteDao;
import com.example.online_clothing_store.database.entities.Product;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class WardrobeActivity extends AppCompatActivity {
    private int currentUserId;
    private List<Product> hats = new ArrayList<>();
    private List<Product> tops = new ArrayList<>();
    private List<Product> bottoms = new ArrayList<>();
    private List<Product> shoes = new ArrayList<>();
    private int hatIndex = 0, topIndex = 0, bottomIndex = 0, shoesIndex = 0;

    private ImageView ivHat, ivShirt, ivPants, ivShoes;
    private ImageButton btnHatPrev, btnHatNext, btnShirtPrev, btnShirtNext, btnPantsPrev, btnPantsNext, btnShoesPrev, btnShoesNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        setupMenuButtons();
        loadFavoritesAndSetup();
    }

    private void loadFavoritesAndSetup() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            FavoriteDao favoriteDao = db.favoriteDao();
            List<Product> favorites = favoriteDao.getFavoriteProductsForUser(currentUserId);
            for (Product p : favorites) {
                if (p.getCategoryId() == null) continue;
                switch (p.getCategoryId()) {
                    case 1: hats.add(p); break;
                    case 2: tops.add(p); break;
                    case 3: bottoms.add(p); break;
                    case 4: shoes.add(p); break;
                }
            }
            runOnUiThread(this::setupWardrobeUI);
        }).start();
    }

    private void setupWardrobeUI() {
        showProductInCategory(ivHat, hats, hatIndex, R.drawable.ic_cap);
        showProductInCategory(ivShirt, tops, topIndex, R.drawable.ic_shirt);
        showProductInCategory(ivPants, bottoms, bottomIndex, R.drawable.ic_pants);
        showProductInCategory(ivShoes, shoes, shoesIndex, R.drawable.ic_shoes);

        btnHatPrev.setOnClickListener(v -> switchProduct(hats, -1, "hat"));
        btnHatNext.setOnClickListener(v -> switchProduct(hats, 1, "hat"));
        btnShirtPrev.setOnClickListener(v -> switchProduct(tops, -1, "top"));
        btnShirtNext.setOnClickListener(v -> switchProduct(tops, 1, "top"));
        btnPantsPrev.setOnClickListener(v -> switchProduct(bottoms, -1, "bottom"));
        btnPantsNext.setOnClickListener(v -> switchProduct(bottoms, 1, "bottom"));
        btnShoesPrev.setOnClickListener(v -> switchProduct(shoes, -1, "shoes"));
        btnShoesNext.setOnClickListener(v -> switchProduct(shoes, 1, "shoes"));
    }

    private void showProductInCategory(ImageView iv, List<Product> list, int index, int placeholderRes) {
        if (list.isEmpty()) {
            iv.setImageResource(placeholderRes);
        } else {
            Product p = list.get(index);
            if (p.getMainImageUrl() != null && !p.getMainImageUrl().isEmpty()) {
                Picasso.get().load(p.getMainImageUrl()).placeholder(placeholderRes).error(placeholderRes).into(iv);
            } else {
                iv.setImageResource(placeholderRes);
            }
        }
    }

    private void switchProduct(List<Product> list, int delta, String type) {
        if (list.isEmpty()) return;
        switch (type) {
            case "hat":
                hatIndex = (hatIndex + delta + list.size()) % list.size();
                showProductInCategory(ivHat, list, hatIndex, R.drawable.ic_cap);
                break;
            case "top":
                topIndex = (topIndex + delta + list.size()) % list.size();
                showProductInCategory(ivShirt, list, topIndex, R.drawable.ic_shirt);
                break;
            case "bottom":
                bottomIndex = (bottomIndex + delta + list.size()) % list.size();
                showProductInCategory(ivPants, list, bottomIndex, R.drawable.ic_pants);
                break;
            case "shoes":
                shoesIndex = (shoesIndex + delta + list.size()) % list.size();
                showProductInCategory(ivShoes, list, shoesIndex, R.drawable.ic_shoes);
                break;
        }
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
}
